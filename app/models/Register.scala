package models

import java.util.Date


case class Register (email:String, guid:Option[String], password:String, name:String, surname:String,
                     DOB: Date, gender:String, tel:Int, addressID:Long, image:Option[String], address: Address)


