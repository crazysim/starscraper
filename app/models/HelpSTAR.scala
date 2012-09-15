package models

import org.openqa.selenium.htmlunit.HtmlUnitDriver
import play.api.Play._
import java.io.{InputStreamReader, InputStream}
import xml.{Node, NodeSeq}
import collection.immutable.{Seq, ListMap}

case class Request(reference_number: Int, title: String, transactions: Seq[Transaction], properties: ListMap[String, String], user_defined_fields: ListMap[String, String])

case class Transaction(who: String, department: String, time: String, memos: List[Memo])

case class Memo(kind: String, content: String)


object HelpSTAR {
  final val nb_space = Character.toString(160.asInstanceOf[Char])

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
        tds(1).text.replace(nb_space, " ").trim)
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
    val transaction_table = (in \ "td" \ "div" \ "div")

    val memos_tr = transaction_table(1) \ "table" \ "tr"
    val memos = (for (ns <- (memos_tr.grouped(2))) yield (
      parseMemo(ns)
      )).toList

    val memo_header = ((transaction_table(0) \ "table" \ "tr")(1) \ "td" \ "table" \ "tr" \ "td")
    val memo_header_entities = memo_header \\ "a"

    val time = memo_header(1).text.split("On:")(1).trim

    memo_header_entities.size match {
      case 0 => {
        val name = (memo_header \\ "b").text.
          replace(nb_space, " ").trim
        Transaction(name, "None", time, memos)
      }
      case 2 => {
        val user = memo_header_entities(0).text.trim
        val department = memo_header_entities(1).text.trim
        Transaction(user, department, time, memos)
      }
      case _ => {
        Transaction(memo_header.text.trim, "Unknown", time, memos)
      }
    }

  }

  def parseMemo(in: NodeSeq): Memo = {
    val title = ((in(0) \ "td") \ "b").text.trim
    val content = (in(1) \ "td").toString()
    Memo(title, content)
  }

  def parseUDF(in: Node): ListMap[String, String] = {
    val tr_fields = (in \\ "tr").filter(n => {
      val first_td = (n \ "td")(0)
      (first_td \ "@width").text.equals("25%") &&
      first_td.text.nonEmpty
    })
    tr_fields.map(n => {
      val field_name = (n \ "td")(0).text.trim.dropRight(1)

    })

    ListMap[String, String]()
  }

}
