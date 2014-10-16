package test

import play.api.test.TestBrowser
import org.specs2.mutable.Specification
import org.openqa.selenium._
import org.openqa.selenium.firefox.FirefoxDriver
import org.openqa.selenium.htmlunit.HtmlUnitDriver
import org.openqa.selenium.remote.DesiredCapabilities
import org.openqa.selenium.phantomjs.{ PhantomJSDriverService, PhantomJSDriver }
import java.nio.file.{ FileSystems, Files }

// Copied from https://github.com/buster84/playTutorialWithCucumber
trait BrowserSpecification extends Specification {

  lazy val PHANTOM = {
    val sCaps = new DesiredCapabilities()
    val phantomjsPath = {
      if(null != System.getenv("PHANTOMJS_PATH")){
        System.getenv("PHANTOMJS_PATH")
      } else {
        System.getProperty("PHANTOMJS_PATH")
      }
    }

    sCaps.setJavascriptEnabled(true);
    sCaps.setCapability("takesScreenshot", true);
    sCaps.setCapability(PhantomJSDriverService.PHANTOMJS_EXECUTABLE_PATH_PROPERTY, phantomjsPath)

    sCaps.setCapability(PhantomJSDriverService.PHANTOMJS_CLI_ARGS, Array(
      "--web-security=false",
      "--ssl-protocol=any",
      "--ignore-ssl-errors=true"
    ))

    val driver = new PhantomJSDriver(sCaps)
    driver.manage().timeouts().implicitlyWait(5L, java.util.concurrent.TimeUnit.SECONDS)
    driver.manage().timeouts().pageLoadTimeout(10L, java.util.concurrent.TimeUnit.SECONDS)
    driver.manage().timeouts().setScriptTimeout(10L, java.util.concurrent.TimeUnit.SECONDS)
    TestBrowser(driver, None).webDriver
  }
  
}
