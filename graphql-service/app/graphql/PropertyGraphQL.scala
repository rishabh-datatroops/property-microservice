package graphql

import javax.inject._
import graphql.builder.SchemaBuilder
import scala.concurrent.ExecutionContext

@Singleton
class PropertyGraphQL @Inject()(
  schemaBuilder: SchemaBuilder
)(implicit ec: ExecutionContext) {

  val graphQL = schemaBuilder.graphQL
}