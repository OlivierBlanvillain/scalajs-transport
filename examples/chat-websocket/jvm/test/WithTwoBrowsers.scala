package test

import play.api.test._
import org.specs2.mutable.Around
import org.specs2.specification.Scope
import org.openqa.selenium.WebDriver
import org.specs2.execute.{ AsResult, Result }

/** Used to run specs within the context of a running server, and using two web browsers. */
abstract class WithTwoBrowsers[WEBDRIVER <: WebDriver](
        val webDriver1: WebDriver = WebDriverFactory(Helpers.HTMLUNIT),
        val webDriver2: WebDriver = WebDriverFactory(Helpers.HTMLUNIT),
        val app: FakeApplication = FakeApplication(),
        val port: Int = Helpers.testServerPort) extends Around with Scope {
  
  implicit def implicitApp = app
  implicit def implicitPort: Port = port

  lazy val browser1: TestBrowser = TestBrowser(webDriver1, Some("http://localhost:" + port))
  lazy val browser2: TestBrowser = TestBrowser(webDriver2, Some("http://localhost:" + port))

  override def around[T: AsResult](t: => T): Result = {
    try {
      Helpers.running(TestServer(port, app))(AsResult(t))
    } finally {
      browser1.quit()
      browser2.quit()
    }
  }
}
