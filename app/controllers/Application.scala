package controllers

import play.api._
import libs.concurrent.Akka
import play.api.mvc._

import play.api.Play.current
import models.FoundRequest

object Application extends Controller {

  def index = TODO

  def ticket(id: Int) = Action {
    val promiseOfSource = Akka.future {
      val username = current.configuration.getString("helpstar.username").getOrElse("No Username")
      val password = current.configuration.getString("helpstar.password").getOrElse("No Username")
      models.HelpSTAR.getRequest(id, username, password)
    }
    AsyncResult {
      promiseOfSource.map(s =>
        s match {
          case f: FoundRequest => Ok(views.html.ticket.index(f))
          case _ => BadRequest
        }
      )
    }
  }

  def test_ticket(id: Int) = Action {
    val promiseOfSource = Akka.future {
      models.HelpSTAR.getSampleRequest(id)
    }
    AsyncResult {
      promiseOfSource.map(s =>
        s match {
          case f: FoundRequest => Ok(views.html.ticket.index(f))
          case _ => BadRequest
        }
      )
    }
  }

}