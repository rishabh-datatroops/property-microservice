package api.property

import messages.property.{Property, PropertyId, PropertyList, CreateProperty, UpdateProperty}
import scala.concurrent.Future

trait GetPropertyRequest {
  def getAllProperties(): Future[PropertyList]
  def getPropertyById(id: String): Future[Option[Property]]
}

trait CreatePropertyRequest {
  def createProperty(property: CreateProperty): Future[Property]
}

trait UpdatePropertyRequest {
  def updateProperty(id: String, updates: UpdateProperty): Future[Option[Property]]
  def updatePropertyPrice(id: String, price: Double): Future[Option[Property]]
}

trait DeletePropertyRequest {
  def deleteProperty(id: String): Future[Boolean]
}