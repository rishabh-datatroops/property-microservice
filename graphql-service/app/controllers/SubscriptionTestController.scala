package controllers

import javax.inject._
import play.api.mvc._
import play.api.libs.json._
import services.PropertyUpdateService
import messages.property.Property
import java.util.UUID
import java.time.Instant
import scala.concurrent.ExecutionContext

@Singleton
class SubscriptionTestController @Inject()(
  cc: ControllerComponents,
  propertyUpdateService: PropertyUpdateService
)(implicit ec: ExecutionContext) extends AbstractController(cc) {

  /**
   * Test endpoint to simulate property creation and trigger subscription
   */
  def testPropertyCreation(): Action[AnyContent] = Action.async {
    val testProperty = Property(
      id = UUID.randomUUID(),
      brokerId = UUID.randomUUID(),
      title = "Test Property for Subscription",
      description = "This is a test property to demonstrate real-time subscriptions",
      propertyType = "Apartment",
      price = 250000.0,
      location = "Test Location",
      area = 120.0,
      createdAt = Instant.now(),
      updatedAt = Instant.now()
    )

    propertyUpdateService.onPropertyCreated(testProperty).map { _ =>
      Ok(Json.obj(
        "message" -> "Test property created and broadcasted to subscribers",
        "property" -> Json.toJson(testProperty)
      ))
    }
  }

  /**
   * Test endpoint to simulate property update and trigger subscription
   */
  def testPropertyUpdate(): Action[AnyContent] = Action.async {
    val testProperty = Property(
      id = UUID.randomUUID(),
      brokerId = UUID.randomUUID(),
      title = "Updated Test Property",
      description = "This property has been updated to test subscriptions",
      propertyType = "House",
      price = 300000.0,
      location = "Updated Test Location",
      area = 150.0,
      createdAt = Instant.now(),
      updatedAt = Instant.now()
    )

    propertyUpdateService.onPropertyUpdated(testProperty).map { _ =>
      Ok(Json.obj(
        "message" -> "Test property updated and broadcasted to subscribers",
        "property" -> Json.toJson(testProperty)
      ))
    }
  }

  /**
   * Health check for subscription service
   */
  def subscriptionHealth(): Action[AnyContent] = Action {
    Ok(Json.obj(
      "status" -> "healthy",
      "service" -> "subscription-test",
      "message" -> "Subscription service is running"
    ))
  }
}
