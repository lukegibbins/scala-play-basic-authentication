package controllers

import java.io.File
import java.nio.file.Paths
import javax.inject.Inject
import play.api.db.Database
import play.api.mvc._
import services._
import scala.concurrent.ExecutionException

class AuthController @Inject()(db: Database, ls: userLoginService, rs: registerService, rp: resetPasswordService, e: emailService) extends
  InjectedController with Secured with play.api.i18n.I18nSupport {

  def login = Action { implicit request =>
    Ok(views.html.login(ls.userLoginForm))
  }

  def authenticate = Action { implicit request =>
    ls.userLoginForm.bindFromRequest.fold(
      formWithErrors => BadRequest(views.html.login(formWithErrors)),
      user => Redirect(routes.ContactController.index).withSession("email" -> user._1)
    )
  }

  def logout = Action {
    Redirect(routes.AuthController.login).withNewSession.flashing(
      "logout" -> "You've been logged out"
    )
  }


  def profile = withAuth { username =>
    implicit request =>
      val viewProfile = rs.findByEmail(username)
      Ok(views.html.profile(viewProfile))
  }


  def uploadProfileImage = Action(parse.multipartFormData) { implicit request =>
    request.body.file("picture").map { picture =>
      val email = request.session.get("email")
      val filename = Paths.get(picture.filename).getFileName
      picture.ref.moveTo(new File(s"public/resources/$filename"), replace = false)
      rs.uploadImage(s"$filename", email.get)
      val viewProfile = rs.findByEmail(email.get)
      Ok(views.html.profile(viewProfile))
    }.getOrElse {
      Redirect(routes.AuthController.profile).flashing(
        "errorOnFile" -> "Missing file")
    }
  }


  def editPassword = withAuth { username =>
    implicit request =>
      val viewProfile: Option[String] = rs.findByEmail(username).map(u => u.email)
      Ok(views.html.changePassword(rp.resetPasswordForm.fill(Tuple4(viewProfile.get, "", "", ""))))
  }


  def changePassword = Action { implicit request =>
    rp.resetPasswordForm.bindFromRequest.fold(
      hasErrors => BadRequest(views.html.changePassword(hasErrors)),
      passwordDetails => {
        if (passwordDetails._3 == passwordDetails._4) {
          try {
            rp.authenticateReset(passwordDetails._1, passwordDetails._2, passwordDetails._3)
            Redirect(routes.AuthController.profile).flashing("updatedPassword" -> "Password updated successfully")
          }
          catch {
            case ex: Exception => Redirect(routes.AuthController.changePassword).flashing("passwordError" -> "Invalid password entered")
          }
        }
        else {
          Redirect(routes.AuthController.changePassword).flashing("passwordError" -> "Invalid password entered")
        }
      }
    )
  }

  def registerForm = Action { implicit request =>
    Ok(views.html.register(rs.registerForm))
  }

  def registerUser = Action { implicit request =>
    rs.registerForm.bindFromRequest.fold(
      hasErrors => BadRequest(views.html.register(hasErrors)),
      success => {
        try {
          rs.registerUser(success)
          Redirect(routes.AuthController.login).flashing("register" -> "User successfully registered")
        }
        catch {
          case executionEx: ExecutionException => Redirect(routes.AuthController.registerForm).flashing("unsuccessful" -> executionEx.getMessage)
          case ex: Exception => Redirect(routes.AuthController.registerForm).flashing("unsuccessful" -> "Email address taken, please choose another")
        }
      })
  }

  def forgottenPassword = Action { implicit request =>
    Ok(views.html.forgottenPassword(e.forgottenPasswordForm))
  }

  def sendEmail = Action { implicit request =>
    e.forgottenPasswordForm.bindFromRequest.fold(
      hasErrors => BadRequest(views.html.forgottenPassword(hasErrors)),
      success => {
        val emailAddress = success.email
        try {
          e.sendEmail(success.email)
          Redirect(routes.AuthController.forgottenPassword).flashing(
            "email" -> s"Success! A temporary password has been successfully sent to the following account: '$emailAddress'. Please login to your email account to view your new password.")
        }
      }
    )
  }

  def newPasswordFormFromEmail = Action { implicit request =>
    Ok(views.html.setPasswordFromEmail(e.setNewPasswordFromEmailForm))
  }

  def setNewPasswordFromEmailForm = Action { implicit request =>
    e.setNewPasswordFromEmailForm.bindFromRequest.fold(
      hasErrors => BadRequest(views.html.setPasswordFromEmail(hasErrors)),
      passwordDetails => {
        if (passwordDetails._2 == passwordDetails._3) {
          try {
            e.updatePassword(passwordDetails._1, passwordDetails._3)
            Redirect(routes.AuthController.login).flashing("updatedPassword" -> "Password updated successfully").flashing("passwordUpdated" -> "Password succesfully updated.")
          }
          catch {
            case ex: Exception => Redirect(routes.AuthController.newPasswordFormFromEmail).flashing("passwordError" -> "Invalid password entered")
          }
        }
        else {
          Redirect(routes.AuthController.newPasswordFormFromEmail).flashing("passwordError" -> "Invalid password entered")
        }
      })
  }


}



