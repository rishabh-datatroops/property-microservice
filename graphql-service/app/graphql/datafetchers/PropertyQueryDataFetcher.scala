package graphql.datafetchers

import graphql.schema.DataFetcher
import messages.property.Property
import services.ListingServiceClient

import javax.inject._
import scala.concurrent.ExecutionContext
import scala.jdk.CollectionConverters._
import scala.jdk.FutureConverters._

@Singleton
class PropertyQueryDataFetcher @Inject()(
  listingService: ListingServiceClient
)(implicit ec: ExecutionContext) {

  val listProperties: DataFetcher[java.util.concurrent.CompletableFuture[java.util.List[Property]]] = _ => {
    listingService.getAllProperties().map(_.asJava).asJava.toCompletableFuture
  }

  val getPropertyById: DataFetcher[java.util.concurrent.CompletableFuture[Property]] = env => {
    val id = env.getArgument[String]("id")
    listingService.getPropertyById(id).map {
      case Some(property) => property
      case None => throw new RuntimeException(s"Property with id $id not found")
    }.asJava.toCompletableFuture
  }
}
