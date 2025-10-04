package controllers

import javax.inject._
import play.api.mvc._
import kafka.KafkaConsumer
import scala.concurrent.ExecutionContext

@Singleton
class TestController @Inject()(
  val controllerComponents: ControllerComponents,
  kafkaConsumer: KafkaConsumer
)(implicit ec: ExecutionContext) extends BaseController {

  def startKafka() = Action {
    try {
      kafkaConsumer.start()
      Ok("Kafka consumer started manually")
    } catch {
      case e: Exception =>
        InternalServerError(s"Failed to start Kafka consumer: ${e.getMessage}")
    }
  }

  def testKafka() = Action {
    Ok("Kafka test endpoint called")
  }
}
