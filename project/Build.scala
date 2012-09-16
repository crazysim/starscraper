import sbt._
import Keys._
import PlayProject._

object ApplicationBuild extends Build {

    val appName         = "starscraper"
    val appVersion      = "1.0-SNAPSHOT"

    val appDependencies = Seq(
      // Add your project dependencies here,
      "org.seleniumhq.selenium" % "selenium-java" % "2.24.1",
      "net.sourceforge.htmlunit" % "htmlunit" % "2.10",
      "org.ccil.cowan.tagsoup" % "tagsoup" % "1.2.1"
    )

    val main = PlayProject(appName, appVersion, appDependencies, mainLang = SCALA).settings(
      // Add your own project settings here      
    )

}
