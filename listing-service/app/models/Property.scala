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
                     updatedAt: Instant,
                     deletedAt: Option[Instant] = None
                   )

object Property {
  implicit val format: Format[Property] = Json.format[Property]
}

case class CreateProperty(
  brokerId: UUID,
  title: String,
  description: Option[String],
  propertyType: String,
  price: Double,
  location: String,
  area: Option[Double]
)

object CreateProperty {
  implicit val format: Format[CreateProperty] = Json.format[CreateProperty]
}

case class UpdateProperty(
  title: Option[String] = None,
  description: Option[String] = None,
  propertyType: Option[String] = None,
  price: Option[Double] = None,
  location: Option[String] = None,
  area: Option[Double] = None
)

object UpdateProperty {
  implicit val format: Format[UpdateProperty] = Json.format[UpdateProperty]
}
