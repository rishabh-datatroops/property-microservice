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

@Singleton
class PropertySubscriptionDataFetcher @Inject()(
  implicit ec: ExecutionContext
) {

  // Actor system for managing subscription state
  private implicit val system: ActorSystem[Nothing] = ActorSystem(Behaviors.empty, "PropertySubscriptionSystem")

  // Custom publisher implementation
  private class PropertyPublisher extends Publisher[Property] {
    private val subscribers = new ConcurrentLinkedQueue[Subscriber[_ >: Property]]()

    override def subscribe(subscriber: Subscriber[_ >: Property]): Unit = {
      if (subscriber == null) {
        throw new NullPointerException("Subscriber cannot be null")
      }
      
      subscribers.add(subscriber)
      
      // Create a subscription for this subscriber
      val subscription = new PropertySubscription(subscriber, this)
      subscriber.onSubscribe(subscription)
    }

    def removeSubscriber(subscriber: Subscriber[_ >: Property]): Unit = {
      subscribers.remove(subscriber)
    }

    def publish(property: Property): Unit = {
      import scala.jdk.CollectionConverters._
      subscribers.asScala.foreach { subscriber =>
        try {
          subscriber.onNext(property)
        } catch {
          case e: Exception =>
            println(s"Error publishing to subscriber: ${e.getMessage}")
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
      }
    }
  }

  private val publisher = new PropertyPublisher()

  // Data fetcher that returns a reactive stream publisher
  val newProperty: DataFetcher[Publisher[Property]] = _ => {
    publisher
  }

  // Method to broadcast new properties to all subscribers
  def broadcastProperty(property: Property): Future[Unit] = {
    Future {
      publisher.publish(property)
    }
  }

  // Method to broadcast property updates
  def broadcastPropertyUpdate(property: Property): Future[Unit] = {
    broadcastProperty(property)
  }

  // Method to broadcast property creation
  def broadcastPropertyCreated(property: Property): Future[Unit] = {
    broadcastProperty(property)
  }

  // Cleanup method
  def shutdown(): Unit = {
    system.terminate()
  }
}