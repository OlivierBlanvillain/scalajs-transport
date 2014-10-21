package test

import org.specs2.mutable._

import play.api.test._
import play.api.test.Helpers._

class Integration extends BrowserSpecification {

  "Autowire helper" should {

    "Handle concurrent requests" in new WithBrowser(PHANTOM) {
      browser goTo "/"
      browser waitUntil browser.pageSource.contains("build.sbt")
      browser.pageSource must contain("42")
    }

  }
}
