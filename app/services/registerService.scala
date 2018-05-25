package services

import javax.inject.Inject
import anorm._
import com.google.inject.ImplementedBy
import models.{Address, Register}
import play.api.data.Form
import play.api.data.Forms._
import play.api.db.Database


class RealRegisterService @Inject()(db:Database) extends registerService {

  implicit val addressParser:RowParser[Address] = Macro.namedParser[Address]
  val parser:RowParser[Register] = Macro.namedParser[Register]


 override def registerForm:Form[Register] = Form(
    mapping(
      "email" -> email,
      "guid" -> ignored[Option[String]](None),
      "password" -> nonEmptyText,
      "name" -> nonEmptyText,
      "surname" -> nonEmptyText,
      "DOB" -> date,
      "gender" -> nonEmptyText,
      "tel" -> number,
      "addressID" -> ignored(0L),
      "image" -> ignored[Option[String]](None),
      "address"-> mapping(
        "addressID" -> ignored(0L),
        "houseNo" -> optional(number),
        "line1" -> nonEmptyText,
        "line2" -> optional(text),
        "postcode"-> nonEmptyText,
        "city" -> nonEmptyText,
        "county" -> nonEmptyText
      )(Address.apply)(Address.unapply)
    )(Register.apply)(Register.unapply)
  )

  override def registerUser(register: Register) = {
    db.withTransaction{ implicit connection =>
      SQL("INSERT into address (houseNo, line1, line2, postcode, city, county) " +
        "VALUES ({houseNo}, {line1}, {line2}, {postcode}, {city}, {county})").on(
        'houseNo -> register.address.houseNo,
        'line1 -> register.address.line1,
        'line2 -> register.address.line2,
        'postcode -> register.address.postcode,
        'city -> register.address.city,
        'county -> register.address.county).executeInsert()

      SQL("INSERT into user (email, password, name, surname, DOB, addressID, gender, tel)" +
        "VALUES ({email}, {password}, {name}, {surname}, {DOB}, {addressID}, {gender}, {tel})").on(
        'email -> register.email,
        'password -> register.password,
        'name -> register.name,
        'surname -> register.surname,
        'DOB -> register.DOB,
        'addressID -> register.addressID,
        'gender -> register.gender,
        'tel -> register.tel).executeInsert()
    }
  }

  override def uploadImage(url:String, email:String) = {
    db.withConnection{ implicit connection =>
      SQL("UPDATE user SET image = {image} WHERE email = {email}").on(
        'image -> url,
        'email -> email
      ).executeUpdate()
    }
  }

  override def findByEmail(email: String): Option[Register] = {
    db.withConnection { implicit connection =>
      SQL("SELECT * FROM user u left join address a ON a.addressID = u.addressID WHERE email = {email}").on('email -> email).as(parser.singleOpt)
    }
  }
}

@ImplementedBy(classOf[RealRegisterService])
trait registerService {
  def registerForm:Form[Register]
  def registerUser(register:Register)
  def findByEmail(email:String):Option[Register]
  def uploadImage(url:String, email:String)
}
