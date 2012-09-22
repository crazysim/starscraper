package controllers

import play.api._
import libs.concurrent.Akka
import play.api.mvc._
import play.api.data.Forms._
import play.api.data._
import play.api.data.validation.Constraints._
import com.typesafe.plugin.{MailerPlugin, use}


import play.api.Play.current
import models.{UnAuthorizedTicket, NotFoundTicket, FoundTicket, Ticket}

import play.api.cache.Cache


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
            case "Email" => Redirect(routes.Application.email_ticket(value._1))
            case _ => Redirect(routes.Application.ticket(value._1))
          }
        }
      )
  }

  def email_ticket(id: Int) = withAuth {
    username => implicit request =>
      val promiseOfSource = Akka.future {
        pull_ticket(id)
      }
      AsyncResult {
        promiseOfSource.map(present_ticket(_, true, username))
      }
      Redirect(routes.Application.ticket(id))
  }

  def ticket(id: Int, email: Boolean = false) = withAuth {
    username => implicit request =>
      val promiseOfSource = Akka.future {
        pull_ticket(id)
      }
      AsyncResult {
        promiseOfSource.map(present_ticket(_, false, username))
      }
  }

  def pull_ticket(id: Int) = {
    val HS_username = current.configuration.getString("helpstar.username").getOrElse("No Username")
    val password = current.configuration.getString("helpstar.password").getOrElse("No Password")
    Cache.getOrElse("ticket." + id, 30)(
      models.HelpSTAR.getTicket(id, HS_username, password)
    )
  }

  def test_ticket(id: Int) = Action {
    val promiseOfSource = Akka.future {
      models.HelpSTAR.getSampleTicket(id)
    }
    AsyncResult {
      promiseOfSource.map(present_ticket(_))
    }
  }

  def present_ticket(s: Ticket, email: Boolean = false, email_address:String = "") = {
    s match {
      case f: FoundTicket => {
        if (email) {
          val mail = use[MailerPlugin].email
          mail.setSubject("StarScraper - " + f.number + ": " + f.title)
          mail.addRecipient(email_address)
          mail.addFrom("StarScraper <crazysim+starscraper@gmail.com>")
          mail.sendHtml(views.html.main("Report")(views.html.ticket.ticket(f)).toString())

          Ok((views.html.ticket.web_ticket(searchForm, f, "Emailed")))
        } else {
          Ok((views.html.ticket.web_ticket(searchForm, f)))
        }
      }
      case n: NotFoundTicket => Ok(views.html.ticket.web_ticket(searchForm, n, "Not Found"))
      case u: UnAuthorizedTicket => Ok(views.html.ticket.web_ticket(searchForm, u, "Unauthorized"))
      case _ => BadRequest
    }
  }

}