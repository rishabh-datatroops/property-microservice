package config

import akka.actor.ActorSystem
import akka.stream.Materializer
import com.google.inject.{Inject, Provider}

class MaterializerProvider @Inject()(actorSystem: ActorSystem) extends Provider[Materializer] {
  
  override def get(): Materializer = {
    Materializer(actorSystem)
  }
}