package graphql.builder

import javax.inject._
import graphql.schema.idl.{RuntimeWiring, TypeRuntimeWiring}
import graphql.datafetchers.PropertyMutationDataFetcher
import scala.concurrent.ExecutionContext

@Singleton
class MutationBuilder @Inject()(
  propertyMutationDataFetcher: PropertyMutationDataFetcher
)(implicit ec: ExecutionContext) extends GraphQLBuilder {

  override protected def schemaFilename: String = "graphql-schema/property/propertyMutations.graphqls"

  override def addRuntimeWiring(runtimeWiringBuilder: RuntimeWiring.Builder): Unit = {
    runtimeWiringBuilder.`type`("MutationRoot", (_: TypeRuntimeWiring.Builder) => buildTypeWiring())
  }

  def buildTypeWiring(): TypeRuntimeWiring.Builder = {
    TypeRuntimeWiring.newTypeWiring("MutationRoot")
      .dataFetcher("createProperty", propertyMutationDataFetcher.createProperty)
      .dataFetcher("updateProperty", propertyMutationDataFetcher.updateProperty)
      .dataFetcher("updatePropertyPrice", propertyMutationDataFetcher.updatePropertyPrice)
      .dataFetcher("deleteProperty", propertyMutationDataFetcher.deleteProperty)
  }
}
