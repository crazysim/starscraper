package controllers

import play.api._
import libs.concurrent.Akka
import play.api.mvc._

import play.api.Play.current
import models.FoundTicket
import models.Ticket

object Application extends Controller {

  def index = TODO

  def ticket(id: Int) = Action {
    val promiseOfSource = Akka.future {
      val username = current.configuration.getString("helpstar.username").getOrElse("No Username")
      val password = current.configuration.getString("helpstar.password").getOrElse("No Username")
      models.HelpSTAR.getTicket(id, username, password)
    }
    AsyncResult {
      promiseOfSource.map(r => present_ticket(r))
    }
  }

  def test_ticket(id: Int) = Action {
    val promiseOfSource = Akka.future {
      models.HelpSTAR.getSampleTicket(id)
    }
    AsyncResult {
      promiseOfSource.map(r => present_ticket(r))
    }
  }

  def present_ticket(s: Ticket) = {
    s match {
      case f: FoundTicket => Ok(views.html.ticket.found_ticket(f))
      case _ => BadRequest
    }
  }

}