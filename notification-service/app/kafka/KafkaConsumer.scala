package kafka

import javax.inject.{Inject, Singleton}
import akka.actor.ActorSystem
import akka.kafka.ConsumerSettings
import akka.kafka.scaladsl.Consumer
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import play.api.libs.json._
import play.api.Configuration
import akka.stream.Materializer
import scala.concurrent.ExecutionContext
import services.NotificationService
import messages.property.{PropertyEvent, PropertyCreatedEvent, PropertyUpdatedEvent, PropertyDeletedEvent}

@Singleton
class KafkaConsumer @Inject()(
  system: ActorSystem,
  config: Configuration,
  notificationService: NotificationService
)(implicit ec: ExecutionContext, mat: Materializer) {

  private val bootstrapServers = config.get[String]("kafka.bootstrapServers")
  private val groupId = config.get[String]("kafka.consumer.groupId")
  private val topic = config.get[String]("kafka.consumer.topic")

  val consumerSettings = ConsumerSettings(system, new StringDeserializer, new StringDeserializer)
    .withBootstrapServers(bootstrapServers)
    .withGroupId(groupId)
    .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")

  def start(): Unit = {
    try {
      // Use a simpler approach with explicit materializer
      import akka.stream.scaladsl.Sink
      
      val source = Consumer.plainSource(consumerSettings, akka.kafka.Subscriptions.topics(topic))
      
      source.runWith(Sink.foreach { record =>
        try {
          val eventJson = Json.parse(record.value())
          val event = eventJson.as[PropertyEvent]

          // Process the event based on its type
          event match {
            case createdEvent: PropertyCreatedEvent =>
              notificationService.processPropertyCreatedEvent(createdEvent)
            case updatedEvent: PropertyUpdatedEvent =>
              notificationService.processPropertyUpdatedEvent(updatedEvent)
            case deletedEvent: PropertyDeletedEvent =>
              notificationService.processPropertyDeletedEvent(deletedEvent)
          }

        } catch {
          case e: Exception =>
            // Error processing Kafka message
        }
      })(mat)
      
    } catch {
      case e: Exception =>
        // Failed to start Kafka consumer
    }
  }
}
