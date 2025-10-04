package api.property.rest

import api.property.UpdatePropertyRequest
import messages.property.{Property, UpdateProperty}
import play.api.Configuration
import play.api.libs.json._
import scala.concurrent.{ExecutionContext, Future}
import java.net.http.{HttpClient, HttpRequest, HttpResponse}
import java.net.URI
import java.nio.charset.StandardCharsets
import java.time.Duration

import javax.inject.{Inject, Singleton}

@Singleton
final class UpdatePropertyRestRequest @Inject()(
  implicit protected val configuration: Configuration,
  private val ec: ExecutionContext
) extends UpdatePropertyRequest with PropertyUrl {

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

  def updateProperty(id: String, updates: UpdateProperty): Future[Option[Property]] = {
    val updateBody = Json.toJson(updates).toString()

    makeHttpRequest("PUT", s"$url/api/properties/$id", Some(updateBody)).map { response =>
      if (response.statusCode() == 200) Some(Json.parse(response.body()).as[Property])
      else None
    }.recover { case ex =>
      None
    }
  }

  def updatePropertyPrice(id: String, price: Double): Future[Option[Property]] = {
    val priceUpdate = UpdateProperty(price = Some(price))
    updateProperty(id, priceUpdate)
  }
}
