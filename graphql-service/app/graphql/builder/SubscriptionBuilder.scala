package graphql.builder

import javax.inject._
import graphql.schema.idl.TypeRuntimeWiring
import graphql.datafetchers.PropertySubscriptionDataFetcher
import scala.concurrent.ExecutionContext

@Singleton
class SubscriptionBuilder @Inject()(
  propertySubscriptionDataFetcher: PropertySubscriptionDataFetcher
)(implicit ec: ExecutionContext) {

  def buildTypeWiring(): TypeRuntimeWiring.Builder = {
    TypeRuntimeWiring.newTypeWiring("Subscription")
      .dataFetcher("newProperty", propertySubscriptionDataFetcher.newProperty)
  }
}




