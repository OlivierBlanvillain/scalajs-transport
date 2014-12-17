package test

import org.specs2.mutable._

import play.api.test._
import play.api.test.Helpers._

class Integration extends BrowserSpecification {

  "RPC helper" should {

    "Handle concurrent requests" in new WithBrowser(Phantom) {
      browser goTo "/"
      browser waitUntil browser.pageSource.contains("README")
      browser.pageSource must contain("42")
    }

  }
}
