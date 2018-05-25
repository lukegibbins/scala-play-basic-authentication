package services

import javax.inject.Inject
import play.api.data.Forms._
import anorm.{Macro, RowParser, SQL}
import com.google.inject.ImplementedBy
import models.User
import play.api.data.Form
import play.api.data.Forms.tuple
import play.api.db.Database

class realResetPasswordService @Inject()(db:Database) extends resetPasswordService {

  val parser: RowParser[User] = Macro.namedParser[User]

   override def resetPasswordForm = Form(
    tuple(
      "email" -> nonEmptyText,
      "password" -> nonEmptyText,
      "newPassword" -> nonEmptyText,
      "confirmPassword" -> nonEmptyText
    )
  )

   override def authenticateReset(email: String, password: String, newPassword:String) = {
    db.withTransaction { implicit connection =>
      val users = SQL("SELECT email, password FROM user WHERE email = {email} AND password = {password}").on(
        'email -> email,
        'password -> password).as(parser.single)
        SQL("UPDATE user SET password = {password} WHERE email = {email}").on(
          'email -> users.email,
          'password -> newPassword).executeUpdate
    }
  }
}


@ImplementedBy(classOf[realResetPasswordService])
trait resetPasswordService {
  def resetPasswordForm:Form[(String,String,String,String)]
  def authenticateReset(email: String, password: String, newPassword:String)
}