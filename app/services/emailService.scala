package services

import javax.inject.Inject
import models.EmailSend
import play.api.data.Form
import play.api.data.Forms._
import play.api.db.Database
import play.api.libs.mailer._
import utils.GUID
import anorm._


class emailService @Inject()(db: Database, rs: registerService, mailerClient: MailerClient) extends GUID {

  val setNewPasswordFromEmailForm = Form(
    tuple(
      "guid" -> nonEmptyText,
      "newPassword" -> nonEmptyText,
      "confirmPassword" -> nonEmptyText
    )
  )

  val forgottenPasswordForm = Form(
    mapping(
      "email" -> email
    )(EmailSend.apply)(EmailSend.unapply)
      verifying("Email address does not currently exist", result => result match {
      case emailExists => rs.findByEmail(emailExists.email).isDefined
    })
  )

  def sendEmail(emailAddress: String) = {
    val guid = generateGUID

    db.withTransaction { implicit connection =>
      SQL("UPDATE user SET guid = {guid} WHERE email = {email}").on(
        'guid -> guid,
        'email -> emailAddress
      ).executeUpdate

      val email: Email = Email(
        subject = "Your New Password",
        from = "Website Administrator <youremailgoeshere@address.com>",
        to = Seq(s"<$emailAddress>"),
        bodyHtml = Some
        ("<html>" +
          "<body>" +
          s"<h3>To the account owner of the following email address: $emailAddress.</h3>" +
          s"<p>Your unique key has been set to: <b>$guid</b>." +
          "<p>Using the link below, enter the unique key and confirm a new password to log back into your account with.</p>" +
          "Link: http://localhost:9000/newpassword" +
          "<br><br>" +
          "<p>Regards," +
          "<p>Website Admin" +
          "</body>" +
          "</html>")
      )
      mailerClient.send(email)
    }
  }

  def updatePassword(guid: String, confirmedPassword: String) = {
    db.withConnection { implicit connection =>
      SQL("UPDATE user SET password = {password} WHERE guid = {guid}").on(
        'password -> confirmedPassword,
        'guid -> guid
      ).executeUpdate
    }
  }

}


