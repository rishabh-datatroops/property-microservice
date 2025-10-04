error id: file://<WORKSPACE>/graphql-service/app/graphql/PropertyGraphQL.scala:graphQL.
file://<WORKSPACE>/graphql-service/app/graphql/PropertyGraphQL.scala
empty definition using pc, found symbol in pc: graphQL.
empty definition using semanticdb
empty definition using fallback
non-local guesses:
	 -javax/inject/schemaBuilder/graphQL.
	 -javax/inject/schemaBuilder/graphQL#
	 -javax/inject/schemaBuilder/graphQL().
	 -schemaBuilder/graphQL.
	 -schemaBuilder/graphQL#
	 -schemaBuilder/graphQL().
	 -scala/Predef.schemaBuilder.graphQL.
	 -scala/Predef.schemaBuilder.graphQL#
	 -scala/Predef.schemaBuilder.graphQL().
offset: 263
uri: file://<WORKSPACE>/graphql-service/app/graphql/PropertyGraphQL.scala
text:
```scala
package graphql

import javax.inject._
import graphql.builder.SchemaBuilder
import scala.concurrent.ExecutionContext

@Singleton
class PropertyGraphQL @Inject()(
  schemaBuilder: SchemaBuilder
)(implicit ec: ExecutionContext) {

  val graphQL = schemaBuilder.grap@@hQL
}
```


#### Short summary: 

empty definition using pc, found symbol in pc: graphQL.