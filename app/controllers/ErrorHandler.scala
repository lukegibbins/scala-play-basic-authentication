package controllers


import javax.inject.{Inject, Provider, Singleton}

import play.api._
import play.api.http.DefaultHttpErrorHandler
import play.api.mvc.Results._
import play.api.mvc.{RequestHeader, Result}
import play.api.routing.Router

import scala.concurrent.Future


@Singleton
class ErrorHandler @Inject()  (env: Environment,
                               config: Configuration,
                               sourceMapper: OptionalSourceMapper,
                               router: Provider[Router]
                              ) extends DefaultHttpErrorHandler(env, config, sourceMapper, router) {


  //403 - Forbidden
  override def onForbidden(request: RequestHeader, message: String): Future[Result] = {
    Future.successful(Forbidden(views.html.errors.forbidden()))
  }

  //404 - page not found error
  override def onNotFound(request: RequestHeader, message: String): Future[Result] =  {
    Future.successful(NotFound(views.html.errors.notFound(request)))
  }

  //500 - internal server error
  override def onProdServerError(request: RequestHeader, exception: UsefulException): Future[Result] = {
    Future.successful(InternalServerError(views.html.errors.internalServerError(request, exception)))
  }

}
