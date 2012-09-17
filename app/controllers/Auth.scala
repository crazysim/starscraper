package controllers

import play.api.mvc.{Action, AsyncResult, Controller}
import play.api.libs.concurrent.{Redeemed, Thrown}
import play.api.libs.openid.OpenID


object Auth extends Controller {

  def login = Action { implicit r =>
    val openid = "https://www.google.com/accounts/o8/id"
    AsyncResult(OpenID.redirectURL(openid,
      routes.Auth.openIDCallBack().absoluteURL(),
      Seq("email" -> "http://schema.openid.net/contact/email")
    )
      .extend( _.value match {
      case Redeemed(url) => Redirect(url)
      case Thrown(t) => Redirect(routes.Auth.login())
    }))
  }

  def authenticate = TODO

  def openIDCallBack = Action { implicit request =>
    AsyncResult(
      OpenID.verifiedId.extend( _.value match {
        case Redeemed(info) => Ok(info.id + "\n" + info.attributes)
        case Thrown(t) => {
          // Here you should look at the error, and give feedback to the user
          Redirect(routes.Auth.login())
        }
      })
    )
  }

  def logout = TODO

}
