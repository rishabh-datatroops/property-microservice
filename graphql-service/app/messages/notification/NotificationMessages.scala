package messages.notification

import messages.property.Property
import play.api.libs.json._

case class NotifyNewProperty(property: Property)

object NotifyNewProperty {
  implicit val format: Format[NotifyNewProperty] = Json.format[NotifyNewProperty]
}

case class NotificationSent(success: Boolean, message: String)

object NotificationSent {
  implicit val format: Format[NotificationSent] = Json.format[NotificationSent]
}

case class UserPreference(
  userId: String,
  emailNotifications: Boolean,
  smsNotifications: Boolean,
  propertyTypes: List[String]
)

object UserPreference {
  implicit val format: Format[UserPreference] = Json.format[UserPreference]
}




