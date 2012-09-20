package controllers

import play.api._
import libs.concurrent.Akka
import play.api.mvc._
import play.api.data.Forms._
import play.api.data._
import play.api.data.validation.Constraints._

import play.api.Play.current
import models.{UnAuthorizedTicket, NotFoundTicket, FoundTicket, Ticket}

object Application extends Controller with Secured {

  val searchForm = Form(
    tuple(
      "id" -> number.verifying(min(0)),
      "lookup" -> text
    )
  )

  def index = withAuth {
    username => implicit request =>
      Ok(views.html.index(searchForm, username))
  }

  def get_ticket = withAuth {
    username => implicit r =>
      searchForm.bindFromRequest().fold(
        searchForm => BadRequest(views.html.index(searchForm, username)),
        value => {
          value._2 match {
            case "Email" => Redirect(routes.Application.ticket(value._1))
            case _ => Redirect(routes.Application.ticket(value._1))
          }
        }
      )
  }

  def ticket(id: Int) = withAuth {
    username => implicit request =>
      val promiseOfSource = Akka.future {
        val HS_username = current.configuration.getString("helpstar.username").getOrElse("No Username")
        val password = current.configuration.getString("helpstar.password").getOrElse("No Password")
        models.HelpSTAR.getTicket(id, HS_username, password)
      }
      AsyncResult {
        promiseOfSource.map(present_ticket(_))
      }
  }

  def test_ticket(id: Int) = Action {
    val promiseOfSource = Akka.future {
      models.HelpSTAR.getSampleTicket(id)
    }
    AsyncResult {
      promiseOfSource.map(present_ticket(_))
    }
  }

  def present_ticket(s: Ticket) = {
    s match {
      case f: FoundTicket => Ok(views.html.ticket.found_ticket(searchForm, f))
      case n: NotFoundTicket => Ok(views.html.ticket.not_found_ticket(searchForm, n))
      case u: UnAuthorizedTicket => Ok(views.html.ticket.unauthorized_ticket(searchForm, u))
      case _ => BadRequest
    }
  }

}