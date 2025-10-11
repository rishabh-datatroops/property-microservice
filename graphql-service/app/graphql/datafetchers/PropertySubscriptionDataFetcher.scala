package graphql.datafetchers

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import graphql.schema.DataFetcher
import messages.property.Property
import org.reactivestreams.{Publisher, Subscriber, Subscription}

import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject._
import scala.concurrent.{ExecutionContext, Future}
import org.slf4j.LoggerFactory

@Singleton
class PropertySubscriptionDataFetcher @Inject()(
                                                 implicit ec: ExecutionContext
                                               ) {

  private val logger = LoggerFactory.getLogger(getClass)

  // Custom publisher implementation
  private class PropertyPublisher extends Publisher[Property] {
    private val subscribers = new ConcurrentLinkedQueue[Subscriber[_ >: Property]]()

    override def subscribe(subscriber: Subscriber[_ >: Property]): Unit = {
      if (subscriber == null) {
        throw new NullPointerException("Subscriber cannot be null")
      }

      subscribers.add(subscriber)
      logger.info(s"New subscriber added: $subscriber | Total subscribers: ${subscribers.size()}")

      // Create a subscription for this subscriber
      val subscription = new PropertySubscription(subscriber, this)
      subscriber.onSubscribe(subscription)
    }

    def removeSubscriber(subscriber: Subscriber[_ >: Property]): Unit = {
      subscribers.remove(subscriber)
      logger.info(s"Subscriber removed: $subscriber | Total subscribers: ${subscribers.size()}")
    }

    def publish(property: Property): Unit = {
      import scala.jdk.CollectionConverters._
      logger.info(s"Broadcasting property: ${property.id} to ${subscribers.size()} subscribers")
      subscribers.asScala.foreach { subscriber =>
        try {
          subscriber.onNext(property)
        } catch {
          case e: Exception =>
            logger.error(s"Error publishing to subscriber: ${e.getMessage}", e)
            removeSubscriber(subscriber)
        }
      }
    }
  }

  private class PropertySubscription(
                                      subscriber: Subscriber[_ >: Property],
                                      publisher: PropertyPublisher
                                    ) extends Subscription {
    private val cancelled = new AtomicBoolean(false)

    override def request(n: Long): Unit = {
      // For simplicity, we don't implement backpressure here
      // In a production system, you'd want to implement proper backpressure
    }

    override def cancel(): Unit = {
      if (cancelled.compareAndSet(false, true)) {
        publisher.removeSubscriber(subscriber)
        logger.info(s"Subscription cancelled by subscriber: $subscriber")
      }
    }
  }

  private val publisher = new PropertyPublisher()

  val newProperty: DataFetcher[Publisher[Property]] = _ => publisher

  // Broadcast a new property to all subscribers
  def broadcastProperty(property: Property): Future[Unit] = Future {
    logger.info(s"Broadcasting property event for propertyId: ${property.id}")
    publisher.publish(property)
  }

  // Broadcast property updates
  def broadcastPropertyUpdate(property: Property): Future[Unit] = broadcastProperty(property)

  // Broadcast property creation
  def broadcastPropertyCreated(property: Property): Future[Unit] = broadcastProperty(property)
}
