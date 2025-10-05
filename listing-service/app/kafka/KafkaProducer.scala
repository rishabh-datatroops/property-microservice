package kafka

import akka.actor.ActorSystem
import akka.kafka.ProducerSettings
import akka.kafka.scaladsl.Producer
import akka.stream.Materializer
import akka.stream.scaladsl.Source
import messages.property.PropertyEvent
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer
import play.api.libs.json.Json

import scala.concurrent.{ExecutionContext, Future}

class KafkaProducer(system: ActorSystem, topic: String)(implicit ec: ExecutionContext) {
  
  val producerSettings = ProducerSettings(system, new StringSerializer, new StringSerializer)
    .withBootstrapServers(system.settings.config.getString("kafka.bootstrap.servers"))
    .withProperty("acks", "all")
    .withProperty("retries", "3")
    .withProperty("batch.size", "16384")
    .withProperty("linger.ms", "1")
    .withProperty("buffer.memory", "33554432")

  def sendPropertyEvent(event: PropertyEvent)(implicit mat: Materializer): Future[Unit] = {
    val eventJson = Json.toJson(event)
    val record = new ProducerRecord[String, String](topic, event.propertyId.toString, eventJson.toString())
    Source.single(record)
      .runWith(Producer.plainSink(producerSettings))
      .map { _ =>
        // Event sent successfully
      }
      .recover { case ex =>
        system.log.error(s"Failed to send property event: ${ex.getMessage}", ex)
        throw ex
      }
  }
}
