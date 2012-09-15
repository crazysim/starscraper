import java.io.InputStreamReader
import models.HelpSTAR
import org.specs2.mutable._

import play.api.test._
import play.api.test.Helpers._
import models.HelpSTAR._

import scala.xml._

class HelpSTARTest extends Specification {

  "Detail Parsing" should {
    val properties = HelpSTAR.parseDetails(details_HTML)
    "parse example details to not be empty" in {
      properties.size must_!= 0
    }
    "parse example title is Cannot Register with Given Perm & PW" in {
      properties("Title") mustEqual ("Cannot Register with Given Perm & PW")
    }

    "parse example number to 5432" in {
      properties("Number") mustEqual ("5432")
    }

    "parse example to a status that has Closed" in {
      properties("Status") must contain("Closed")
    }

    //    "parse transactions" in {
    //      "Hello world" must startWith("Hello")
    //    }
    //    "parse UDFs" in {
    //      "Hello world" must endWith("world")
    //    }
  }

  def details_HTML = {
    val is = getClass.getResourceAsStream("details.html")
    HelpSTAR.readDirtyHTMLInputSteam(is)
  }

  "Transaction Parsing" should {
    val transactions = HelpSTAR.parseTransactions(transactions_HTML)
    "parse example transaction to not be empty" in {
      transactions.size must_!= 0
    }

    "parse example transaction's first user is Danny'" in {
      transactions(0).who mustEqual  "Danny Phillips"
    }

    "parse business rules transaction to Business Rule Service" in {
      transactions(3).who mustEqual "Business Rule Service"
    }

  }

  def transactions_HTML = {
    val is = getClass.getResourceAsStream("transactions.html")
    HelpSTAR.readDirtyHTMLInputSteam(is)
  }
}