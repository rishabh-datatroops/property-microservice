file://<WORKSPACE>/graphql-service/app/api/notification/rest/NotificationRestApi.scala
empty definition using pc, found symbol in pc: 
semanticdb not found
empty definition using fallback
non-local guesses:
	 -api/notification/WSClient#
	 -messages/notification/WSClient#
	 -play/api/libs/ws/WSClient#
	 -WSClient#
	 -scala/Predef.WSClient#
offset: 476
uri: file://<WORKSPACE>/graphql-service/app/api/notification/rest/NotificationRestApi.scala
text:
```scala
package api.notification.rest

import api.notification._
import executor.DefaultExecutor
import messages.notification._
import _root_.rest.RestRequest
import javax.inject.{Inject, Singleton}
import play.api.libs.ws._
import scala.concurrent.Future
import scala.concurrent.duration.FiniteDuration

@Singleton()
final class NotifyNewPropertyRestRequest @Inject() (
  restRequest: RestRequest,
  implicit private val defaultExecutor: DefaultExecutor,
  private val wsClient: WSCl@@ient)
  extends NotifyNewPropertyRequest
    with NotificationUrl {

  protected val route = "notifications/property"

  def apply(notifyNewProperty: NotifyNewProperty)(
    implicit timeout: FiniteDuration): Future[NotificationSent] = {

    restRequest
      .to(url)
      .withTimeout(timeout)
      .postMessage[NotifyNewProperty, NotificationSent](notifyNewProperty)
  }
}

@Singleton()
final class GetUserPreferencesRestRequest @Inject() (
  restRequest: RestRequest,
  implicit private val defaultExecutor: DefaultExecutor,
  private val wsClient: WSClient)
  extends GetUserPreferencesRequest
    with NotificationUrl {

  protected val route = "preferences"

  def apply(userId: String)(
    implicit timeout: FiniteDuration): Future[UserPreference] = {

    restRequest
      .to(s"$url/$userId")
      .withTimeout(timeout)
      .getMessage[UserPreference]()
  }
}

trait NotificationUrl {
  protected val route: String
  protected val baseUrl: String = "http://localhost:9002/api"
  protected val url: String = s"$baseUrl/$route"
}

```


#### Short summary: 

empty definition using pc, found symbol in pc: 