package graphql.builder

import graphql.schema.idl.{RuntimeWiring, SchemaParser, TypeDefinitionRegistry}
import scala.io.Source

trait GraphQLBuilder {

  protected def schemaFilename: String

  def loadSchemaContent(): String = {
    val stream = getClass.getClassLoader.getResourceAsStream(schemaFilename)
    if (stream == null) {
      throw new RuntimeException(s"Could not load schema file: $schemaFilename")
    }
    val content = Source.fromInputStream(stream).mkString
    stream.close()
    content
  }

  def mergeSchema(typeRegistry: TypeDefinitionRegistry): Unit = {
    val schemaParser = new SchemaParser
    val schemaContent = loadSchemaContent()
    typeRegistry.merge(schemaParser.parse(schemaContent))
  }

  def addRuntimeWiring(runtimeWiringBuilder: RuntimeWiring.Builder): Unit = {}
}

