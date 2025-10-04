package modules

import api.property.{GetPropertyRequest, CreatePropertyRequest, UpdatePropertyRequest, DeletePropertyRequest}
import api.property.rest.{GetPropertyRestRequest, CreatePropertyRestRequest, UpdatePropertyRestRequest, DeletePropertyRestRequest}
import com.google.inject.AbstractModule

class PropertyApiModule extends AbstractModule {
  override def configure(): Unit = {
    bind(classOf[GetPropertyRequest]).to(classOf[GetPropertyRestRequest])
    bind(classOf[CreatePropertyRequest]).to(classOf[CreatePropertyRestRequest])
    bind(classOf[UpdatePropertyRequest]).to(classOf[UpdatePropertyRestRequest])
    bind(classOf[DeletePropertyRequest]).to(classOf[DeletePropertyRestRequest])
  }
}
