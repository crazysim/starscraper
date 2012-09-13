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
      val is = getClass.getResourceAsStream("details.html")
      val html = HelpSTAR.readDirtyHTMLInputSteam(is)
      val properties = HelpSTAR.parseDetails(html)
      (properties must not).beEmpty
    }
//    "parse transactions" in {
//      "Hello world" must startWith("Hello")
//    }
//    "parse UDFs" in {
//      "Hello world" must endWith("world")
//    }
  }
}