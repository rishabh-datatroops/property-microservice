package api.notification

import messages.notification._
import scala.concurrent.duration.FiniteDuration
import scala.concurrent.Future
import constants.Defaults

trait NotifyNewPropertyRequest {
  def apply(notifyNewProperty: NotifyNewProperty)(
    implicit timeout: FiniteDuration = Defaults.timeout
  ): Future[NotificationSent]
}

trait GetUserPreferencesRequest {
  def apply(userId: String)(
    implicit timeout: FiniteDuration = Defaults.timeout
  ): Future[UserPreference]
}
