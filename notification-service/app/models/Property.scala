package models

import java.time.Instant
import java.util.UUID
import play.api.libs.json._

case class Property(
                     id: UUID,
                     brokerId: UUID,
                     title: String,
                     description: String,
                     propertyType: String,
                     price: Double,
                     location: String,
                     area: Double,
                     createdAt: Instant,
                     updatedAt: Instant
                   )

object Property {
  implicit val format: Format[Property] = Json.format[Property]
}