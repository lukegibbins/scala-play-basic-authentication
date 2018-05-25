package models

case class Address(addressID:Long, houseNo:Option[Int], line1:String, line2:Option[String], postcode: String, city:String, county:String)