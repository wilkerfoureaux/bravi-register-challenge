/**
 * I could create only a ajax request to test the API
 * Request URL: https://api.mlab.com/api/1/databases/userdetails/collections/newtable?apiKey=YEX0M2QMPd7JWJw_ipMB3a5gDddt4B_X
 * POST
 * formData = [{FirstName: "www", LastName: "www", Email: "ww@ww.www", Phone: "9999999991", Gender: "Male"}]
 * but I will create only a headless test with selenium webdriver
 *
 * I could create with capybara and cucumber too but I prefer katalon, nowdays
 *
 * Below, I created the script with selenium libraries to exemplify,
 * but using katalon default libraries would be better
 *
 * */

import org.json.JSONObject
import org.openqa.selenium.By as By
import org.openqa.selenium.By.ByClassName
import org.openqa.selenium.WebDriver as WebDriver
import org.openqa.selenium.WebElement as WebElement
import org.openqa.selenium.chrome.ChromeDriver as ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.WebDriverWait

import com.kms.katalon.core.util.KeywordUtil
import com.kms.katalon.core.webui.driver.DriverFactory as DriverFactory

/**
 *	SETUP
 * 
 * 	change the web driver path for yours
 * 	my version was
 * 	https://chromedriver.storage.googleapis.com/index.html?path=80.0.3987.106/
 * 
 * */
def CHROME_WEB_DRIVER_PATH = 'C:\\Katalon_Studio_Windows_64-6.2.2\\configuration\\resources\\drivers\\chromedriver_win32\\chromedriver.exe'
System.setProperty("webdriver.chrome.driver", CHROME_WEB_DRIVER_PATH)

/**
 * 	if run headless is wanted, set above opstion to true
 * */
def CHROME_HEADLESS = false

static ChromeOptions options(){
	return new ChromeOptions().addArguments("--headless")
}
WebDriver chrome = CHROME_HEADLESS ? new ChromeDriver(options()) : new ChromeDriver()
DriverFactory.changeWebDriver(chrome)


/**
 *	DATA DRIVEN
 */
def RANDOM_USER_API = 'https://randomuser.me/api/'

chrome.get(RANDOM_USER_API)

WebDriverWait wait = new WebDriverWait(chrome, 30);
wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(By.tagName('pre')))
WebElement pre = chrome.findElement(By.tagName('pre'))

JSONObject json = new JSONObject(pre.getText())

def results, userFirstName, userLastName, userEmail, userPhone, userGender

try {
	results = json.getJSONArray("results").getJSONObject(0)
	userFirstName = results.getJSONObject('name').getString('first')
	userLastName = results.getJSONObject('name').getString('last')
	userEmail = results.getString('email')
	userPhone = results.getString('phone').replaceAll("[-()\\s]", '').concat('0000000000').substring(0, 10)
	userGender = results.getString('gender')
} catch (Exception e) {
	KeywordUtil.markFailedAndStop("error setting test data")
	chrome.close()
	e.printStackTrace()
}

/**
 * 	TEST TARGET
 * */

def TARGET_URL = 'http://demo.automationtesting.in/Register.html'

chrome.navigate().to(TARGET_URL)

assert chrome.getTitle() == 'Register'

WebElement title = chrome.findElement(By.xpath('//h2'))

assert title.getText() == 'Register'

WebElement firstName = chrome.findElement(By.xpath('//input[@placeholder="First Name"]'))
firstName.sendKeys(userFirstName)

WebElement lastName = chrome.findElement(By.xpath('//input[@placeholder="Last Name"]'))
lastName.sendKeys(userLastName)

WebElement email = chrome.findElement(By.xpath('//input[@type="email"]'))
email.sendKeys(userEmail)

WebElement phone = chrome.findElement(By.xpath('//input[@type="tel"]'))
phone.sendKeys(userPhone)

String setGender(String text){
	
	// this is a hook and the form actually contains a typo in "FeMale" radio value
	// TODO -- FIX "FeMale" TYPO
	
	def result = text.toLowerCase().equals('male') ? 'Male' : 'FeMale'
	return '//input[@type="radio" and @value="'+ result +'"]'
}

WebElement gender = chrome.findElement(By.xpath(setGender(userGender)))
gender.click()

chrome.findElement(By.xpath('//button[@id="submitbtn"]')).submit()

wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(By.xpath('//div[@role="grid"]')))

/**
 * FINAL ASSERTIONS
 * */

assert chrome.getTitle() == 'Web Table'

assert chrome.findElement(By.xpath('//div[@role="grid"]')).isDisplayed()

/**
 * TEARDOWN
 * */

chrome.close()