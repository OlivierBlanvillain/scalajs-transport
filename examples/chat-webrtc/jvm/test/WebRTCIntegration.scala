package test

import play.api.test._

class WebRTCIntegration extends BrowserSpecification {
  "Two browsers" should {
    "be able to chat via WebRTC Client" in new WithTwoBrowsers(Chrome, Chrome) {
    // "be able to chat via WebRTC" in new WithTwoBrowsers(Firefox, Firefox) {
    // "be able to chat via WebRTC" in new WithTwoBrowsers(Chrome, Firefox) {
      browser1 goTo "/"
      browser2 goTo "/"

      browser2 waitUntil (browser2.pageSource.contains("display: block;"))
      browser2.$("#msgtext").text("Sup?").submit()
    
      browser1 waitUntil browser1.pageSource.contains("Sup?")
      browser2 waitUntil browser2.pageSource.contains("Sup?")
    }
  }
}
