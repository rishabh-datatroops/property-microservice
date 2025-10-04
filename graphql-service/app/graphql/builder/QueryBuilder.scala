package graphql.builder

import javax.inject._
import graphql.schema.idl.TypeRuntimeWiring
import graphql.datafetchers.PropertyQueryDataFetcher
import scala.concurrent.ExecutionContext

@Singleton
class QueryBuilder @Inject()(
  propertyQueryDataFetcher: PropertyQueryDataFetcher
)(implicit ec: ExecutionContext) {

  def buildTypeWiring(): TypeRuntimeWiring.Builder = {
    TypeRuntimeWiring.newTypeWiring("Query")
      .dataFetcher("listProperties", propertyQueryDataFetcher.listProperties)
      .dataFetcher("property", propertyQueryDataFetcher.getPropertyById)
  }
}




