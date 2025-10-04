package services

import javax.inject.{Inject, Singleton}
import models.Property
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class PropertyStateService @Inject()(implicit ec: ExecutionContext) {

  // In-memory store for property states (PropertyId -> Property)
  private val propertyStates: ConcurrentHashMap[UUID, Property] = new ConcurrentHashMap()

  /**
   * Updates the state of a property and returns its previous state if it existed.
   * This method is thread-safe.
   * @param newProperty The new or updated property.
   * @return An Option containing the previous Property state if it existed, otherwise None.
   */
  def updatePropertyState(newProperty: Property): Option[Property] = {
    val previousState = propertyStates.put(newProperty.id, newProperty)
    Option(previousState)
  }

  /**
   * Retrieves the current state of a property.
   * @param propertyId The ID of the property to retrieve.
   * @return An Option containing the Property if found, otherwise None.
   */
  def getPropertyState(propertyId: UUID): Option[Property] = {
    Option(propertyStates.get(propertyId))
  }

  /**
   * Removes a property from state tracking.
   * @param propertyId The ID of the property to remove.
   * @return An Option containing the removed Property if it existed, otherwise None.
   */
  def removePropertyState(propertyId: UUID): Option[Property] = {
    val removedProperty = propertyStates.remove(propertyId)
    Option(removedProperty)
  }

  /**
   * Clears all stored property states. Useful for testing or system resets.
   */
  def clearAllStates(): Unit = {
    propertyStates.clear()
  }

  /**
   * Returns the number of properties currently tracked.
   */
  def getTrackedPropertyCount(): Int = {
    propertyStates.size()
  }
}
