package graphql.builder

import graphql.GraphQL
import graphql.execution.{AsyncExecutionStrategy, AsyncSerialExecutionStrategy}
import graphql.schema.idl._
import graphql.scalars.ExtendedScalars

import javax.inject._
import scala.concurrent.ExecutionContext
import scala.io.Source

@Singleton
class SchemaBuilder @Inject()(
  queryBuilder: QueryBuilder,
  mutationBuilder: MutationBuilder,
  subscriptionBuilder: SubscriptionBuilder
)(implicit ec: ExecutionContext) {

  // Base schema files (types and inputs)
  private val baseSchemaFiles = Seq(
    "graphql-schema/property/propertyTypes.graphqls",
    "graphql-schema/property/propertyInputs.graphqls",
    "graphql-schema/index.graphqls"
  )

  private def loadSchemaFromResource(path: String): String = {
    val stream = getClass.getClassLoader.getResourceAsStream(path)
    if (stream == null) {
      throw new RuntimeException(s"Could not load schema file: $path")
    }
    val content = Source.fromInputStream(stream).mkString
    stream.close()
    content
  }

  private def loadBaseSchemas(typeRegistry: TypeDefinitionRegistry): Unit = {
    val schemaParser = new SchemaParser()
    baseSchemaFiles.foreach { path =>
      val content = loadSchemaFromResource(path)
      typeRegistry.merge(schemaParser.parse(content))
    }
  }

  // Build type registry by merging all schemas
  private val typeRegistry = {
    val registry = new TypeDefinitionRegistry()
    
    // Load base schemas first
    loadBaseSchemas(registry)
    
    // Merge schemas from builders
    queryBuilder.mergeSchema(registry)
    mutationBuilder.mergeSchema(registry)
    subscriptionBuilder.mergeSchema(registry)
    
    registry
  }

  // Build runtime wiring using builders
  private val runtimeWiring = {
    val builder = RuntimeWiring.newRuntimeWiring()
    
    // Add custom scalar types
    builder.scalar(ExtendedScalars.Json)
    builder.scalar(ExtendedScalars.GraphQLLong)
    
    // Add wiring from each builder
    queryBuilder.addRuntimeWiring(builder)
    mutationBuilder.addRuntimeWiring(builder)
    subscriptionBuilder.addRuntimeWiring(builder)
    
    builder.build()
  }

  // Generate the executable schema
  private val schema = new SchemaGenerator().makeExecutableSchema(typeRegistry, runtimeWiring)

  // Build the GraphQL instance
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
