package config

import javax.inject.{Inject, Singleton}
import play.api.inject.ApplicationLifecycle
import scala.concurrent.Future
import kafka.KafkaConsumer

@Singleton
class KafkaStartupHook @Inject()(
  kafkaConsumer: KafkaConsumer,
  lifecycle: ApplicationLifecycle
) {
  
  // Start Kafka consumer when application starts
  try {
    kafkaConsumer.start()
  } catch {
    case e: Exception =>
      // Kafka consumer failed to start
  }
  
  // Cleanup when application stops
  lifecycle.addStopHook { () =>
    Future.successful(())
  }
}
