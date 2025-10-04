package models

import java.util.UUID
import play.api.libs.json._

case class UserPreference(
                           userId: UUID,
                           preferredLocations: List[String],
                           minPrice: Option[Double],
                           maxPrice: Option[Double],
                           propertyTypes: List[String]
                         )

object UserPreference {
  implicit val format: Format[UserPreference] = Json.format[UserPreference]
}
