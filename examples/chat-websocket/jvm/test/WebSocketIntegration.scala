package test

import play.api.test._

class WebSocketIntegration extends BrowserSpecification {
  "Two browsers" should {
    "be able to chat via WebSocket" in new WithTwoBrowsers(Phantom, Phantom) {
      browser1 goTo "/"
      browser2 goTo "/"
      
      browser2 waitUntil browser2.pageSource.contains("display: block;")
      browser2.$("#msgtext").text("Sup?").submit()
      
      browser1 waitUntil browser1.pageSource.contains("Sup?")
    }
  }
}
