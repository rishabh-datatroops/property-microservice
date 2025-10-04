package controllers

import javax.inject.{Inject, Singleton}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import play.api.mvc._
import play.api.libs.json._
import models._
import models.Tables._
import models.CreateProperty
import models.UpdateProperty
import slick.jdbc.PostgresProfile
import slick.jdbc.PostgresProfile.api._
import scala.concurrent.{ExecutionContext, Future}
import java.util.UUID
import java.time.Instant
import akka.actor.ActorSystem
import akka.stream.Materializer
import kafka.KafkaProducer
import messages.property._

@Singleton
class PropertyController @Inject()(
  val controllerComponents: ControllerComponents,
  protected val dbConfigProvider: DatabaseConfigProvider,
  system: ActorSystem
)(implicit ec: ExecutionContext, mat: Materializer) 
  extends BaseController with HasDatabaseConfigProvider[PostgresProfile] {

  val properties = TableQuery[Properties]
  val kafkaPublisher = new KafkaProducer(system, "property-events")

  implicit val propertyFormat: Format[Property] = Property.format
  implicit val createPropertyFormat: Format[CreateProperty] = CreateProperty.format
  implicit val updatePropertyFormat: Format[UpdateProperty] = UpdateProperty.format

  def createProperty() = Action.async(parse.json) { request =>
    val createPropertyRequest = request.body.as[CreateProperty]
    val property = Property(
      id = UUID.randomUUID(),
      brokerId = createPropertyRequest.brokerId,
      title = createPropertyRequest.title,
      description = createPropertyRequest.description,
      propertyType = createPropertyRequest.propertyType,
      price = createPropertyRequest.price,
      location = createPropertyRequest.location,
      area = createPropertyRequest.area,
      createdAt = Instant.now(),
      updatedAt = Instant.now()
    )
    
    val action = for {
      _ <- properties += property
    } yield property
    
    db.run(action.transactionally).flatMap { createdProperty =>
      // Create and send PropertyCreated event
      val event = PropertyCreatedEvent(
        propertyId = createdProperty.id,
        brokerId = createdProperty.brokerId,
        title = createdProperty.title,
        description = createdProperty.description,
        propertyType = createdProperty.propertyType,
        price = createdProperty.price,
        location = createdProperty.location,
        area = createdProperty.area,
        timestamp = createdProperty.createdAt
      )
      
      kafkaPublisher.sendPropertyEvent(event).map { _ =>
        Ok(Json.toJson(createdProperty))
      }.recover { case ex =>
        Ok(Json.toJson(createdProperty)) // Still return success even if Kafka fails
      }
    }
  }

  def getProperty(id: String) = Action.async {
    db.run(properties.filter(_.id === UUID.fromString(id)).result.headOption)
      .map {
        case Some(prop) => Ok(Json.toJson(prop))
        case None => NotFound
      }
  }

  def listProperties() = Action.async {
    db.run(properties.result).map(props => Ok(Json.toJson(props)))
  }

  def updateProperty(id: String) = Action.async(parse.json) { request =>
    val updateRequest = request.body.as[UpdateProperty]

    // Safely convert string to UUID
    val propertyIdTry = scala.util.Try(UUID.fromString(id))
    propertyIdTry.fold(
      _ => Future.successful(BadRequest("Invalid UUID format")),
      propertyId => {
        // Transactional DB action
        val action = for {
          existingOpt <- properties.filter(_.id === propertyId).result.headOption
          updatedOpt <- existingOpt match {
            case Some(existingProperty) =>
              val updatedProperty = existingProperty.copy(
                title = updateRequest.title.getOrElse(existingProperty.title),
                description = updateRequest.description.getOrElse(existingProperty.description),
                propertyType = updateRequest.propertyType.getOrElse(existingProperty.propertyType),
                price = updateRequest.price.getOrElse(existingProperty.price),
                location = updateRequest.location.getOrElse(existingProperty.location),
                area = updateRequest.area.getOrElse(existingProperty.area),
                updatedAt = Instant.now()
              )
              properties.filter(_.id === propertyId).update(updatedProperty).map(_ => Some(updatedProperty))
            case None =>
              DBIO.successful(None)
          }
        } yield updatedOpt

        db.run(action.transactionally).flatMap {
          case Some(updatedProperty) =>
            // Create and send PropertyUpdated event
            val event = PropertyUpdatedEvent(
              propertyId = updatedProperty.id,
              brokerId = updatedProperty.brokerId,
              title = updatedProperty.title,
              description = updatedProperty.description,
              propertyType = updatedProperty.propertyType,
              price = updatedProperty.price,
              location = updatedProperty.location,
              area = updatedProperty.area,
              timestamp = updatedProperty.updatedAt
            )
            
            kafkaPublisher.sendPropertyEvent(event).map { _ =>
              Ok(Json.toJson(updatedProperty))
            }
          case None =>
            Future.successful(NotFound)
        }
      }
    )
  }

  def deleteProperty(id: String) = Action.async {
    val propertyIdTry = scala.util.Try(UUID.fromString(id))
    propertyIdTry.fold(
      _ => Future.successful(BadRequest("Invalid UUID format")),
      propertyId => {
        val action = for {
          existingOpt <- properties.filter(_.id === propertyId).result.headOption
          deletedOpt <- existingOpt match {
            case Some(existingProperty) =>
              properties.filter(_.id === propertyId).delete.map(_ => Some(existingProperty))
            case None =>
              DBIO.successful(None)
          }
        } yield deletedOpt

        db.run(action.transactionally).flatMap {
          case Some(deletedProperty) =>
            // Create and send PropertyDeleted event
            val event = PropertyDeletedEvent(
              propertyId = deletedProperty.id,
              brokerId = deletedProperty.brokerId,
              title = deletedProperty.title,
              location = deletedProperty.location,
              timestamp = Instant.now()
            )
            
            kafkaPublisher.sendPropertyEvent(event).map { _ =>
              Ok(Json.obj("success" -> true, "message" -> s"Property ${deletedProperty.title} deleted successfully"))
            }
          case None =>
            Future.successful(NotFound)
        }
      }
    )
  }
}
