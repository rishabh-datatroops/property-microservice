package kafka

import akka.actor.ActorSystem
import akka.kafka.scaladsl.Consumer
import akka.kafka.{ConsumerSettings, Subscriptions}
import akka.stream.{ActorAttributes, Materializer, Supervision}
import akka.stream.scaladsl.Sink
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import play.api.Configuration
import play.api.libs.json._
import services.NotificationService
import messages.property.{PropertyCreatedEvent, PropertyDeletedEvent, PropertyEvent, PropertyUpdatedEvent}
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
  private val groupId = config.get[String]("kafka.consumer.groupId")
  private val topic = config.get[String]("kafka.consumer.topic")

  private val consumerSettings = ConsumerSettings(system, new StringDeserializer, new StringDeserializer)
    .withBootstrapServers(bootstrapServers)
    .withGroupId(groupId)
    .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")

  private var running = true

  def start(): Unit = {
    val decider: Supervision.Decider = {
      case ex: Throwable =>
        logger.error("Error in Kafka stream", ex)
        Supervision.Resume
    }

    val source = Consumer.plainSource(consumerSettings, Subscriptions.topics(topic))

    source
      .mapAsync(1)(processRecord) // now takes ConsumerRecord[String, String]
      .withAttributes(ActorAttributes.supervisionStrategy(decider))
      .runWith(Sink.ignore)
  }

    private def processRecord(record: org.apache.kafka.clients.consumer.ConsumerRecord[String, String]): Future[Unit] = {
    val value = record.value()
    Json.parse(value).validate[PropertyEvent] match {
      case JsSuccess(event, _) =>
        event match {
          case created: PropertyCreatedEvent =>
            notificationService.processPropertyCreatedEvent(created)
          case updated: PropertyUpdatedEvent =>
            notificationService.processPropertyUpdatedEvent(updated)
          case deleted: PropertyDeletedEvent =>
            notificationService.processPropertyDeletedEvent(deleted)
        }
      case JsError(errors) =>
        logger.error(s"Failed to parse Kafka message JSON: $value, errors: $errors")
        Future.successful(())
    }
  }

  def stop(): Unit = {
    running = false
    logger.info("KafkaConsumer stopped")
  }
}
