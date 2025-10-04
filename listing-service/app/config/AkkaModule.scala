package config

import akka.actor.ActorSystem
import akka.stream.Materializer
import com.google.inject.AbstractModule

class AkkaModule extends AbstractModule {
  
  override def configure(): Unit = {
    bind(classOf[ActorSystem])
      .toProvider(classOf[ActorSystemProvider])
      .asEagerSingleton()
      
    bind(classOf[Materializer])
      .toProvider(classOf[MaterializerProvider])
      .asEagerSingleton()
  }
}