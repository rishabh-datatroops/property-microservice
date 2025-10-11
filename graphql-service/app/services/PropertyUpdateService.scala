package services

import graphql.datafetchers.PropertySubscriptionDataFetcher
import messages.property.Property
import org.slf4j.LoggerFactory

import javax.inject._
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class PropertyUpdateService @Inject()(
  subscriptionDataFetcher: PropertySubscriptionDataFetcher
)(implicit ec: ExecutionContext) {

  private val logger = LoggerFactory.getLogger(getClass)

  def onPropertyCreated(property: Property): Future[Unit] = {
    logger.info(s"Broadcasting new property to subscribers: ${property.id}")
    subscriptionDataFetcher.broadcastPropertyCreated(property)
      .map { _ =>
        logger.info(s"Successfully broadcasted new property to subscribers: ${property.id}")
      }
      .recover {
        case ex: Exception =>
          logger.error(s"Failed to broadcast property creation: ${ex.getMessage}", ex)
      }
  }

  def onPropertyUpdated(property: Property): Future[Unit] = {
    logger.info(s"Broadcasting property update to subscribers: ${property.id}")
    subscriptionDataFetcher.broadcastPropertyUpdate(property)
      .map { _ =>
        logger.info(s"Successfully broadcasted property update to subscribers: ${property.id}")
      }
      .recover {
        case ex: Exception =>
          logger.error(s"Failed to broadcast property update: ${ex.getMessage}", ex)
      }
  }

  def onPropertyDeleted(propertyId: String): Future[Unit] = {
    logger.info(s"Property deleted: $propertyId")
    Future.successful(())
  }
}
