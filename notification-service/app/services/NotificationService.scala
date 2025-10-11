package services

import messages.property.{PropertyCreatedEvent, PropertyUpdatedEvent, PropertyDeletedEvent}
import models.{NotifyNewProperty, NotifyPropertyUpdate, Property}
import javax.inject._
import org.slf4j.LoggerFactory
import scala.concurrent.{ExecutionContext, Future}
import java.time.Instant

@Singleton
class NotificationService @Inject()(
                                     propertyStateService: PropertyStateService
                                   )(implicit ec: ExecutionContext) {

  private val logger = LoggerFactory.getLogger(getClass)

  private case class Change(field: String, previous: Option[Any], current: Option[Any])

  def processPropertyCreatedEvent(event: PropertyCreatedEvent): Future[Unit] = {
    logger.info(s"[NotificationService] Processing PropertyCreatedEvent for property: ${event.propertyId}")
    val property = convertCreatedEventToProperty(event)
    propertyStateService.updatePropertyState(property)

    val notifyRequest = NotifyNewProperty(
      propertyId = event.propertyId,
      propertyTitle = event.title,
      propertyLocation = event.location,
      propertyPrice = event.price,
      brokerId = event.brokerId
    )

    sendNotification(notifyRequest)
  }

  def processPropertyUpdatedEvent(event: PropertyUpdatedEvent): Future[Unit] = {
    logger.info(s"[NotificationService] Processing PropertyUpdatedEvent for property: ${event.propertyId}")
    val current = convertEventToProperty(event)
    val previousOpt = propertyStateService.getPropertyState(event.propertyId)

    previousOpt match {
      case Some(prev) =>
        val changes = detectChanges(prev, current)
        propertyStateService.updatePropertyState(current)

        if (changes.nonEmpty) {
          val notifyReq = NotifyPropertyUpdate(
            propertyId = event.propertyId,
            propertyTitle = event.title,
            propertyLocation = event.location,
            propertyPrice = event.price,
            brokerId = event.brokerId,
            updatedFields = changes.map(_.field),
            previousPrice = changes.find(_.field == "price").flatMap(_.previous).collect { case d: Double => d },
            previousLocation = changes.find(_.field == "location").flatMap(_.previous).collect { case s: String => s }
          )
          sendNotification(notifyReq)
        } else {
          Future.successful(())
        }
      case None =>
        processPropertyCreatedEvent(PropertyCreatedEvent(
          propertyId = event.propertyId,
          brokerId = event.brokerId,
          title = event.title,
          description = event.description,
          propertyType = event.propertyType,
          price = event.price,
          location = event.location,
          area = event.area,
          timestamp = event.timestamp
        ))
    }
  }

  def processPropertyDeletedEvent(event: PropertyDeletedEvent): Future[Unit] = {
    logger.info(s"[NotificationService] Processing PropertyDeletedEvent for property: ${event.propertyId}")
    propertyStateService.removePropertyState(event.propertyId)
    Future.successful(())
  }

  private def convertCreatedEventToProperty(event: PropertyCreatedEvent): Property =
    Property(event.propertyId, event.brokerId, event.title, event.description, event.propertyType,
      event.price, event.location, event.area, Instant.now(), Instant.now())

  private def convertEventToProperty(event: PropertyUpdatedEvent): Property =
    Property(event.propertyId, event.brokerId, event.title, event.description, event.propertyType,
      event.price, event.location, event.area, Instant.now(), event.timestamp)

  private def detectChanges(prev: Property, current: Property): List[Change] = {
    List(
      ("price", prev.price, current.price),
      ("location", prev.location, current.location),
      ("title", prev.title, current.title),
      ("description", prev.description, current.description),
      ("propertyType", prev.propertyType, current.propertyType),
      ("area", prev.area, current.area)
    ).collect { case (field, p, c) if p != c => Change(field, Some(p), Some(c)) }
  }

  private def sendNotification(notification: Any): Future[Unit] = Future {
    notification match {
      case n: NotifyNewProperty =>
        logger.info(s"[Notification] New property: ${n.propertyId} at ${n.propertyLocation} for ${n.propertyPrice}")
      case u: NotifyPropertyUpdate =>
        logger.info(s"[Notification] Updated property: ${u.propertyId}, updatedFields: ${u.updatedFields.mkString(", ")}")
      case _ =>
        logger.warn(s"Unknown notification type: $notification")
    }
  }
}
