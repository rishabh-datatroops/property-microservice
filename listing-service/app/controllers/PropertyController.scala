package controllers

import akka.actor.ActorSystem
import akka.stream.Materializer
import kafka.KafkaProducer
import messages.property._
import models._
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import play.api.libs.json._
import org.slf4j.LoggerFactory
import play.api.mvc._
import slick.jdbc.PostgresProfile
import slick.jdbc.PostgresProfile.api._

import java.time.Instant
import java.util.UUID
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class PropertyController @Inject()(
  val controllerComponents: ControllerComponents,
  protected val dbConfigProvider: DatabaseConfigProvider,
  system: ActorSystem,
  mat: Materializer
)(implicit ec: ExecutionContext) 
  extends BaseController with HasDatabaseConfigProvider[PostgresProfile] {

  private val logger = LoggerFactory.getLogger(getClass)

  val properties = TableQuery[Properties]
  val kafkaPublisher = new KafkaProducer(system, "property-events")(ec)

  implicit val propertyFormat: Format[Property] = Property.format
  implicit val createPropertyFormat: Format[CreateProperty] = CreateProperty.format
  implicit val updatePropertyFormat: Format[UpdateProperty] = UpdateProperty.format

  def createProperty() = Action.async(parse.json) { request =>
    val createPropertyRequest = request.body.as[CreateProperty]
    logger.info(s"Creating property: ${createPropertyRequest.title}")
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
      updatedAt = Instant.now(),
      deletedAt = None
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
      
      kafkaPublisher.sendPropertyEvent(event)(mat).map { _ =>
        logger.info(s"Property created successfully: ${createdProperty.id}")
        Ok(Json.toJson(createdProperty))
      }.recover { case ex =>
        logger.error(s"Failed to send Kafka event for property ${createdProperty.id}: ${ex.getMessage}", ex)
        Ok(Json.toJson(createdProperty)) // Still return success even if Kafka fails
      }
    }
  }

  def getProperty(id: String) = Action.async {
    logger.info(s"Getting property: $id")
    db.run(properties.filter(p => p.id === UUID.fromString(id) && p.deletedAt.isEmpty).result.headOption)
      .map {
        case Some(prop) => 
          logger.info(s"Property found: $id")
          Ok(Json.toJson(prop))
        case None => 
          logger.warn(s"Property not found: $id")
          NotFound
      }
  }

  def listProperties() = Action.async {
    logger.info("Listing all properties")
    db.run(properties.filter(_.deletedAt.isEmpty).result)
      .map(props => {
        logger.info(s"Found ${props.length} properties")
        Ok(Json.toJson(props))
      })
  }

  def updateProperty(id: String) = Action.async(parse.json) { request =>
    val updateRequest = request.body.as[UpdateProperty]
    logger.info(s"Updating property: $id")

    // Safely convert string to UUID
    val propertyIdTry = scala.util.Try(UUID.fromString(id))
    propertyIdTry.fold(
      _ => Future.successful(BadRequest("Invalid UUID format")),
      propertyId => {
        // Transactional DB action
        val action = for {
          existingOpt <- properties.filter(p => p.id === propertyId && p.deletedAt.isEmpty).result.headOption
          updatedOpt <- existingOpt match {
            case Some(existingProperty) =>
              val updatedProperty = existingProperty.copy(
                title = updateRequest.title.getOrElse(existingProperty.title),
                description = updateRequest.description.orElse(existingProperty.description),
                propertyType = updateRequest.propertyType.getOrElse(existingProperty.propertyType),
                price = updateRequest.price.getOrElse(existingProperty.price),
                location = updateRequest.location.getOrElse(existingProperty.location),
                area = updateRequest.area.orElse(existingProperty.area),
                updatedAt = Instant.now()
              )
              properties.filter(p => p.id === propertyId && p.deletedAt.isEmpty).update(updatedProperty).map(_ => Some(updatedProperty))
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
            
            kafkaPublisher.sendPropertyEvent(event)(mat).map { _ =>
              logger.info(s"Property updated successfully: ${updatedProperty.id}")
              Ok(Json.toJson(updatedProperty))
            }.recover { case ex =>
              logger.error(s"Failed to send Kafka event for property update ${updatedProperty.id}: ${ex.getMessage}", ex)
              Ok(Json.toJson(updatedProperty)) // Still return success even if Kafka fails
            }
          case None =>
            logger.warn(s"Property not found for update: $id")
            Future.successful(NotFound)
        }
      }
    )
  }

  def deleteProperty(id: String) = Action.async {
    logger.info(s"Deleting property: $id")
    val propertyIdTry = scala.util.Try(UUID.fromString(id))
    propertyIdTry.fold(
      _ => Future.successful(BadRequest("Invalid UUID format")),
      propertyId => {
        val action = for {
          existingOpt <- properties.filter(_.id === propertyId).result.headOption
          deleted <- existingOpt match {
            case Some(existingProperty) =>
              properties.filter(_.id === propertyId).delete.map(_ => Some(existingProperty))
            case None =>
              DBIO.successful(None)
          }
        } yield deleted

        db.run(action.transactionally).flatMap {
          case Some(deletedProperty) =>
            // Send PropertyDeletedEvent to Kafka
            val event = PropertyDeletedEvent(
              propertyId = deletedProperty.id,
              brokerId = deletedProperty.brokerId,
              title = deletedProperty.title,
              location = deletedProperty.location,
              timestamp = Instant.now()
            )

            kafkaPublisher.sendPropertyEvent(event)(mat).map { _ =>
              logger.info(s"Property deleted successfully: ${deletedProperty.id}")
              Ok(Json.obj(
                "success" -> true,
                "message" -> s"Property ${deletedProperty.title} deleted successfully"
              ))
            }.recover { case ex =>
              logger.error(s"Failed to send Kafka event for property deletion ${deletedProperty.id}: ${ex.getMessage}", ex)
              Ok(Json.obj(
                "success" -> true,
                "message" -> s"Property ${deletedProperty.title} deleted successfully"
              ))
            }
          case None =>
            logger.warn(s"Property not found for deletion: $id")
            Future.successful(NotFound(Json.obj(
              "success" -> false,
              "message" -> "Property not found"
            )))
        }
      }
    )
  }
}
