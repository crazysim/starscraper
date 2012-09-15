package models

import org.openqa.selenium.htmlunit.HtmlUnitDriver
import play.api.Play._
import play.api.http
import java.io.{InputStreamReader, InputStream}
import xml.{Node, NodeSeq}
import collection.immutable.{Seq, ListMap}

case class Request(reference_number: Int, title: String, transactions: Seq[Transaction], properties: ListMap[String, String], user_defined_fields: ListMap[String, String])

case class Transaction(who: String, department: String, time: String, memos: List[Memo])

case class Memo(kind: String, content: String)


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
    driver.get("http://resnetservice.housing.ucsb.edu/hsPages/RB_RequestTemplate.aspx?requestId=" + id + "&TabTobeLoaded=tabTransactions&LoadPartially=0&Preview=1")
    val transactions_src = driver.getPageSource
    driver.get("http://resnetservice.housing.ucsb.edu/hsPages/RB_RequestTemplate.aspx?requestId=" + id + "&TabTobeLoaded=tabRequestProperties")
    val details_src = driver.getPageSource
    driver.get("http://resnetservice.housing.ucsb.edu/hsPages/RB_UDFTemplate.aspx?ObjectId=" + id + "&ActiveTabIndex=0&TabTobeLoaded=tabUDFs ")
    val udf_src = driver.getPageSource
    driver.close()
    transactions_src + details_src + udf_src
  }

  def readDirtyHTMLInputSteam(is: InputStream): Node = {
    val sax_parser = (new org.ccil.cowan.tagsoup.jaxp.SAXFactoryImpl).newSAXParser()
    val in = new org.xml.sax.InputSource(new InputStreamReader(is))
    val adapter = new scala.xml.parsing.NoBindingFactoryAdapter
    adapter.loadXML(in, sax_parser)
  }

  def parseDetails(in: Node): ListMap[String, String] = {
    // Get the number and title
    val number_and_title = ((in \\ "tr" head) \\ "b").map(_.text)
    val number = number_and_title(0)
    val title = number_and_title(1)
    // Get the rest
    val properties_tr =
      ((in \\ "table" \\ "tr")(2) \\ "tr").filter(n =>
        (n \ "td")(0).attribute("style") match {
          case Some(style) => style.text.contains("font-weight:bold;")
          case _ => false
        }
      )
    val properties_map = properties_tr.map(node => {
      val tds = node \\ "td"
      (tds(0).text.trim.dropRight(1),
        tds(1).text.replace(Character.toString(160.asInstanceOf[Char]), " ").trim)
    })

    ListMap[String, String](
      "Number" -> number,
      "Title" -> title
    ) ++ properties_map
  }

  def parseTransactions(in: Node): Seq[Transaction] = {
    val transactions_table = (in \ "body" \ "table" \ "tr").drop(1)
    transactions_table.map(parseTransaction(_))
  }

  def parseTransaction(in: Node): Transaction = {
    val transaction_table = (in \ "td" \ "div" \ "div")(0)
    val memo_header = ((transaction_table \ "table" \ "tr")(1) \ "td" \ "table" \ "tr" \ "td") \\ "a"
    //val user = memo_header(0).text
    //val department = memo_header(1).text
    Transaction("user","department", "No time", List[Memo]())
  }

  def parseMemo(in: Node): Memo = {
    Memo("n","n")
  }

  def parseUDF(in: Node): ListMap[String, String] = {
    ListMap[String, String]()
  }

}
