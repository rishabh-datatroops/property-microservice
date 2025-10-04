package api.property.rest

import play.api.Configuration

trait PropertyUrl {
  protected implicit val configuration: Configuration
  protected val listingServiceUrl = configuration.get[String]("services.listing-service.url")
  protected val url = listingServiceUrl
}
