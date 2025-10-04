package api.property.rest

import api.property.DeletePropertyRequest
import play.api.Configuration
import scala.concurrent.{ExecutionContext, Future}
import java.net.http.{HttpClient, HttpRequest, HttpResponse}
import java.net.URI
import java.nio.charset.StandardCharsets
import java.time.Duration

import javax.inject.{Inject, Singleton}

@Singleton
final class DeletePropertyRestRequest @Inject()(
  implicit protected val configuration: Configuration,
  private val ec: ExecutionContext
) extends DeletePropertyRequest with PropertyUrl {

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

  def deleteProperty(id: String): Future[Boolean] = {
    makeHttpRequest("DELETE", s"$url/api/properties/$id").map { response =>
      response.statusCode() match {
        case 200 => true
        case 404 => false
        case _   => false
      }
    }.recover { case ex =>
      false
    }
  }
}
