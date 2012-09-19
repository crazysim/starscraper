package models

import java.io.{ByteArrayInputStream, InputStreamReader, InputStream}
import xml.{Node, NodeSeq}
import collection.immutable.{Seq, ListMap}
import com.gargoylesoftware.htmlunit.WebClient
import com.gargoylesoftware.htmlunit.html._
import scala.Some


case class TicketHTML(transactions_src: Node, details_src: Node, udf_src: Node)

abstract class Ticket()

case class FoundTicket(transactions: Seq[Transaction], properties: ListMap[String, String], user_defined_fields: ListMap[String, String]) extends Ticket{
  val number = properties.getOrElse("Number", "No Number?")
  val title = properties.getOrElse("Title", "No Title?")
}

case class NotFoundTicket(number: Int) extends Ticket
case class UnAuthorizedTicket(number: Int) extends Ticket

case class Transaction(who: String, department: String, time: String, memos: List[Memo])

case class Memo(kind: String, content: String)


object HelpSTAR {
  final val nb_space = Character.toString(160.asInstanceOf[Char])

  def getTicketHTML(id: Int, username: String, password: String): TicketHTML = {
    val client = new WebClient()
    client.setJavaScriptEnabled(true)
    client.setThrowExceptionOnScriptError(false)
    val login_page: HtmlPage = client.getPage("http://resnetservice.housing.ucsb.edu")
    val form: HtmlForm = login_page.getFormByName("frmLogin")
    val username_input: HtmlTextInput = form.getInputByName("txtUserName")
    val password_input: HtmlPasswordInput = form.getInputByName("txtPassword")
    username_input.setValueAttribute(username)
    password_input.setValueAttribute(password)
    val submit_input: HtmlSubmitInput = form.getInputByName("btnLogin")
    client.setJavaScriptEnabled(false)
    submit_input.click()
    val transactions_page: HtmlPage = client.getPage("http://resnetservice.housing.ucsb.edu/hsPages/RB_RequestTemplate.aspx?requestId=" + id + "&TabTobeLoaded=tabTransactions&LoadPartially=0&Preview=1")
    val transactions_src = transactions_page.getWebResponse.getContentAsString

    val details_page: HtmlPage = client.getPage("http://resnetservice.housing.ucsb.edu/hsPages/RB_RequestTemplate.aspx?requestId=" + id + "&TabTobeLoaded=tabRequestProperties")
    val details_src = details_page.getWebResponse.getContentAsString

    val udf_page: HtmlPage = client.getPage("http://resnetservice.housing.ucsb.edu/hsPages/RB_UDFTemplate.aspx?ObjectId=" + id + "&ActiveTabIndex=0&TabTobeLoaded=tabUDFs")
    val udf_src = udf_page.getWebResponse.getContentAsString

    client.setJavaScriptEnabled(false)
    client.closeAllWindows()

    implicit def string2Node(str: String): Node = {
      readDirtyHTMLInputSteam(new ByteArrayInputStream(str.getBytes("UTF-8")))
    }

    TicketHTML(transactions_src, details_src, udf_src)
  }

  def getTicket(id: Int, username: String, password: String): Ticket = {
    val req_html = getTicketHTML(id, username, password)
    req_html.details_src.text match {
      case "" => NotFoundTicket(id)
      case "You are not authorized to access this request." => UnAuthorizedTicket(id)
      case _ => {
        val transactions = parseTransactions(req_html.transactions_src)
        val details = parseDetails(req_html.details_src)
        val udf = parseUDF(req_html.udf_src)
        FoundTicket(transactions, details, udf)
      }
    }

  }

  def getSampleTicket(id: Int): Ticket = {
    val req_html = TicketHTML(
      get_ticket_HTML(id, "transactions.html"),
      get_ticket_HTML(id, "details.html"),
      get_ticket_HTML(id, "udf.html")
    )
    val transactions = parseTransactions(req_html.transactions_src)
    val details = parseDetails(req_html.details_src)
    val udf = parseUDF(req_html.udf_src)
    FoundTicket(transactions, details, udf)
  }

  def get_res_HTML(res: String): Node = {
    val is = getClass.getResourceAsStream(res)
    HelpSTAR.readDirtyHTMLInputSteam(is)
  }

  def get_ticket_HTML(id: Int, filename: String) = {
    get_res_HTML("/tickets/" + id + "/" + filename)
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
    val fields = tr_fields.map(n => {
      val tds = (n \ "td")
      val field_name = tds(0).text.trim.dropRight(1)
      val field_value = tds(1).child(0) match {
        case inp@ <input/> => {
          inp \ "@value"
        }.text
        case sel@ <select>{_*}</select> => {
          sel.child.filter(n => (n \ "@selected").nonEmpty)(0).text
        }
        case _ => "Unknown"
      }
      (field_name, field_value)
    })

    ListMap[String, String]() ++ fields
  }

}
