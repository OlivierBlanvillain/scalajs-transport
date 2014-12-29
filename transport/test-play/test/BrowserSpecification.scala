package test

import collection.JavaConversions._
import org.openqa.selenium._
import org.openqa.selenium.chrome.{ ChromeDriverService, ChromeDriver }
import org.openqa.selenium.firefox.{ FirefoxDriver, FirefoxBinary, FirefoxProfile }
import org.openqa.selenium.htmlunit.HtmlUnitDriver
import org.openqa.selenium.phantomjs.{ PhantomJSDriverService, PhantomJSDriver }
import org.openqa.selenium.remote.DesiredCapabilities
import org.openqa.selenium.WebDriver
import org.specs2.mutable.Specification

/** Helper trait defining commonly used browsers in both GUI and Headless configurations.
 *
 *  To use all configurations you need to do the following:
 *    - Install Xvfb, google-chrome, firefox and phantomjs
 *    - Dowload [[http://chromedriver.storage.googleapis.com/index.html chromedriver]] in `/opt/google/chromedriver`
 *    - Set `PHANTOMJS_PATH `in your environment
 *    - Start Xvfb using `Xvfb :1 -screen 0 1024x768x16` */
trait BrowserSpecification extends Specification {
  private val chromedriverPath = "/opt/google/chromedriver"
  private val phantomjsPath = System.getenv("PHANTOMJS_PATH")
  private val display = ":1"
  
  def HtmlUnit: WebDriver = {
    val driver = new HtmlUnitDriver()
    driver.setJavascriptEnabled(true)
    driver
  }
  
  def Firefox: WebDriver = new FirefoxDriver()
  
  def FirefoxHeadless: WebDriver = {
    val firefox = new FirefoxBinary()
    firefox.setEnvironmentProperty("DISPLAY", display)
    new FirefoxDriver(firefox, new FirefoxProfile())
  }
  
  def Chrome: WebDriver = {
    System.setProperty("webdriver.chrome.driver", chromedriverPath)
    new ChromeDriver()
  }

  def ChromeHeadless: WebDriver = {
    val service: ChromeDriverService = new ChromeDriverService.Builder()
      .usingAnyFreePort()
      .usingDriverExecutable(new java.io.File(chromedriverPath))
      .withEnvironment(mapAsJavaMap(Map("DISPLAY" -> display)))
      .build()
    new ChromeDriver(service)
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
    new PhantomJSDriver(caps)
  }
  
  // private def withTimeouts(driver: WebDriver): WebDriver = {
  //   import java.util.concurrent.TimeUnit.SECONDS
  //   driver.manage().timeouts().implicitlyWait(5L, SECONDS)
  //   driver.manage().timeouts().pageLoadTimeout(10L, SECONDS)
  //   driver.manage().timeouts().setScriptTimeout(10L, SECONDS)
  //   driver
  // }
}
