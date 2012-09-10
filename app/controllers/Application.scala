package controllers

import play.api._
import libs.concurrent.Akka
import play.api.mvc._

import play.api.Play.current

import org.openqa.selenium._
import org.openqa.selenium.htmlunit._

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
      val driver = new HtmlUnitDriver()
      driver.get("http://resnetservice.housing.ucsb.edu/")
      val login_source = driver.getPageSource
      driver.close()
      login_source
    }

    AsyncResult {
      promiseOfSource.map(s => Ok(s))
    }
  }

}