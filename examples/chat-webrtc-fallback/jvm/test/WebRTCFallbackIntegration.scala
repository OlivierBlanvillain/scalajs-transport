package test

import play.api.test._

class WebRTCClientFallbackIntegration extends BrowserSpecification {
  "Two browsers" should {
    "be able to chat via WebRTCClientFallback" in new WithTwoBrowsers(Chrome, Phantom) {
      browser1 goTo "/"
      browser2 goTo "/"

      browser2 waitUntil (browser2.pageSource.contains("display: block;"))
      browser2.$("#msgtext").text("Sup?").submit()

      browser1 waitUntil browser1.pageSource.contains("Sup?")
      browser2 waitUntil browser2.pageSource.contains("Sup?")

      browser1.pageSource must contain("Supports WebRTC")
      browser2.pageSource must contain("Does not support WebRTC")
    }
  }
}
