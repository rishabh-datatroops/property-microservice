package config

import akka.actor.ActorSystem
import com.google.inject.Provider
import play.api.Application

class ActorSystemProvider extends Provider[ActorSystem] {
  
  override def get(): ActorSystem = {
    ActorSystem("PropertyMicroserviceActorSystem")
  }
}