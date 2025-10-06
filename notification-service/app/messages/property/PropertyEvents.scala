package messages.property

import java.util.UUID
import java.time.Instant
import play.api.libs.json._

/**
 * Consistent event models for property-related events across all services
 * These events are published to Kafka and consumed by the notification service
 */

sealed trait PropertyEvent {
  def eventType: String
  def timestamp: Instant
  def propertyId: UUID
}

case class PropertyCreatedEvent(
  propertyId: UUID,
  brokerId: UUID,
  title: String,
  description: Option[String],
  propertyType: String,
  price: Double,
  location: String,
  area: Option[Double],
  timestamp: Instant = Instant.now()
) extends PropertyEvent {
  val eventType = "PropertyCreated"
}

case class PropertyUpdatedEvent(
  propertyId: UUID,
  brokerId: UUID,
  title: String,
  description: Option[String],
  propertyType: String,
  price: Double,
  location: String,
  area: Option[Double],
  timestamp: Instant = Instant.now()
) extends PropertyEvent {
  val eventType = "PropertyUpdated"
}

case class PropertyDeletedEvent(
  propertyId: UUID,
  brokerId: UUID,
  title: String,
  location: String,
  timestamp: Instant = Instant.now()
) extends PropertyEvent {
  val eventType = "PropertyDeleted"
}

// JSON serialization for all event types
object PropertyCreatedEvent {
  implicit val format: Format[PropertyCreatedEvent] = Json.format[PropertyCreatedEvent]
}

object PropertyUpdatedEvent {
  implicit val format: Format[PropertyUpdatedEvent] = Json.format[PropertyUpdatedEvent]
}

object PropertyDeletedEvent {
  implicit val format: Format[PropertyDeletedEvent] = Json.format[PropertyDeletedEvent]
}

object PropertyEvent {
  implicit val format: Format[PropertyEvent] = new Format[PropertyEvent] {
    def writes(event: PropertyEvent): JsValue = {
      event match {
        case e: PropertyCreatedEvent => Json.toJson(e)
        case e: PropertyUpdatedEvent => Json.toJson(e)
        case e: PropertyDeletedEvent => Json.toJson(e)
      }
    }
    
    def reads(json: JsValue): JsResult[PropertyEvent] = {
      // Try to get eventType from JSON, if not present, assume PropertyCreated
      val eventTypeOpt = (json \ "eventType").asOpt[String]
      val eventType = eventTypeOpt.getOrElse("PropertyCreated")
      
      eventType match {
        case "PropertyCreated" => Json.fromJson[PropertyCreatedEvent](json)
        case "PropertyUpdated" => Json.fromJson[PropertyUpdatedEvent](json)
        case "PropertyDeleted" => Json.fromJson[PropertyDeletedEvent](json)
        case _ => JsError(s"Unknown event type: $eventType")
      }
    }
  }
}
