package test

import play.api.test.TestBrowser
import org.specs2.mutable.Specification
import org.openqa.selenium._
import org.openqa.selenium.htmlunit.HtmlUnitDriver
import org.openqa.selenium.remote.DesiredCapabilities
import org.openqa.selenium.phantomjs.{ PhantomJSDriverService, PhantomJSDriver }
import org.openqa.selenium.firefox.{ FirefoxDriver, FirefoxBinary, FirefoxProfile }
import org.openqa.selenium.chrome.{ ChromeDriverService, ChromeDriver }
import collection.JavaConversions._
import java.io.File

/** Requires Xvfb, google-chrome, firefox, phantomjs */
trait BrowserSpecification extends Specification {
  private val chromedriverPath = "/opt/google/chromedriver"
  private val phantomjsPath = System.getenv("PHANTOMJS_PATH")
  private val display = ":1"
  
  def Firefox: WebDriver = withTimeouts(new FirefoxDriver())
  
  def FirefoxHeadless: WebDriver = {
    val firefox = new FirefoxBinary()
    firefox.setEnvironmentProperty("DISPLAY", display)
    withTimeouts(new FirefoxDriver(firefox, new FirefoxProfile()))
  }
  
  def Chrome: WebDriver = {
    System.setProperty("webdriver.chrome.driver", chromedriverPath)
    withTimeouts(new ChromeDriver())
  }

  def ChromeHeadless: WebDriver = {
    val service: ChromeDriverService = new ChromeDriverService.Builder()
      .usingAnyFreePort()
      .usingDriverExecutable(new File(chromedriverPath))
      .withEnvironment(mapAsJavaMap(Map("DISPLAY" -> display)))
      .build()
    withTimeouts(new ChromeDriver(service))
  }

  def Phantom: WebDriver = {
    // Inspired from https://github.com/buster84/playTutorialWithCucumber
    val caps = new DesiredCapabilities()
    caps.setJavascriptEnabled(true)
    caps.setCapability(PhantomJSDriverService.PHANTOMJS_EXECUTABLE_PATH_PROPERTY, phantomjsPath)
    caps.setCapability(PhantomJSDriverService.PHANTOMJS_CLI_ARGS, Array(
      "--web-security=false",
      "--ssl-protocol=any",
      "--ignore-ssl-errors=true"
    ))
    withTimeouts(new PhantomJSDriver(caps))
  }
  
  private def withTimeouts(driver: WebDriver): WebDriver = {
    import java.util.concurrent.TimeUnit.SECONDS
    driver.manage().timeouts().implicitlyWait(5L, SECONDS)
    driver.manage().timeouts().pageLoadTimeout(10L, SECONDS)
    driver.manage().timeouts().setScriptTimeout(10L, SECONDS)
    driver
  }
}
