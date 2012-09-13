package models

import org.openqa.selenium.htmlunit.HtmlUnitDriver
import play.api.Play._

case class Request(reference_number: Int, title: String, transactions: List[Transaction], properties: List[Property], user_defined_fields: List[UserDefinedField])

case class Transaction(who: String, dept: String, time: String, memos: List[Memo])

case class Property(name:String, value: String)

case class Memo(kind: String, content: String)

case class UserDefinedField(name: String, value: String)

object HelpSTAR {
  def getTicket(id: String = "5432"): String = {
    val driver = new HtmlUnitDriver()
    driver.setJavascriptEnabled(true)
    driver.get("http://resnetservice.housing.ucsb.edu/")
    val username = current.configuration.getString("helpstar.username").getOrElse("None")
    val password = current.configuration.getString("helpstar.password").getOrElse("None")
    driver.findElementByName("txtUserName").sendKeys(username)
    driver.findElementByName("txtPassword").sendKeys(password)
    driver.findElementByName("btnLogin").click()
    driver.get("http://resnetservice.housing.ucsb.edu/hsPages/RB_RequestTemplate.aspx?requestId="+ id + "&TabTobeLoaded=tabTransactions&LoadPartially=0&Preview=1")
    val transaction_src = driver.getPageSource
    driver.get("http://resnetservice.housing.ucsb.edu/hsPages/RB_RequestTemplate.aspx?requestId=" + id + "&ActiveTabIndex=0&TabTobeLoaded=tabTransactions&OrderBy=undefined&MemoId=undefined&Preview=0")
    val request_src = driver.getPageSource
    driver.get("http://resnetservice.housing.ucsb.edu/hsPages/RB_UDFTemplate.aspx?ObjectId=" + id + "&ActiveTabIndex=0&TabTobeLoaded=tabUDFs ")
    val udf_src = driver.getPageSource
    driver.close()
    request_src + udf_src
  }
}
