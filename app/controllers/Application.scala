package controllers

import play.api._
import libs.concurrent.Akka
import play.api.mvc._
import play.api.data.Forms._
import play.api.data._


import play.api.Play.current
import models.{NotFoundTicket, FoundTicket, Ticket}

object Application extends Controller {

  val searchForm = Form(
    "id" -> number
  )

  def index = Action {
    Ok(views.html.index(searchForm))
  }

  def get_ticket = Action { implicit r =>
    val id = searchForm.bindFromRequest.get
    Redirect(routes.Application.ticket(id))
  }

  def ticket(id: Int) = Action {
    val promiseOfSource = Akka.future {
      val username = current.configuration.getString("helpstar.username").getOrElse("No Username")
      val password = current.configuration.getString("helpstar.password").getOrElse("No Username")
      models.HelpSTAR.getTicket(id, username, password)
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
      case f: FoundTicket => Ok(views.html.ticket.found_ticket(f))
      case n: NotFoundTicket => Ok(views.html.ticket.not_found_ticket(n))
      case _ => BadRequest
    }
  }

}