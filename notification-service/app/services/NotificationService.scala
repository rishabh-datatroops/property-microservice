package services

import messages.property.{PropertyCreatedEvent, PropertyDeletedEvent, PropertyUpdatedEvent}
import models.{NotifyNewProperty, NotifyPropertyUpdate, Property, UserPreference}
import org.slf4j.LoggerFactory

import java.time.Instant
import java.util.UUID
import javax.inject._
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class NotificationService @Inject()(
                                     propertyStateService: PropertyStateService
                                   )(implicit ec: ExecutionContext) {

  private val logger = LoggerFactory.getLogger(getClass)

  private case class Change(field: String, previous: Option[Any], current: Option[Any])

  /**
   * Process PropertyCreated events from Kafka
   */
  def processPropertyCreatedEvent(event: PropertyCreatedEvent): Future[Unit] = {
    val notifyRequest = NotifyNewProperty(
      propertyId = event.propertyId,
      propertyTitle = event.title,
      propertyLocation = event.location,
      propertyPrice = event.price,
      brokerId = event.brokerId
    )

    // Update state first (so subsequent updates see this state).
    val property = convertCreatedEventToProperty(event)
    val _ = propertyStateService.updatePropertyState(property)

    processNewPropertyNotification(notifyRequest)
  }

  /**
   * Process PropertyUpdated events from Kafka
   */
  def processPropertyUpdatedEvent(event: PropertyUpdatedEvent): Future[Unit] = {
    val current = convertEventToProperty(event)
    val previousOpt = propertyStateService.getPropertyState(event.propertyId)

    previousOpt match {
      case Some(prev) =>
        val changes = detectChanges(prev, current)
        if (changes.nonEmpty) {
          // typed extraction
          val updatedFields = changes.map(_.field)
          val prevPriceOpt = changes.find(_.field == "price").flatMap(_.previous).collect { case d: Double => d }
          val prevLocationOpt = changes.find(_.field == "location").flatMap(_.previous).collect { case s: String => s }

          val notifyReq = NotifyPropertyUpdate(
            propertyId = event.propertyId,
            propertyTitle = event.title,
            propertyLocation = event.location,
            propertyPrice = event.price,
            brokerId = event.brokerId,
            updatedFields = updatedFields,
            previousPrice = prevPriceOpt,
            previousLocation = prevLocationOpt
          )

          // Update state, then send notification and return its future
          propertyStateService.updatePropertyState(current)
          processPropertyUpdateNotification(notifyReq)
        } else {
          // No changes -> still update timestamp/state
          propertyStateService.updatePropertyState(current)
          Future.successful(())
        }

      case None =>
        // No previous state — treat as new property (safest). Do update + notify as creation
        logger.info(s"No previous state found for ${event.propertyId}. Treating update as create.")
        val notifyRequest = NotifyNewProperty(
          propertyId = event.propertyId,
          propertyTitle = event.title,
          propertyLocation = event.location,
          propertyPrice = event.price,
          brokerId = event.brokerId
        )
        propertyStateService.updatePropertyState(current)
        processNewPropertyNotification(notifyRequest)
    }
  }

  /**
   * Process PropertyDeleted events from Kafka
   */
  def processPropertyDeletedEvent(event: PropertyDeletedEvent): Future[Unit] = {
    propertyStateService.removePropertyState(event.propertyId)

    Future.successful(())
  }

  private def convertCreatedEventToProperty(event: PropertyCreatedEvent): Property = {
    Property(
      id = event.propertyId,
      brokerId = event.brokerId,
      title = event.title,
      description = event.description,
      propertyType = event.propertyType,
      price = event.price,
      location = event.location,
      area = event.area,
      createdAt = Instant.now(),
      updatedAt = Instant.now()
    )
  }

  private def convertEventToProperty(event: PropertyUpdatedEvent): Property = {
    Property(
      id = event.propertyId,
      brokerId = event.brokerId,
      title = event.title,
      description = event.description,
      propertyType = event.propertyType,
      price = event.price,
      location = event.location,
      area = event.area,
      createdAt = Instant.now(), // ideally present on events
      updatedAt = event.timestamp
    )
  }

  private def detectChanges(previous: Property, current: Property): List[Change] = {
    var changes = List.empty[Change]

    if (previous.price != current.price) {
      changes ::= Change("price", Option(previous.price), Option(current.price))
    }

    if (previous.location != current.location) {
      changes ::= Change("location", Option(previous.location), Option(current.location))
    }

    if (previous.title != current.title) {
      changes ::= Change("title", Option(previous.title), Option(current.title))
    }

    if (previous.description != current.description) {
      changes ::= Change("description", Option(previous.description), Option(current.description))
    }

    if (previous.propertyType != current.propertyType) {
      changes ::= Change("propertyType", Option(previous.propertyType), Option(current.propertyType))
    }

    if (previous.area != current.area) {
      changes ::= Change("area", Option(previous.area), Option(current.area))
    }

    changes.reverse
  }

  def processNewPropertyNotification(notifyRequest: NotifyNewProperty): Future[Unit] = {
    // In real implementation: query preferences -> filter -> send notifications
    // Return Future so caller can wait.
    Future {
      logger.info(s"Processing new property notification for ${notifyRequest.propertyId}")
      // non-blocking work here. Remove Thread.sleep in production.
    }
  }

  def processPropertyUpdateNotification(notifyRequest: NotifyPropertyUpdate): Future[Unit] = {
    Future {
      // compute price change metrics (synchronous small calc is fine)
      notifyRequest.previousPrice.foreach { prevPrice =>
        val priceChange = notifyRequest.propertyPrice - prevPrice
        val changePercent = if (prevPrice > 0) ((priceChange / prevPrice) * 100) else 0
        val changeDirection = if (priceChange > 0) "INCREASED" else if (priceChange < 0) "DECREASED" else "UNCHANGED"
        logger.info(s"Property ${notifyRequest.propertyId} price $changeDirection by $changePercent% (from $prevPrice to ${notifyRequest.propertyPrice})")
      }
      logger.info(s"Processing update notification for ${notifyRequest.propertyId} updatedFields=${notifyRequest.updatedFields}")
    }
  }

  def getUserPreferences(userId: UUID): Future[UserPreference] = {
    Future.successful(
      UserPreference(
        userId = userId,
        preferredLocations = List("New York", "San Francisco", "Los Angeles"),
        minPrice = Some(100000.0),
        maxPrice = Some(2000000.0),
        propertyTypes = List("house", "apartment", "condo")
      )
    )
  }

  def matchUserPreferences(property: NotifyNewProperty, preferences: UserPreference): Boolean = {
    val locationMatch =
      preferences.preferredLocations.isEmpty ||
        preferences.preferredLocations.exists(_.toLowerCase.contains(property.propertyLocation.toLowerCase))

    val priceMatch = {
      val aboveMin = preferences.minPrice.forall(min => property.propertyPrice >= min)
      val belowMax = preferences.maxPrice.forall(max => property.propertyPrice <= max)
      aboveMin && belowMax
    }

    val typeMatch = true

    locationMatch && priceMatch && typeMatch
  }

  // Legacy method for backward compatibility (kept but refactored to use typed flows)
  def processPropertyEvent(property: Property): Future[Unit] = {
    val previousProperty = propertyStateService.updatePropertyState(property)

    previousProperty match {
      case Some(prevProp) =>
        processPropertyUpdate(prevProp, property)
      case None =>
        processNewProperty(property)
    }
  }

  private def processNewProperty(property: Property): Future[Unit] = {
    val notifyRequest = NotifyNewProperty(
      propertyId = property.id,
      propertyTitle = property.title,
      propertyLocation = property.location,
      propertyPrice = property.price,
      brokerId = property.brokerId
    )

    processNewPropertyNotification(notifyRequest)
  }

  private def processPropertyUpdate(previousProperty: Property, currentProperty: Property): Future[Unit] = {
    val changes = detectChanges(previousProperty, currentProperty)

    if (changes.nonEmpty) {
      val updatedFields = changes.map(_.field)
      val prevPriceOpt = changes.find(_.field == "price").flatMap(_.previous).collect { case d: Double => d }
      val prevLocationOpt = changes.find(_.field == "location").flatMap(_.previous).collect { case s: String => s }

      val notifyRequest = NotifyPropertyUpdate(
        propertyId = currentProperty.id,
        propertyTitle = currentProperty.title,
        propertyLocation = currentProperty.location,
        propertyPrice = currentProperty.price,
        brokerId = currentProperty.brokerId,
        updatedFields = updatedFields,
        previousPrice = prevPriceOpt,
        previousLocation = prevLocationOpt
      )

      processPropertyUpdateNotification(notifyRequest)
    } else Future.successful(())
  }
}
