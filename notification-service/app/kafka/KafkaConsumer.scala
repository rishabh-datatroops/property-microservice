package kafka

import akka.actor.ActorSystem
import akka.kafka.{ConsumerSettings, Subscriptions}
import akka.kafka.scaladsl.Consumer
import akka.stream.Materializer
import messages.property.{PropertyCreatedEvent, PropertyDeletedEvent, PropertyEvent, PropertyUpdatedEvent}
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.slf4j.LoggerFactory
import play.api.Configuration
import play.api.libs.json._
import services.NotificationService

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

@Singleton
class KafkaConsumer @Inject()(
                               system: ActorSystem,
                               config: Configuration,
                               notificationService: NotificationService
                             )(implicit ec: ExecutionContext, mat: Materializer) {

  private val logger = LoggerFactory.getLogger(getClass)

  private val bootstrapServers = config.get[String]("kafka.bootstrapServers")
  private val groupId = config.get[String]("kafka.consumer.groupId")
  private val topic = config.get[String]("kafka.consumer.topic")

  val consumerSettings = ConsumerSettings(system, new StringDeserializer, new StringDeserializer)
    .withBootstrapServers(bootstrapServers)
    .withGroupId(groupId)
    .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")

  def start(): Unit = {
    try {
      import akka.stream.scaladsl.Sink

      val source = Consumer.plainSource(consumerSettings, Subscriptions.topics(topic))

      // Process events sequentially => mapAsync(1) keeps ordering and allows backpressure
      source
        .mapAsync(1) { record =>
          Future {
            record
          }.flatMap { r =>
            try {
              val eventJson = Json.parse(r.value())
              val event = eventJson.as[PropertyEvent]
              event match {
                case created: PropertyCreatedEvent =>
                  notificationService.processPropertyCreatedEvent(created)
                case updated: PropertyUpdatedEvent =>
                  notificationService.processPropertyUpdatedEvent(updated)
                case deleted: PropertyDeletedEvent =>
                  notificationService.processPropertyDeletedEvent(deleted)
              }
            } catch {
              case ex: Throwable =>
                logger.error(s"Failed to parse/process record: offset=${r.offset()} key=${r.key()}", ex)
                Future.successful(())
            }
          }.andThen {
            case Success(_) =>
            case Failure(ex) => logger.error("Error processing kafka event", ex)
          }
        }
        .runWith(Sink.ignore)(mat)

    } catch {
      case e: Exception =>
        logger.error("Failed to start Kafka consumer", e)
    }
  }
}
