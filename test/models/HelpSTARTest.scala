import java.io.InputStreamReader
import models.HelpSTAR
import org.specs2.mutable._

import play.api.test._
import play.api.test.Helpers._
import models.HelpSTAR._

import scala.xml._

class HelpSTARTest extends Specification {

  "Detail Parsing" should {
    "parse example details to not be empty" in {
      val properties = HelpSTAR.parseDetails(details_HTML)
      properties.size must_!= 0
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
}