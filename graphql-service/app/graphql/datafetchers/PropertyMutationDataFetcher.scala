package graphql.datafetchers

import api.property.{CreatePropertyRequest, DeletePropertyRequest, UpdatePropertyRequest}
import graphql.schema.DataFetcher
import messages.property.{CreateProperty, Property, UpdateProperty}
import services.PropertyUpdateService

import java.util.UUID
import javax.inject._
import scala.concurrent.{ExecutionContext, Future}
import scala.jdk.FutureConverters._

@Singleton
class PropertyMutationDataFetcher @Inject()(
  private val createPropertyRequest: CreatePropertyRequest,
  private val updatePropertyRequest: UpdatePropertyRequest,
  private val deletePropertyRequest: DeletePropertyRequest,
  private val propertyUpdateService: PropertyUpdateService
)(implicit ec: ExecutionContext) {

  /** Helper to build a CreateProperty object */
  private def buildCreateProperty(
    title: String,
    price: Double,
    location: String,
    propertyType: String,
    description: String,
    area: Double,
    brokerId: UUID
  ): CreateProperty = CreateProperty(
    brokerId = brokerId,
    title = title,
    description = Option(description),
    propertyType = propertyType,
    price = price,
    location = location,
    area = Option(area)
  )

  /** Single createProperty data fetcher */
  val createProperty: DataFetcher[java.util.concurrent.CompletableFuture[Property]] = env => {
    val input = env.getArgument[java.util.Map[String, Any]]("input")

    val title = input.get("title").toString
    val price = input.get("price").toString.toDouble
    val location = input.get("location").toString
    val propertyType = input.get("propertyType").toString
    val description = Option(input.get("description")).map(_.toString).getOrElse("")
    val area = Option(input.get("area")).map(_.toString.toDouble).getOrElse(1000.0)
    val brokerId = Option(input.get("brokerId")).map(b => UUID.fromString(b.toString)).getOrElse(UUID.randomUUID())

    val createPropertyData = buildCreateProperty(title, price, location, propertyType, description, area, brokerId)

    // Create the property and broadcast to subscribers
    val result = createPropertyRequest.createProperty(createPropertyData).flatMap { createdProperty =>
      // Broadcast the new property to GraphQL subscribers
      propertyUpdateService.onPropertyCreated(createdProperty).map(_ => createdProperty)
    }

    result.asJava.toCompletableFuture
  }

  /** Update property */
  val updateProperty: DataFetcher[java.util.concurrent.CompletableFuture[Property]] = env => {
    val input = env.getArgument[java.util.Map[String, Any]]("input")
    val id = input.get("id").toString

    val updates = UpdateProperty(
      title = Option(input.get("title")).map(_.toString),
      description = Option(input.get("description")).map(_.toString),
      propertyType = Option(input.get("propertyType")).map(_.toString),
      price = Option(input.get("price")).map(_.toString.toDouble),
      location = Option(input.get("location")).map(_.toString),
      area = Option(input.get("area")).map(_.toString.toDouble)
    )

    val result = updatePropertyRequest.updateProperty(id, updates).flatMap {
      case Some(updatedProperty) => 
        // Broadcast the property update to GraphQL subscribers
        propertyUpdateService.onPropertyUpdated(updatedProperty).map(_ => updatedProperty)
      case None => 
        Future.failed(new RuntimeException(s"Property with id $id not found"))
    }

    result.asJava.toCompletableFuture
  }

  /** Update only price */
  val updatePropertyPrice: DataFetcher[java.util.concurrent.CompletableFuture[Property]] = env => {
    val id = env.getArgument[String]("id")
    val price = env.getArgument[Double]("price")

    val result = updatePropertyRequest.updatePropertyPrice(id, price).flatMap {
      case Some(updatedProperty) => 
        // Broadcast the property update to GraphQL subscribers
        propertyUpdateService.onPropertyUpdated(updatedProperty).map(_ => updatedProperty)
      case None => 
        Future.failed(new RuntimeException(s"Property with id $id not found"))
    }

    result.asJava.toCompletableFuture
  }

  /** Delete property */
  val deleteProperty: DataFetcher[java.util.concurrent.CompletableFuture[Boolean]] = env => {
    val id = env.getArgument[String]("id")

    val result = deletePropertyRequest.deleteProperty(id).flatMap { success =>
      if (success) {
        // Notify about property deletion
        propertyUpdateService.onPropertyDeleted(id).map(_ => success)
      } else {
        Future.failed(new RuntimeException(s"Property with id $id not found or could not be deleted"))
      }
    }

    result.asJava.toCompletableFuture
  }
}
