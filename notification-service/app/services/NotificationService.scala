package services

import javax.inject._
import models.{NotifyNewProperty, NotifyPropertyUpdate, UserPreference, Property}
import messages.property.{PropertyCreatedEvent, PropertyUpdatedEvent, PropertyDeletedEvent}
import scala.concurrent.{ExecutionContext, Future}
import java.time.Instant
import java.util.UUID

@Singleton
class NotificationService @Inject()(
  propertyStateService: PropertyStateService
)(implicit ec: ExecutionContext) {
  
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
    
    processNewPropertyNotification(notifyRequest)
  }
  
  /**
   * Process PropertyUpdated events from Kafka
   */
  def processPropertyUpdatedEvent(event: PropertyUpdatedEvent): Future[Unit] = {
    // Get previous state to detect changes
    val previousProperty = propertyStateService.getPropertyState(event.propertyId)
    
    previousProperty match {
      case Some(prevProp) =>
        val changes = detectChanges(prevProp, convertEventToProperty(event))
        
        if (changes.nonEmpty) {
          // Create update notification with detected changes
          val updatedFields = changes.map(_._1).toList
          val priceChange = changes.find(_._1 == "price").map(_._2.asInstanceOf[Option[Double]])
          val locationChange = changes.find(_._1 == "location").map(_._2.asInstanceOf[Option[String]])
          
          val notifyRequest = NotifyPropertyUpdate(
            propertyId = event.propertyId,
            propertyTitle = event.title,
            propertyLocation = event.location,
            propertyPrice = event.price,
            brokerId = event.brokerId,
            updatedFields = updatedFields,
            previousPrice = priceChange.flatten,
            previousLocation = locationChange.flatten
          )
          
          processPropertyUpdateNotification(notifyRequest)
        } else {
          Future.successful(())
        }
      case None =>
        processPropertyUpdatedEvent(event)
    }
    
    // Update the property state
    propertyStateService.updatePropertyState(convertEventToProperty(event))
    Future.successful(())
  }
  
  /**
   * Process PropertyDeleted events from Kafka
   */
  def processPropertyDeletedEvent(event: PropertyDeletedEvent): Future[Unit] = {
    // Remove from state tracking
    propertyStateService.removePropertyState(event.propertyId)
    
    // In a real implementation, this would:
    // 1. Notify users who had this property in their favorites/saved searches
    // 2. Send "Property No Longer Available" notifications
    // 3. Update user preferences and recommendations
    
    Future {
      // Simulate processing time
      Thread.sleep(500)
    }
  }
  
  /**
   * Convert PropertyUpdatedEvent to Property model for compatibility
   */
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
      createdAt = Instant.now(), // We don't have this in the event
      updatedAt = event.timestamp
    )
  }
  
  /**
   * Detect changes between previous and current property states
   * Returns a list of (fieldName, previousValue, currentValue) tuples
   */
  private def detectChanges(previous: Property, current: Property): List[(String, Option[Any], Option[Any])] = {
    var changes = List.empty[(String, Option[Any], Option[Any])]
    
    if (previous.price != current.price) {
      changes = ("price", Some(previous.price), Some(current.price)) :: changes
    }
    
    if (previous.location != current.location) {
      changes = ("location", Some(previous.location), Some(current.location)) :: changes
    }
    
    if (previous.title != current.title) {
      changes = ("title", Some(previous.title), Some(current.title)) :: changes
    }
    
    if (previous.description != current.description) {
      changes = ("description", Some(previous.description), Some(current.description)) :: changes
    }
    
    if (previous.propertyType != current.propertyType) {
      changes = ("propertyType", Some(previous.propertyType), Some(current.propertyType)) :: changes
    }
    
    if (previous.area != current.area) {
      changes = ("area", Some(previous.area), Some(current.area)) :: changes
    }
    
    changes.reverse // Return in original order
  }

  def processNewPropertyNotification(notifyRequest: NotifyNewProperty): Future[Unit] = {
    // In a real implementation, this would:
    // 1. Fetch all user preferences
    // 2. Filter users who match the property criteria
    // 3. Send notifications (email, SMS, push, etc.)
    
    Future {
      // Simulate processing time
      Thread.sleep(500)
    }
  }
  
  def processPropertyUpdateNotification(notifyRequest: NotifyPropertyUpdate): Future[Unit] = {
    // Special handling for price changes
    notifyRequest.previousPrice.foreach { prevPrice =>
      val priceChange = notifyRequest.propertyPrice - prevPrice
      val changePercent = if (prevPrice > 0) ((priceChange / prevPrice) * 100) else 0
      val changeDirection = if (priceChange > 0) "INCREASED" else if (priceChange < 0) "DECREASED" else "UNCHANGED"
      
      // In a real implementation, this would:
      // 1. Fetch users who are interested in this property (saved searches, favorites, etc.)
      // 2. Send notifications about the specific price change
      // 3. Different notification templates based on price increase/decrease
      // 4. Send alerts to users who have price alerts set up
    }
    
    Future {
      // Simulate processing time
      Thread.sleep(500)
    }
  }
  
  def getUserPreferences(userId: UUID): Future[UserPreference] = {
    // Mock implementation - return default preferences
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
    // Check location
    val locationMatch = preferences.preferredLocations.isEmpty || 
                       preferences.preferredLocations.exists(_.toLowerCase.contains(property.propertyLocation.toLowerCase))
    
    // Check price range
    val priceMatch = preferences.minPrice.isEmpty || property.propertyPrice >= preferences.minPrice.get &&
                   preferences.maxPrice.isEmpty || property.propertyPrice <= preferences.maxPrice.get
    
    // Note: propertyType is not available in NotifyNewProperty model
    // For now, assume all properties match type preferences
    val typeMatch = true
    
    locationMatch && priceMatch && typeMatch
  }
  
  // Legacy method for backward compatibility
  def processPropertyEvent(property: Property): Future[Unit] = {
    val previousProperty = propertyStateService.updatePropertyState(property)
    
    previousProperty match {
      case Some(prevProp) =>
        // This is an update - check for changes
        processPropertyUpdate(prevProp, property)
      case None =>
        // This is a new property
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
      // Create update notification with detected changes
      val updatedFields = changes.map(_._1).toList
      val priceChange = changes.find(_._1 == "price").map(_._2.asInstanceOf[Option[Double]])
      val locationChange = changes.find(_._1 == "location").map(_._2.asInstanceOf[Option[String]])
      
      val notifyRequest = NotifyPropertyUpdate(
        propertyId = currentProperty.id,
        propertyTitle = currentProperty.title,
        propertyLocation = currentProperty.location,
        propertyPrice = currentProperty.price,
        brokerId = currentProperty.brokerId,
        updatedFields = updatedFields,
        previousPrice = priceChange.flatten,
        previousLocation = locationChange.flatten
      )
      
      processPropertyUpdateNotification(notifyRequest)
    } else {
      Future.successful(())
    }
  }
}
