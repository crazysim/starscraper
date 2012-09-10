package controllers

import play.api._
import play.api.mvc._

import org.openqa.selenium._
import org.openqa.selenium.htmlunit._

object Application extends Controller {
  
  def index = Action {
    val driver = new HtmlUnitDriver()
    driver.get("http://resnetservice.housing.ucsb.edu/")
    Ok(driver.getPageSource)
  }
  
}