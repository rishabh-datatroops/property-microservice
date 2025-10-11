package services

import javax.inject.{Inject, Singleton}
import models.Property
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import scala.concurrent.ExecutionContext

@Singleton
class PropertyStateService @Inject()(implicit ec: ExecutionContext) {

  // In-memory store for property states (PropertyId -> Property)
  private val propertyStates: ConcurrentHashMap[UUID, Property] = new ConcurrentHashMap()

  def updatePropertyState(newProperty: Property): Option[Property] = {
    val previousState = propertyStates.put(newProperty.id, newProperty)
    Option(previousState)
  }

  def getPropertyState(propertyId: UUID): Option[Property] = {
    Option(propertyStates.get(propertyId))
  }

  def removePropertyState(propertyId: UUID): Option[Property] = {
    Option(propertyStates.remove(propertyId))
  }
}
