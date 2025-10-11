package controllers

import javax.inject._
import play.api.mvc._
import play.api.libs.json._
import models._
import scala.concurrent.{ExecutionContext, Future}
import org.slf4j.LoggerFactory

@Singleton
class NotificationController @Inject()(
                                        val controllerComponents: ControllerComponents
                                      )(implicit ec: ExecutionContext) extends BaseController {

  private val logger = LoggerFactory.getLogger(getClass)

  implicit val notifyNewPropertyFormat: Format[NotifyNewProperty] = NotifyNewProperty.format
  implicit val notificationSentFormat: Format[NotificationSent] = NotificationSent.format

  def notifyNewProperty() = Action.async(parse.json) { request =>
    val notifyRequest = request.body.as[NotifyNewProperty]

    logger.info(s"Received notification request: ${notifyRequest.propertyId}")
    
    val response = NotificationSent(
      success = true,
      messageId = java.util.UUID.randomUUID().toString,
      sentAt = java.time.Instant.now()
    )
    
    logger.info(s"Notification sent successfully for property: ${notifyRequest.propertyId}")
    Future.successful(Ok(Json.toJson(response)))
  }
}
