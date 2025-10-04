package graphql.builder

import javax.inject._
import graphql.schema.idl.TypeRuntimeWiring
import graphql.datafetchers.PropertyMutationDataFetcher
import scala.concurrent.ExecutionContext

@Singleton
class MutationBuilder @Inject()(
  propertyMutationDataFetcher: PropertyMutationDataFetcher
)(implicit ec: ExecutionContext) {

  def buildTypeWiring(): TypeRuntimeWiring.Builder = {
    TypeRuntimeWiring.newTypeWiring("Mutation")
      .dataFetcher("createProperty", propertyMutationDataFetcher.createProperty)
      .dataFetcher("updateProperty", propertyMutationDataFetcher.updateProperty)
      .dataFetcher("updatePropertyPrice", propertyMutationDataFetcher.updatePropertyPrice)
      .dataFetcher("deleteProperty", propertyMutationDataFetcher.deleteProperty)
  }
}
