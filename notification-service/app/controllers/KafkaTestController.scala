package controllers

import javax.inject._
import play.api.mvc._
import kafka.KafkaConsumer
import scala.concurrent.ExecutionContext

@Singleton
class KafkaTestController @Inject()(
  val controllerComponents: ControllerComponents,
  kafkaConsumer: KafkaConsumer
)(implicit ec: ExecutionContext) extends BaseController {

  def startKafkaConsumer() = Action {
    try {
      kafkaConsumer.start()
      Ok("Kafka consumer started successfully")
    } catch {
      case e: Exception =>
        InternalServerError(s"Failed to start Kafka consumer: ${e.getMessage}")
    }
  }
}
