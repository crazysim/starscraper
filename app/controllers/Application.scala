package controllers

import play.api._
import libs.concurrent.Akka
import play.api.mvc._

import play.api.Play.current

import org.openqa.selenium._
import org.openqa.selenium.htmlunit._

import models.HelpSTAR.getTicket

object Application extends Controller {

  def index = Action {
    val promiseOfSource = Akka.future {
      val driver = new HtmlUnitDriver()
      driver.get("http://slickdeals.net/")
      driver.getPageSource
    }
    AsyncResult {
      promiseOfSource.map(s => Ok(s))
    }
  }

  def ticket(id: String) = Action {
    val promiseOfSource = Akka.future {
      getTicket(id)
    }

    AsyncResult {
      promiseOfSource.map(s => Ok(s))
    }
  }


}