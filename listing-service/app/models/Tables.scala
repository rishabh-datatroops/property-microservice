package models

import slick.jdbc.PostgresProfile.api._
import java.util.UUID
import java.time.Instant

object Tables {
  lazy val Properties: TableQuery[Properties] = TableQuery[Properties]
}

class Properties(tag: Tag) extends Table[Property](tag, "properties") {
  def id = column[UUID]("id", O.PrimaryKey)
  def brokerId = column[UUID]("broker_id")
  def title = column[String]("title")
  def description = column[String]("description")
  def propertyType = column[String]("property_type")
  def price = column[Double]("price")
  def location = column[String]("location")
  def area = column[Double]("area")
  def createdAt = column[Instant]("created_at")
  def updatedAt = column[Instant]("updated_at")

  def * = (id, brokerId, title, description, propertyType, price, location, area, createdAt, updatedAt) <> (Property.tupled, Property.unapply)
}
