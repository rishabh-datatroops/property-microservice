package graphql.builder

import javax.inject._
import graphql.schema.idl.{RuntimeWiring, TypeRuntimeWiring}
import graphql.datafetchers.PropertySubscriptionDataFetcher
import scala.concurrent.ExecutionContext

@Singleton
class SubscriptionBuilder @Inject()(
  propertySubscriptionDataFetcher: PropertySubscriptionDataFetcher
)(implicit ec: ExecutionContext) extends GraphQLBuilder {

  override protected def schemaFilename: String = "graphql-schema/property/propertySubscription.graphqls"

  override def addRuntimeWiring(runtimeWiringBuilder: RuntimeWiring.Builder): Unit = {
    runtimeWiringBuilder.`type`("SubscriptionRoot", (_: TypeRuntimeWiring.Builder) => buildTypeWiring())
  }

  def buildTypeWiring(): TypeRuntimeWiring.Builder = {
    TypeRuntimeWiring.newTypeWiring("SubscriptionRoot")
      .dataFetcher("newProperty", propertySubscriptionDataFetcher.newProperty)
  }
}




