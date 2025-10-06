package config

import javax.inject.{Inject, Singleton}
import play.api.inject.ApplicationLifecycle
import scala.concurrent.{ExecutionContext, Future}
import kafka.KafkaConsumer
import org.slf4j.LoggerFactory

@Singleton
class KafkaStartupHook @Inject()(
                                  kafkaConsumer: KafkaConsumer,
                                  lifecycle: ApplicationLifecycle
                                )(implicit ec: ExecutionContext) {

  private val logger = LoggerFactory.getLogger(getClass)

  try {
    kafkaConsumer.start()
    logger.info("KafkaConsumer started successfully")
  } catch {
    case e: Exception =>
      logger.error("Failed to start KafkaConsumer", e)
  }

  // Graceful shutdown
  lifecycle.addStopHook { () =>
    kafkaConsumer.stop()
    Future.successful(())
  }
}
