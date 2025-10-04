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

object NotifyNewProperty {
  implicit val format: Format[NotifyNewProperty] = Json.format[NotifyNewProperty]
}

