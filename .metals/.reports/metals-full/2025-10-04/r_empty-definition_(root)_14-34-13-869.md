error id: file://<WORKSPACE>/graphql-service/app/services/NotificationServiceClient.scala:
file://<WORKSPACE>/graphql-service/app/services/NotificationServiceClient.scala
empty definition using pc, found symbol in pc: 
empty definition using semanticdb
empty definition using fallback
non-local guesses:
	 -javax/inject/NotificationRestApi.
	 -javax/inject/NotificationRestApi#
	 -javax/inject/NotificationRestApi().
	 -api/notification/rest/NotificationRestApi.
	 -api/notification/rest/NotificationRestApi#
	 -api/notification/rest/NotificationRestApi().
	 -NotificationRestApi.
	 -NotificationRestApi#
	 -NotificationRestApi().
	 -scala/Predef.NotificationRestApi.
	 -scala/Predef.NotificationRestApi#
	 -scala/Predef.NotificationRestApi().
offset: 247
uri: file://<WORKSPACE>/graphql-service/app/services/NotificationServiceClient.scala
text:
```scala
package services

import javax.inject._
import messages.notification.{NotifyNewProperty, NotificationSent, UserPreference}
import api.notification.{NotifyNewPropertyRequest, GetUserPreferencesRequest}
import api.notification.rest.NotificationRestA@@pi
import constants.Defaults
import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration.FiniteDuration

@Singleton
class NotificationServiceClient @Inject()(
  notifyNewPropertyRequest: NotifyNewPropertyRequest,
  getUserPreferencesRequest: GetUserPreferencesRequest
)(implicit ec: ExecutionContext) {

  def notifyNewProperty(notifyNewProperty: NotifyNewProperty)(
    implicit timeout: FiniteDuration = Defaults.timeout
  ): Future[NotificationSent] = {
    notifyNewPropertyRequest(notifyNewProperty)
  }

  def getUserPreferences(userId: String)(
    implicit timeout: FiniteDuration = Defaults.timeout
  ): Future[UserPreference] = {
    getUserPreferencesRequest(userId)
  }
}

```


#### Short summary: 

empty definition using pc, found symbol in pc: 