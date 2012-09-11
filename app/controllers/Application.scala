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
      getTicket(id)
    }

    AsyncResult {
      promiseOfSource.map(s => Ok(s))
    }
  }

  def getTicket(id: String = "5432"): String = {
    val driver = new HtmlUnitDriver()
    driver.setJavascriptEnabled(true)
    driver.get("http://resnetservice.housing.ucsb.edu/")
    val username = current.configuration.getString("helpstar.username").getOrElse("None")
    val password = current.configuration.getString("helpstar.password").getOrElse("None")
    driver.findElementByName("txtUserName").sendKeys(username)
    driver.findElementByName("txtPassword").sendKeys(password)
    driver.findElementByName("btnLogin").click()
    val landing_src = driver.getPageSource
    driver.get("http://resnetservice.housing.ucsb.edu/hsPages/RB_RequestTemplate.aspx?requestId=" + id + "&ActiveTabIndex=0&TabTobeLoaded=tabTransactions&OrderBy=undefined&MemoId=undefined&Preview=0")
    val request_src = driver.getPageSource
    driver.get("http://resnetservice.housing.ucsb.edu/hsPages/RB_UDFTemplate.aspx?ObjectId=" + id + "&ActiveTabIndex=0&TabTobeLoaded=tabUDFs ")
    val udf_src = driver.getPageSource
    driver.close()
    request_src + udf_src
  }
}