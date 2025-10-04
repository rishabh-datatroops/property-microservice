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

  val listProperties: DataFetcher[java.util.concurrent.CompletableFuture[java.util.List[Property]]] = _ => {
    getPropertyRequest.getAllProperties().map(_.properties.asJava).asJava.toCompletableFuture
  }

  val getPropertyById: DataFetcher[java.util.concurrent.CompletableFuture[Property]] = env => {
    val id = env.getArgument[String]("id")
    getPropertyRequest.getPropertyById(id).map {
      case Some(property) => property
      case None => throw new RuntimeException(s"Property with id $id not found")
    }.asJava.toCompletableFuture
  }
}
