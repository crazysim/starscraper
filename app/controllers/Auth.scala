package controllers

import play.api.mvc._
import play.api.libs.concurrent.{Redeemed, Thrown}
import play.api.libs.openid.OpenID
import play.api.libs.concurrent.Redeemed
import play.api.mvc.AsyncResult
import scala.Some
import play.api.libs.concurrent.Thrown


object Auth extends Controller {

  def login = Action {
    implicit r =>
      val openid = "https://www.google.com/accounts/o8/id"
      AsyncResult(OpenID.redirectURL(openid,
        routes.Auth.openIDCallBack().absoluteURL(),
        Seq("email" -> "http://schema.openid.net/contact/email")
      )
        .extend(_.value match {
        case Redeemed(url) => Redirect(url)
        case Thrown(t) => Redirect(routes.Auth.login())
      }))
  }

  def logged_out = TODO

  def openIDCallBack = Action {
    implicit request =>
      AsyncResult(
        OpenID.verifiedId.extend(_.value match {
          case Redeemed(info) => {
            info.attributes.get("email") match {
              case None => Redirect(routes.Auth.login())
              case Some(email) => Redirect(routes.Application.index()).withSession(Security.username -> email)
            }
          }
          case Thrown(t) => {
            // Here you should look at the error, and give feedback to the user
            Redirect(routes.Auth.login())
          }
        })
      )
  }

  def logout = Action {
    Redirect(routes.Auth.login()).withNewSession
  }


}

trait Secured {

  def username(request: RequestHeader) = request.session.get(Security.username)

  def onUnauthorized(request: RequestHeader) = Results.Redirect(routes.Auth.login())

  def withAuth(f: => String => Request[AnyContent] => Result) = {
    Security.Authenticated(username, onUnauthorized) { user =>
      Action(request => f(user)(request))
    }
  }
}