package graphql.builder

import javax.inject._
import graphql.schema.idl.{RuntimeWiring, TypeRuntimeWiring}
import graphql.datafetchers.PropertyQueryDataFetcher
import scala.concurrent.ExecutionContext

@Singleton
class QueryBuilder @Inject()(
  propertyQueryDataFetcher: PropertyQueryDataFetcher
)(implicit ec: ExecutionContext) extends GraphQLBuilder {

  override protected def schemaFilename: String = "graphql-schema/property/propertyQuery.graphqls"

  override def addRuntimeWiring(runtimeWiringBuilder: RuntimeWiring.Builder): Unit = {
    runtimeWiringBuilder.`type`("QueryRoot", (_: TypeRuntimeWiring.Builder) => buildTypeWiring())
  }

  def buildTypeWiring(): TypeRuntimeWiring.Builder = {
    TypeRuntimeWiring.newTypeWiring("QueryRoot")
      .dataFetcher("listProperties", propertyQueryDataFetcher.listProperties)
      .dataFetcher("property", propertyQueryDataFetcher.getPropertyById)
  }
}




