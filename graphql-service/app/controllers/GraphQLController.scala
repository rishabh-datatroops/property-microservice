package controllers

import javax.inject._
import play.api.mvc._
import play.api.libs.json._

import scala.concurrent.{ExecutionContext, Future}
import scala.jdk.FutureConverters._
import graphql.PropertyGraphQL
import graphql.ExecutionInput
import com.fasterxml.jackson.databind.ObjectMapper

import scala.jdk.CollectionConverters.CollectionHasAsScala
import scala.jdk.CollectionConverters.MapHasAsJava

@Singleton
class GraphQLController @Inject()(
                                   cc: ControllerComponents,
                                   propertyGraphQL: PropertyGraphQL
                                 )(implicit ec: ExecutionContext) extends AbstractController(cc) {

  def graphql(): Action[JsValue] = Action.async(parse.json) { request =>
    val queryOpt = (request.body \ "query").asOpt[String]
    val operationName = (request.body \ "operationName").asOpt[String]
    val variablesOpt = (request.body \ "variables").asOpt[JsValue]

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
              println(s"Error parsing variables: ${e.getMessage}")
              println(s"Variables JSON: ${variables.toString}")
          }
        }
        
        val executionInput = executionInputBuilder.build()

        propertyGraphQL.graphQL.executeAsync(executionInput)
          .asScala
          .map { result =>
            if (result.getErrors.isEmpty) {
              val data = result.getData[java.util.Map[String, Object]]()
              val mapper = new ObjectMapper()
              Ok(Json.parse(mapper.writeValueAsString(data)))
            } else {
              val errors = result.getErrors.asScala.map(_.getMessage).toSeq
              BadRequest(Json.obj("errors" -> errors))
            }
          }
          .recover {
            case error: Exception =>
              val errorMessage = Option(error.getMessage).getOrElse(error.getClass.getSimpleName)
              InternalServerError(Json.obj("error" -> errorMessage))
          }

      case None =>
        Future.successful(BadRequest(Json.obj("error" -> "No query found in request body")))
    }
  }

  def health(): Action[AnyContent] = Action {
    Ok(Json.obj("status" -> "healthy", "service" -> "graphql-service"))
  }
}
