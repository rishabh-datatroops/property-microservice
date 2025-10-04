package models

import java.util.UUID
import play.api.libs.json._

case class NotifyNewProperty(
  propertyId: UUID,
  propertyTitle: String,
  propertyLocation: String,
  propertyPrice: Double,
  brokerId: UUID
)

case class NotifyPropertyUpdate(
  propertyId: UUID,
  propertyTitle: String,
  propertyLocation: String,
  propertyPrice: Double,
  brokerId: UUID,
  updatedFields: List[String], // List of fields that were updated (e.g., ["price", "location"])
  previousPrice: Option[Double] = None, // Previous price if price was updated
  previousLocation: Option[String] = None // Previous location if location was updated
)

case class NotificationSent(
  success: Boolean,
  messageId: String,
  sentAt: java.time.Instant
)

object NotifyNewProperty {
  implicit val format: Format[NotifyNewProperty] = Json.format[NotifyNewProperty]
}

object NotifyPropertyUpdate {
  implicit val format: Format[NotifyPropertyUpdate] = Json.format[NotifyPropertyUpdate]
}

object NotificationSent {
  implicit val format: Format[NotificationSent] = Json.format[NotificationSent]
}


