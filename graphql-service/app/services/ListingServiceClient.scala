package services

import javax.inject._
import messages.property._
import scala.concurrent.{ExecutionContext, Future}
import play.api.libs.json._
import play.api.Configuration
import java.time.Instant
import java.util.UUID
import java.net.http.{HttpClient, HttpRequest, HttpResponse}
import java.net.URI
import java.nio.charset.StandardCharsets
import java.time.Duration

@Singleton
class ListingServiceClient @Inject()(config: Configuration)(implicit ec: ExecutionContext) {

  private val listingServiceUrl = config.get[String]("services.listing-service.url")
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

  def getAllProperties(): Future[List[Property]] = {
    makeHttpRequest("GET", s"$listingServiceUrl/api/properties").map { response =>
      if (response.statusCode() == 200) Json.parse(response.body()).as[List[Property]]
      else List.empty
    }.recover { case ex =>
      List.empty
    }
  }

  def getPropertyById(id: String): Future[Option[Property]] = {
    makeHttpRequest("GET", s"$listingServiceUrl/api/properties/$id").map { response =>
      response.statusCode() match {
        case 200 => Some(Json.parse(response.body()).as[Property])
        case 404 => None
        case _   => None
      }
    }.recover { case ex =>
      None
    }
  }

  def createProperty(property: Property): Future[Property] = {
    val body = Json.toJson(
      CreateProperty(
        brokerId = property.brokerId,
        title = property.title,
        description = property.description,
        propertyType = property.propertyType,
        price = property.price,
        location = property.location,
        area = property.area
      )
    ).toString()

    makeHttpRequest("POST", s"$listingServiceUrl/api/properties", Some(body)).map { response =>
      if (response.statusCode() == 200) Json.parse(response.body()).as[Property]
      else throw new RuntimeException(s"Failed to create property: ${response.statusCode()} - ${response.body()}")
    }
  }

  def updateProperty(id: String, updates: Map[String, Any]): Future[Option[Property]] = {
    val updateBody = Json.toJson(
      UpdateProperty(
        title = updates.get("title").map(_.toString),
        description = updates.get("description").map(_.toString),
        propertyType = updates.get("propertyType").map(_.toString),
        price = updates.get("price").map(_.toString.toDouble),
        location = updates.get("location").map(_.toString),
        area = updates.get("area").map(_.toString.toDouble)
      )
    ).toString()

    makeHttpRequest("PUT", s"$listingServiceUrl/api/properties/$id", Some(updateBody)).map { response =>
      if (response.statusCode() == 200) Some(Json.parse(response.body()).as[Property])
      else None
    }.recover { case ex =>
      None
    }
  }

  def deleteProperty(id: String): Future[Boolean] = {
    println(s"[DEBUG] Deleting property with id: $id")
    makeHttpRequest("DELETE", s"$listingServiceUrl/api/properties/$id").map { response =>
      println(s"[DEBUG] Delete response body: ${response.body()}")
      response.statusCode() match {
        case 200 => true
        case 404 => false
        case _   => false
      }
    }.recover { case ex =>
      println(s"[DEBUG] Delete request failed with exception: $ex")
      false
    }
  }

  def updatePropertyPrice(id: String, newPrice: Double): Future[Option[Property]] =
    updateProperty(id, Map("price" -> newPrice))
}
