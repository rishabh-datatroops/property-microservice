package services

import javax.inject._
import messages.property.Property
import graphql.datafetchers.PropertySubscriptionDataFetcher
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

@Singleton
class PropertyUpdateService @Inject()(
  subscriptionDataFetcher: PropertySubscriptionDataFetcher
)(implicit ec: ExecutionContext) {

  /**
   * Called when a new property is created
   * Broadcasts the property to all GraphQL subscribers
   */
  def onPropertyCreated(property: Property): Future[Unit] = {
    subscriptionDataFetcher.broadcastPropertyCreated(property)
      .map { _ =>
        println(s"Broadcasted new property to subscribers: ${property.id}")
      }
      .recover {
        case ex: Exception =>
          println(s"Failed to broadcast property creation: ${ex.getMessage}")
      }
  }

  /**
   * Called when a property is updated
   * Broadcasts the updated property to all GraphQL subscribers
   */
  def onPropertyUpdated(property: Property): Future[Unit] = {
    subscriptionDataFetcher.broadcastPropertyUpdate(property)
      .map { _ =>
        println(s"Broadcasted property update to subscribers: ${property.id}")
      }
      .recover {
        case ex: Exception =>
          println(s"Failed to broadcast property update: ${ex.getMessage}")
      }
  }

  /**
   * Called when a property is deleted
   * Could broadcast a deletion event to subscribers
   */
  def onPropertyDeleted(propertyId: String): Future[Unit] = {
    // For now, just log the deletion
    // In a full implementation, you might want to broadcast deletion events
    Future {
      println(s"Property deleted: $propertyId")
    }
  }
}
