package kafka

import akka.actor.ActorSystem
import akka.kafka.ProducerSettings
import akka.kafka.scaladsl.Producer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer
import play.api.libs.json.{JsValue, Json}
import akka.stream.scaladsl.Source
import akka.stream.Materializer
import scala.concurrent.{ExecutionContext, Future}
import messages.property.PropertyEvent

class KafkaProducer(system: ActorSystem, topic: String)(implicit ec: ExecutionContext) {
  
  val producerSettings = ProducerSettings(system, new StringSerializer, new StringSerializer)
    .withBootstrapServers("kafka:9092")
    .withProperty("acks", "all")
    .withProperty("retries", "3")
    .withProperty("batch.size", "16384")
    .withProperty("linger.ms", "1")
    .withProperty("buffer.memory", "33554432")

  /**
   * Send a property event to Kafka
   * @param event The property event to send
   * @param mat Materializer for Akka Streams
   * @return Future that completes when the message is sent
   */
  def sendPropertyEvent(event: PropertyEvent)(implicit mat: Materializer): Future[Unit] = {
    val eventJson = Json.toJson(event)
    val record = new ProducerRecord[String, String](topic, eventJson.toString())
    
    Source.single(record)
      .runWith(Producer.plainSink(producerSettings))
      .map { _ =>
        // Event sent successfully
      }
      .recover { case ex =>
        throw ex
      }
  }

  /**
   * Legacy method for backward compatibility
   * @deprecated Use sendPropertyEvent instead
   */
  def sendMessage(message: JsValue)(implicit mat: Materializer): Unit = {
    val record = new ProducerRecord[String, String](topic, message.toString())
    Source.single(record).runWith(Producer.plainSink(producerSettings))
  }
}
