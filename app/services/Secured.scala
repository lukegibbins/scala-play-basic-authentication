package services

import controllers.routes
import play.api.mvc._


trait Secured {

  def username(request: RequestHeader) = request.session.get("email")

  def unauthorized(request: RequestHeader) = Results.Redirect(routes.AuthController.login).flashing(
    "Forbidden" -> "Your are not authorised to access that page, please login")

  def withAuth(f: => String => Request[AnyContent] => Result) = {
    Security.Authenticated(username, unauthorized) { user =>
      Action(request => f(user)(request))
    }
  }
  def withAuthForm(f: => String => Request[AnyContent] => Result) = {
    Security.Authenticated(username, unauthorized) { user =>
      Action(request => f(user)(request))
    }
  }
}