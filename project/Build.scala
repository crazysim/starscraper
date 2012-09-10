import sbt._
import Keys._
import PlayProject._

object ApplicationBuild extends Build {

    val appName         = "starscraper"
    val appVersion      = "1.0-SNAPSHOT"

    val appDependencies = Seq(
      "org.seleniumhq.selenium" % "selenium-java" % "2.24.1"
      // Add your project dependencies here,
    )

    val main = PlayProject(appName, appVersion, appDependencies, mainLang = SCALA).settings(
      // Add your own project settings here      
    )

}
