package controllers

import javax.inject._
import play.api.mvc._
import play.api.libs.json._
import models._
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class NotificationController @Inject()(val controllerComponents: ControllerComponents)(implicit ec: ExecutionContext) extends BaseController {

  implicit val userPreferenceFormat: Format[UserPreference] = UserPreference.format
  implicit val notifyNewPropertyFormat: Format[NotifyNewProperty] = NotifyNewProperty.format
  implicit val notificationSentFormat: Format[NotificationSent] = NotificationSent.format

  def getPreferences(userId: String) = Action.async {
    // Mock implementation - replace with actual service logic
    val mockPreference = UserPreference(
      userId = java.util.UUID.fromString(userId),
      preferredLocations = List("New York", "San Francisco"),
      minPrice = Some(50000.0),
      maxPrice = Some(1000000.0),
      propertyTypes = List("house", "apartment")
    )
    Future.successful(Ok(Json.toJson(mockPreference)))
  }

  def notifyNewProperty() = Action.async(parse.json) { request =>
    val notifyRequest = request.body.as[NotifyNewProperty]
    
    // Mock implementation - replace with actual notification logic
    println(s"Received notification request: ${notifyRequest.propertyId}")
    
    val mockResponse = NotificationSent(
      success = true,
      messageId = java.util.UUID.randomUUID().toString,
      sentAt = java.time.Instant.now()
    )
    
    Future.successful(Ok(Json.toJson(mockResponse)))
  }
}
