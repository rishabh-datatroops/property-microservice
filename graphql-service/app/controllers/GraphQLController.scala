package controllers

import com.fasterxml.jackson.databind.ObjectMapper
import graphql.{ExecutionInput, PropertyGraphQL}
import play.api.libs.json._
import play.api.mvc._
import org.slf4j.LoggerFactory
import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.stream.Materializer

import javax.inject._
import scala.concurrent.{ExecutionContext, Future}
import scala.jdk.CollectionConverters.CollectionHasAsScala
import scala.jdk.FutureConverters._

@Singleton
class GraphQLController @Inject()(
                                   cc: ControllerComponents,
                                   propertyGraphQL: PropertyGraphQL
                                 )(implicit ec: ExecutionContext, system: ActorSystem, mat: Materializer) extends AbstractController(cc) {

  private val logger = LoggerFactory.getLogger(getClass)

  def graphql(): Action[JsValue] = Action.async(parse.json) { request =>
    val queryOpt = (request.body \ "query").asOpt[String]
    val operationName = (request.body \ "operationName").asOpt[String]
    val variablesOpt = (request.body \ "variables").asOpt[JsValue]
    
    logger.info(s"Processing GraphQL request - Operation: ${operationName.getOrElse("unnamed")}")

    queryOpt match {
      case Some(query) =>
        val executionInputBuilder = ExecutionInput.newExecutionInput()
          .query(query)
          .operationName(operationName.orNull)
        
        // Add variables if they exist
        variablesOpt.foreach { variables =>
          try {
            // Convert Play JSON to Java Map using Jackson
            val mapper = new ObjectMapper()
            val variablesMap = mapper.readValue(variables.toString, classOf[java.util.Map[String, Any]])
            executionInputBuilder.variables(variablesMap)
          } catch {
            case e: Exception =>
              logger.error(s"Error parsing variables: ${e.getMessage}", e)
              logger.debug(s"Variables JSON: ${variables.toString}")
          }
        }
        
        val executionInput = executionInputBuilder.build()

        propertyGraphQL.graphQL.executeAsync(executionInput)
          .asScala
          .map { result =>
            if (result.getErrors.isEmpty) {
              logger.info(s"GraphQL query executed successfully - Operation: ${operationName.getOrElse("unnamed")}")
              val data = result.getData[java.util.Map[String, Object]]()
              val mapper = new ObjectMapper()
              Ok(Json.parse(mapper.writeValueAsString(data)))
            } else {
              val errors = result.getErrors.asScala.map(_.getMessage).toSeq
              logger.warn(s"GraphQL query executed with errors: ${errors.mkString(", ")}")
              BadRequest(Json.obj("errors" -> errors))
            }
          }
          .recover {
            case error: Exception =>
              val errorMessage = Option(error.getMessage).getOrElse(error.getClass.getSimpleName)
              logger.error(s"GraphQL execution failed: $errorMessage", error)
              InternalServerError(Json.obj("error" -> errorMessage))
          }

      case None =>
        logger.warn("GraphQL request received without query")
        Future.successful(BadRequest(Json.obj("error" -> "No query found in request body")))
    }
  }

  def health(): Action[AnyContent] = Action {
    Ok(Json.obj("status" -> "healthy", "service" -> "graphql-service"))
  }
}
