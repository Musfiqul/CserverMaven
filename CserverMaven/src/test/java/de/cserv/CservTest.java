package de.cserv;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.annotations.*;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;



public class CservTest {
    public String baseUrl,testUser, testPasswords, searchNum, searchString;
    public String expectErgebnisse, expectString;

    // webdriver initialization
    WebDriver driver = new FirefoxDriver();
    WebDriverWait waitObj = new WebDriverWait(driver, 10);


    @BeforeClass   // Runs before all the test methods in this class
    @Parameters("baseUrl")
    public void nevigate(String baseUrl) throws InterruptedException {
        driver.get(baseUrl);
        driver.manage().timeouts().pageLoadTimeout(15, TimeUnit.SECONDS);
        waitObj.until(ExpectedConditions.elementToBeClickable(By.name("ctsUser")));
        Reporter.log("Navigation to " + baseUrl + " is Done");
    }

    @Test(priority = 1)
    @Parameters({"testUser","testPassword"})
    public void loginTest(String testUser, String testPassword) throws InterruptedException {

        driver.findElement(By.name("ctsUser")).clear();
        driver.findElement(By.name("ctsUser")).sendKeys (testUser);
        driver.findElement(By.id("CSPortalLoginPassword")).clear();
        driver.findElement(By.id("CSPortalLoginPassword")).sendKeys(testPassword);
        driver.findElement(By.id("login")).click();
        waitObj.until(ExpectedConditions.elementToBeClickable(By.id("login")));
        driver.findElement(By.id("login")).click();
        waitObj.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("iframe")));
        Thread.sleep(7000);
        driver.switchTo().frame(0);
        waitObj.until(ExpectedConditions.visibilityOfElementLocated(By.id("CSPortalTabButtonTitle_105")));
        Assert.assertTrue(driver.findElement(By.id("CSPortalTabButtonTitle_105")).isDisplayed(), "Portel displayed");
        Reporter.log("Step 1: User: '"+ testUser + "' logged in");

    }

    @Test(priority = 2)
    public void openPortal() throws InterruptedException {

        driver.manage().timeouts().pageLoadTimeout(10, TimeUnit.SECONDS);
        driver.findElement(By.id("CSPortalTabButtonTitle_105")).click();
        waitObj.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt("frame_193"));
        waitObj.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt("main"));
        waitObj.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt("main"));
        waitObj.until(ExpectedConditions.elementToBeClickable(By.id("toolbarSearchInput")));
        Assert.assertTrue(driver.findElement(By.id("toolbarSearchInput")).isEnabled(), "Element Exists");
        Reporter.log("Step 2: PIM portal opened");

    }

    @Test(priority = 3)
    @Parameters({"searchNum", "expectErgebnisse"})
    public void searchWithFilter(String searchNum, String expectErgebnisse) throws InterruptedException {

        //  1st Filtering search with 19.39
        driver.findElement(By.id("toolbarSearchInput")).sendKeys(searchNum);
        driver.findElement(By.id("toolbarSearchInput")).sendKeys("\n");
        waitObj.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[@id='8c67e84ce50c9a14c91cf208cdf7f8c5']/img")));
        Reporter.log("Step 3: Search with '" + searchNum + "' is done");
        Thread.sleep(7000);

        //  2nd filtering - Click on the filter button
        driver.findElement(By.xpath("//a[@id='8c67e84ce50c9a14c91cf208cdf7f8c5']/img")).click();
        waitObj.until(ExpectedConditions.elementToBeClickable(By.id("Filter_Itemlocale_Label")));
        driver.findElement(By.id("Filter_Itemlocale_Label")).sendKeys(searchNum);
        Thread.sleep(7000);
        driver.findElement(By.id("Filter_Itemlocale_Label")).sendKeys("\n");
        waitObj.until(ExpectedConditions.presenceOfElementLocated(By.xpath(".//*[@id='CSGuiDialogContent']/table/tbody/tr[3]/td/table/tbody/tr/td[2]")));
        Reporter.log("Step 4: Filter Search with '" + searchNum + "' is done");
        searchString = driver.findElement(By.xpath(".//*[@id='CSGuiDialogContent']/table/tbody/tr[3]/td/table/tbody/tr/td[2]")).getText();
        searchString = searchString.trim();
        Thread.sleep(7000);

        if (searchString.equals(expectErgebnisse)){
            Reporter.log("Actual value '" + searchString +"' matched with Expected value");
        }else {
            Reporter.log("Actual value '" + searchString +"' did not match with Expected value '" + expectErgebnisse + "");
        }

        Assert.assertEquals(searchString, expectErgebnisse, "Check number of search iteam");
    }

    @Test(priority = 4)
    @Parameters("expectString")
    public void showInTree(String expectString) throws InterruptedException {

        waitObj.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//tr[@id='4007777']/td[3]")));
        WebElement searchResult1 = driver.findElement(By.xpath("//tr[@id='4007777']/td[3]"));
//        right click on the 1st search iteam
        Actions oAction = new Actions(driver);
        oAction.contextClick(searchResult1).build().perform();
        Reporter.log("Step 5: Open context menu");
        Thread.sleep(7000);
        driver.manage().timeouts().pageLoadTimeout(10, TimeUnit.SECONDS);
        driver.switchTo().defaultContent(); // go back the main frame
        waitObj.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt("CSPopupFrame"));
        //System.out.println("" + driver.findElement(By.tagName("table")).getText()); //only for debuging

//        click on the last option of the sub-manu
        driver.findElement(By.xpath("//td[contains(., 'Element im Baum anzeigen')]")).click(); // try sendKeys("\n") if it didn't work
        Reporter.log("Step 6: Select context menu iteam");
        Thread.sleep(7000);
        driver.switchTo().defaultContent();
        waitObj.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(0));
        waitObj.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt("frame_193"));
        waitObj.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt("frmLeft"));
        waitObj.until(ExpectedConditions.elementToBeClickable(By.xpath(".//*[@id='4007777']")));
        Reporter.log("Step 7: Show the element in the tree");
        searchString = driver.findElement(By.xpath(".//*[@id='4007777']")).getText();

        if (searchString.equals(expectString)){
            Reporter.log("Actual value '" + searchString +"' matched with Expected value");
        }else {
            Reporter.log("Actual value '" + searchString +"' did not match with Expected value '" + expectString + "'");
        }

        Assert.assertEquals(searchString, expectString, "Actual value '" + searchString +"' did not match with Expected value '" + expectString + "'");
        Reporter.log("Test case finished");
    }


    @AfterMethod // executes after each method
    public void takeScreenShotOnFailure(ITestResult testResult1) throws IOException {
        if (testResult1.getStatus() == ITestResult.FAILURE) {
            System.out.println(testResult1.getStatus());
            File scrFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            FileUtils.copyFile(scrFile, new File("errorScreenshots\\" + testResult1.getName() + "-"
                    + Arrays.toString(testResult1.getParameters()) + ".png"));
        }
    }

    @AfterClass
    public void teardown() {

        driver.quit();
        Reporter.log("Browser closed");
    }
}
