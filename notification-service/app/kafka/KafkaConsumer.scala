package kafka

import akka.actor.ActorSystem
import akka.kafka.scaladsl.Consumer
import akka.kafka.{ConsumerSettings, Subscriptions}
import akka.stream.Materializer
import akka.stream.scaladsl.Sink
import messages.property.{PropertyCreatedEvent, PropertyDeletedEvent, PropertyEvent, PropertyUpdatedEvent}
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import play.api.Configuration
import play.api.libs.json._
import services.NotificationService
import org.slf4j.LoggerFactory

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class KafkaConsumer @Inject()(
                               system: ActorSystem,
                               config: Configuration,
                               notificationService: NotificationService
                             )(implicit ec: ExecutionContext, mat: Materializer) {

  private val logger = LoggerFactory.getLogger(getClass)

  private val bootstrapServers = config.get[String]("kafka.bootstrap.servers")
  private val topic = config.get[String]("kafka.consumer.topic")

  private val consumerSettings = ConsumerSettings(system, new StringDeserializer, new StringDeserializer)
    .withBootstrapServers(bootstrapServers)
    .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")

  def start(): Unit = {
    logger.info(s"Starting Kafka consumer for topic: $topic")

    Consumer
      .plainSource(consumerSettings, Subscriptions.topics(topic))
      .mapAsync(1) { msg =>
        logger.info(s"[KafkaConsumer] Received message from topic: ${msg.topic()}")
        handleMessage(msg.value())
      }
      .runWith(Sink.ignore)
  }

  private def handleMessage(value: String): Future[Unit] = {
    Json.parse(value).validate[PropertyEvent] match {
      case JsSuccess(event, _) =>
        logger.info(s"[KafkaConsumer] Processing ${event.getClass.getSimpleName} for property: ${event.propertyId}")
        event match {
          case e: PropertyCreatedEvent => 
            notificationService.processPropertyCreatedEvent(e)
          case e: PropertyUpdatedEvent => 
            notificationService.processPropertyUpdatedEvent(e)
          case e: PropertyDeletedEvent => 
            notificationService.processPropertyDeletedEvent(e)
        }
      case JsError(errors) =>
        logger.error(s"[KafkaConsumer] Invalid Kafka message: $value, errors: $errors")
        Future.successful(())
    }
  }

  def stop(): Unit = {
    logger.info("KafkaConsumer stopped")
  }
}