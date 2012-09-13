import java.io.InputStreamReader
import org.specs2.mutable._

import play.api.test._
import play.api.test.Helpers._
import models.HelpSTAR._

import scala.xml._

class HelpSTARTest extends Specification {

  "File Parsing" should {
    "parse properties" in {
      "Hello world" must have size(11)
    }
//    "parse transactions" in {
//      "Hello world" must startWith("Hello")
//    }
//    "parse UDFs" in {
//      "Hello world" must endWith("world")
//    }
  }
}