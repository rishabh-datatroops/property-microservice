package graphql.builder

import javax.inject._
import graphql.schema._
import graphql.schema.idl._
import graphql.schema.idl.RuntimeWiring._
import graphql.schema.idl.TypeRuntimeWiring._
import graphql.GraphQL
import graphql.execution.AsyncExecutionStrategy
import graphql.execution.AsyncSerialExecutionStrategy
import scala.concurrent.ExecutionContext

@Singleton
class SchemaBuilder @Inject()(
  queryBuilder: QueryBuilder,
  mutationBuilder: MutationBuilder,
  subscriptionBuilder: SubscriptionBuilder
)(implicit ec: ExecutionContext) {

  private val schemaDefinition = """
  type Property {
    id: ID!
    title: String!
    description: String
    propertyType: String!
    price: Float!
    location: String!
    area: Float
    createdAt: String!
    updatedAt: String!
  }

  type Query {
    listProperties: [Property!]!
    property(id: ID!): Property
  }

  input CreatePropertyInput {
    title: String!
    price: Float!
    location: String!
    propertyType: String!
    description: String
    area: Float
    brokerId: String
  }

  input UpdatePropertyInput {
    id: ID!
    title: String
    price: Float
    location: String
    propertyType: String
    description: String
    area: Float
  }

  type Mutation {
    createProperty(input: CreatePropertyInput!): Property!
    updateProperty(input: UpdatePropertyInput!): Property!
    updatePropertyPrice(id: ID!, price: Float!): Property!
    deleteProperty(id: ID!): Boolean!
  }

  type Subscription {
    newProperty: Property!
  }

  schema {
    query: Query
    mutation: Mutation
    subscription: Subscription
  }
"""


  private val runtimeWiring = {
    RuntimeWiring.newRuntimeWiring()
      .`type`("Query", (builder: TypeRuntimeWiring.Builder) => queryBuilder.buildTypeWiring())
      .`type`("Mutation", (builder: TypeRuntimeWiring.Builder) => mutationBuilder.buildTypeWiring())
      .`type`("Subscription", (builder: TypeRuntimeWiring.Builder) => subscriptionBuilder.buildTypeWiring())
      .build()
  }

  private val typeRegistry = new SchemaParser().parse(schemaDefinition)
  private val schema = new SchemaGenerator().makeExecutableSchema(typeRegistry, runtimeWiring)

  val graphQL: GraphQL = GraphQL.newGraphQL(schema)
    .queryExecutionStrategy(new AsyncExecutionStrategy())
    .mutationExecutionStrategy(new AsyncSerialExecutionStrategy())
    .defaultDataFetcherExceptionHandler(new graphql.execution.DataFetcherExceptionHandler() {
      override def handleException(handlerParameters: graphql.execution.DataFetcherExceptionHandlerParameters): java.util.concurrent.CompletableFuture[graphql.execution.DataFetcherExceptionHandlerResult] = {
        val exception = handlerParameters.getException
        val errorMessage = Option(exception.getMessage).getOrElse(exception.getClass.getSimpleName)
        val error = graphql.GraphqlErrorBuilder.newError()
          .message(errorMessage)
          .build()
        val result = graphql.execution.DataFetcherExceptionHandlerResult.newResult()
          .error(error)
          .build()
        java.util.concurrent.CompletableFuture.completedFuture(result)
      }
    })
    .build()
}
