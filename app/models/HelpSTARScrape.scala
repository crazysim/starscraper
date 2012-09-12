package models

import org.openqa.selenium.htmlunit.HtmlUnitDriver
import play.api.Play._

/**
 * Created with IntelliJ IDEA.
 * User: nelson
 * Date: 9/11/12
 * Time: 10:02 PM
 * To change this template use File | Settings | File Templates.
 */
object HelpSTARScrape {
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
