package graphql.datafetchers

import api.property.GetPropertyRequest
import graphql.schema.DataFetcher
import messages.property.Property

import javax.inject._
import scala.concurrent.ExecutionContext
import scala.jdk.CollectionConverters._
import scala.jdk.FutureConverters._

@Singleton
class PropertyQueryDataFetcher @Inject()(
  private val getPropertyRequest: GetPropertyRequest
)(implicit ec: ExecutionContext) {

  // Helper method to convert Property to a GraphQL-compatible map
  private def propertyToMap(property: Property): java.util.Map[String, Any] = {
    val map = new java.util.HashMap[String, Any]()
    map.put("id", property.id.toString)
    map.put("brokerId", property.brokerId.toString)
    map.put("title", property.title)
    map.put("description", property.description.orNull)
    map.put("propertyType", property.propertyType)
    map.put("price", property.price)
    map.put("location", property.location)
    map.put("area", property.area.map(_.asInstanceOf[java.lang.Double]).orNull)
    map.put("createdAt", property.createdAt.toString)
    map.put("updatedAt", property.updatedAt.toString)
    map
  }

  val listProperties: DataFetcher[java.util.concurrent.CompletableFuture[java.util.List[java.util.Map[String, Any]]]] = _ => {
    getPropertyRequest.getAllProperties().map { propertyList =>
      propertyList.properties.map(propertyToMap).asJava
    }.asJava.toCompletableFuture
  }

  val getPropertyById: DataFetcher[java.util.concurrent.CompletableFuture[java.util.Map[String, Any]]] = env => {
    val id = env.getArgument[String]("id")
    getPropertyRequest.getPropertyById(id).map {
      case Some(property) => propertyToMap(property)
      case None => throw new RuntimeException(s"Property with id $id not found")
    }.asJava.toCompletableFuture
  }
}
