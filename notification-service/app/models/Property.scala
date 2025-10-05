package models

import java.time.Instant
import java.util.UUID
import play.api.libs.json._

case class Property(
                     id: UUID,
                     brokerId: UUID,
                     title: String,
                     description: Option[String],
                     propertyType: String,
                     price: Double,
                     location: String,
                     area: Option[Double],
                     createdAt: Instant,
                     updatedAt: Instant
                   )

object Property {
  implicit val format: Format[Property] = Json.format[Property]
}