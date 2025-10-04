package config

import com.google.inject.AbstractModule
import play.api.libs.concurrent.PekkoGuiceSupport
import kafka.KafkaConsumer
import services.PropertyStateService
import services.NotificationService

class KafkaModule extends AbstractModule with PekkoGuiceSupport {
  
  override def configure(): Unit = {
    // Bind services as eager singletons
    bind(classOf[KafkaConsumer]).asEagerSingleton()
    bind(classOf[PropertyStateService]).asEagerSingleton()
    bind(classOf[NotificationService]).asEagerSingleton()
    
    // Bind startup hook
    bind(classOf[KafkaStartupHook]).asEagerSingleton()
  }
}
