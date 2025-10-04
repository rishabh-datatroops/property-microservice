package api.property

import messages.property._
import scala.concurrent.duration.FiniteDuration
import scala.concurrent.Future
import constants.Defaults

trait ListPropertiesRequest {
  def apply()(
    implicit timeout: FiniteDuration = Defaults.timeout
  ): Future[PropertyList]
}

trait GetPropertyRequest {
  def apply(propertyId: String)(
    implicit timeout: FiniteDuration = Defaults.timeout
  ): Future[Property]
}

trait CreatePropertyRequest {
  def apply(createProperty: CreateProperty)(
    implicit timeout: FiniteDuration = Defaults.timeout
  ): Future[PropertyCreated]
}
