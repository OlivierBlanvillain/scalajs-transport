package test

import org.specs2.mutable._

import play.api.test._
import play.api.test.Helpers._

class Integration extends BrowserSpecification {

  "Two browsers" should {

    "be able to chat" in new WithTwoBrowsers(Phantom, Phantom) {
      browser1 goTo "/"
      browser2 goTo "/"
      browser2 waitUntil browser2.pageSource.contains("display: block;")
      browser2.$("#msgtext").text("Sup?").submit()
      browser1 waitUntil browser1.pageSource.contains("Sup?")
    }

  }
}
