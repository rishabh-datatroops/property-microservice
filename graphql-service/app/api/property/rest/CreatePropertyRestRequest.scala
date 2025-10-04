package api.property.rest

import api.property.CreatePropertyRequest
import messages.property.{CreateProperty, Property}
import play.api.Configuration
import play.api.libs.json._
import scala.concurrent.{ExecutionContext, Future}
import java.net.http.{HttpClient, HttpRequest, HttpResponse}
import java.net.URI
import java.nio.charset.StandardCharsets
import java.time.Duration

import javax.inject.{Inject, Singleton}

@Singleton
final class CreatePropertyRestRequest @Inject()(
  implicit protected val configuration: Configuration,
  private val ec: ExecutionContext
) extends CreatePropertyRequest with PropertyUrl {

  private val httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build()

  private def makeHttpRequest(method: String, url: String, bodyOpt: Option[String] = None): Future[HttpResponse[String]] = {
    val builder = HttpRequest.newBuilder()
      .uri(URI.create(url))
      .timeout(Duration.ofSeconds(30))
      .header("Content-Type", "application/json")

    val request = method.toUpperCase match {
      case "GET"    => builder.GET().build()
      case "POST"   => builder.POST(HttpRequest.BodyPublishers.ofString(bodyOpt.getOrElse(""))).build()
      case "PUT"    => builder.PUT(HttpRequest.BodyPublishers.ofString(bodyOpt.getOrElse(""))).build()
      case "DELETE" => builder.DELETE().build()
      case _        => throw new IllegalArgumentException(s"Unsupported HTTP method: $method")
    }

    Future {
      httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8))
    }
  }

  def createProperty(createProperty: CreateProperty): Future[Property] = {
    val body = Json.toJson(createProperty).toString()

    makeHttpRequest("POST", s"$url/api/properties", Some(body)).map { response =>
      if (response.statusCode() == 200) Json.parse(response.body()).as[Property]
      else throw new RuntimeException(s"Failed to create property: ${response.statusCode()} - ${response.body()}")
    }
  }
}
