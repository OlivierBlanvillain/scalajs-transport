package test

import org.specs2.mutable._

import play.api.test._
import play.api.test.Helpers._

class Integration extends BrowserSpecification {

  "run in a browser" in new WithBrowser(PHANTOM) {
    browser goTo "/"
    browser waitUntil browser.pageSource.contains("build.sbt")
  }
  
}
