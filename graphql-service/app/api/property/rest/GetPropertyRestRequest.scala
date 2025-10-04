package api.property.rest

import api.property.GetPropertyRequest
import messages.property.{Property, PropertyList}
import play.api.Configuration
import play.api.libs.json._
import scala.concurrent.{ExecutionContext, Future}
import java.net.http.{HttpClient, HttpRequest, HttpResponse}
import java.net.URI
import java.nio.charset.StandardCharsets
import java.time.Duration

import javax.inject.{Inject, Singleton}

@Singleton
final class GetPropertyRestRequest @Inject()(
  implicit protected val configuration: Configuration,
  private val ec: ExecutionContext
) extends GetPropertyRequest with PropertyUrl {

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

  def getAllProperties(): Future[PropertyList] = {
    makeHttpRequest("GET", s"$url/api/properties").map { response =>
      if (response.statusCode() == 200) {
        val properties = Json.parse(response.body()).as[List[Property]]
        PropertyList(properties)
      } else {
        PropertyList(List.empty)
      }
    }.recover { case ex =>
      PropertyList(List.empty)
    }
  }

  def getPropertyById(id: String): Future[Option[Property]] = {
    makeHttpRequest("GET", s"$url/api/properties/$id").map { response =>
      response.statusCode() match {
        case 200 => Some(Json.parse(response.body()).as[Property])
        case 404 => None
        case _   => None
      }
    }.recover { case ex =>
      None
    }
  }
}
