package services

import javax.inject.Inject

import anorm.{Macro, RowParser, _}
import com.google.inject.ImplementedBy
import models.User
import play.api.data.Form
import play.api.data.Forms._
import play.api.db.Database

class RealUserLoginService @Inject() (db:Database) extends userLoginService {

  val parser: RowParser[User] = Macro.namedParser[User]


  override def userLoginForm = Form(
    tuple(
      "email" -> text,
      "password" -> text
    ) verifying("Invalid email or password", result => result match {
      case (email, password) => authenticate(email, password).isDefined
    })
  )


  override def authenticate(email: String, password: String): Option[User] = {
    db.withConnection { implicit connection =>
      SQL("SELECT email, password FROM user WHERE email = {email} AND password = {password}").on(
        'email -> email,
        'password -> password).as(parser.singleOpt)
    }
  }
}

@ImplementedBy(classOf[RealUserLoginService])
trait userLoginService {

  def userLoginForm:Form[(String,String)]
  def authenticate(email:String, password:String):Option[User]
}
