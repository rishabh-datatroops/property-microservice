package graphql.datafetchers

import javax.inject._
import graphql.schema.DataFetcher
import messages.property.Property
import services.ListingServiceClient
import java.util.UUID
import java.time.Instant
import scala.concurrent.{ExecutionContext, Future}
import scala.jdk.FutureConverters._

@Singleton
class PropertyMutationDataFetcher @Inject()(
  listingService: ListingServiceClient,
  propertyUpdateService: services.PropertyUpdateService
)(implicit ec: ExecutionContext) {

  /** Helper to build a Property object */
  private def buildProperty(
    title: String,
    price: Double,
    location: String,
    propertyType: String,
    description: String,
    area: Double,
    brokerId: UUID
  ): Property = Property(
    id = UUID.randomUUID(),
    brokerId = brokerId,
    title = title,
    description = description,
    propertyType = propertyType,
    price = price,
    location = location,
    area = area,
    createdAt = Instant.now(),
    updatedAt = Instant.now()
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

    val property = buildProperty(title, price, location, propertyType, description, area, brokerId)

    // Create the property and broadcast to subscribers
    val result = listingService.createProperty(property).flatMap { createdProperty =>
      // Broadcast the new property to GraphQL subscribers
      propertyUpdateService.onPropertyCreated(createdProperty).map(_ => createdProperty)
    }

    result.asJava.toCompletableFuture
  }

  /** Update property */
  val updateProperty: DataFetcher[java.util.concurrent.CompletableFuture[Property]] = env => {
    val input = env.getArgument[java.util.Map[String, Any]]("input")
    val id = input.get("id").toString

    val updates = scala.collection.mutable.Map[String, Any]()
    Option(input.get("title")).foreach(v => updates.put("title", v.toString))
    Option(input.get("description")).foreach(v => updates.put("description", v.toString))
    Option(input.get("propertyType")).foreach(v => updates.put("propertyType", v.toString))
    Option(input.get("price")).foreach(v => updates.put("price", v.toString.toDouble))
    Option(input.get("location")).foreach(v => updates.put("location", v.toString))
    Option(input.get("area")).foreach(v => updates.put("area", v.toString.toDouble))

    val result = listingService.updateProperty(id, updates.toMap).flatMap {
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

    val result = listingService.updatePropertyPrice(id, price).flatMap {
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

    val result = listingService.deleteProperty(id).flatMap { success =>
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
