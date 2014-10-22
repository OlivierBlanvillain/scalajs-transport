package test

import org.specs2.mutable._

import play.api.test._
import play.api.test.Helpers._
import java.util.concurrent.TimeUnit

class Integration extends BrowserSpecification {

  "Two browsers" should {

    "be able to chat" in new WithTwoBrowsers(Chrome, Chrome) {
      browser1 goTo "/"
      browser2 goTo "/"
      browser2.waitUntil(100, TimeUnit.SECONDS)(browser2.pageSource.contains("display: block;"))
      browser2.$("#msgtext").text("Sup?").submit()
      browser1 waitUntil browser1.pageSource.contains("Sup?")
    }

  }
}
