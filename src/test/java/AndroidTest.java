import io.appium.java_client.TouchAction;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.remote.MobileCapabilityType;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Set;

public class AndroidTest {

    AndroidDriver<WebElement> driver;
    static String WEB_UNDER_TEST_URI;
    final static int WEB_UNDER_TEST_PORT = 3000;
    final static String APPIUM_SERVER_URI = "http://127.0.0.1:4723/wd/hub";
    static String WEBVIEW;

    WebElement actionResult;
    WebElement singleTapBtn;
    WebElement doubleTapBtn;
    WebElement longTapBtn;
    WebElement bunchOfNumbersTextarea;

    int elementCoordinateX, elementCoordinateY;

    @Before
    public void setUp() throws Exception {
        DesiredCapabilities capabilities = new DesiredCapabilities();
        // change to your target device information
        capabilities.setCapability(MobileCapabilityType.PLATFORM_VERSION, "9");
        capabilities.setCapability(MobileCapabilityType.PLATFORM_NAME, "Android");
        capabilities.setCapability(MobileCapabilityType.AUTOMATION_NAME, "UiAutomator2");
        capabilities.setCapability(MobileCapabilityType.DEVICE_NAME, "Tablet Emulator");
        capabilities.setCapability(MobileCapabilityType.BROWSER_NAME, "Chrome");
        capabilities.setCapability("newCommandTimeout", 2000);
        try {
            driver = new AndroidDriver<>(new URL(APPIUM_SERVER_URI), capabilities);
        } catch (MalformedURLException e) {
            System.out.println(e);
        }

        Set<String> contextNames = driver.getContextHandles();

        WEBVIEW = contextNames.toArray()[1].toString();
        driver.context(WEBVIEW); // set context to CHROMIUM or something else

        // Navigate to the page
        WEB_UNDER_TEST_URI = "http://" + InetAddress.getLocalHost().toString().split("/")[1] + ":" + WEB_UNDER_TEST_PORT;
        driver.get(WEB_UNDER_TEST_URI);

        singleTapBtn = driver.findElement(By.id("single_tap_btn"));
        longTapBtn = driver.findElement(By.id("long_tap_btn"));
        doubleTapBtn = driver.findElement(By.id("double_tap_btn"));
        actionResult = driver.findElement(By.id("action_result"));
        bunchOfNumbersTextarea = driver.findElement(By.id("bunch_of_numbers_textarea"));
    }

    /**
     * Find center position of target element
     * @param driver
     * @param element
     * @return
     */
    public static float[] getElementCenter(AndroidDriver<WebElement> driver, WebElement element) {
        driver.context(WEBVIEW);
        JavascriptExecutor js = driver;

        // get webview dimensions
        Long webviewWidth = (Long) js.executeScript("return screen.width");
        Long webviewHeight = (Long) js.executeScript("return screen.height");

        // get element location in webview
        int elementLocationX = element.getLocation().getX();
        int elementLocationY = element.getLocation().getY();

        // get the center location of the element
        int elementWidthCenter = element.getSize().getWidth() / 2;
        int elementHeightCenter = element.getSize().getHeight() / 2;
        int elementWidthCenterLocation = elementWidthCenter + elementLocationX;
        int elementHeightCenterLocation = elementHeightCenter + elementLocationY;

        // switch to native context
        driver.context("NATIVE_APP");
        float deviceScreenWidth, deviceScreenHeight;

        // offset: may need to change each device screen
        int offset = 130;

        // get the actual screen dimensions
        deviceScreenWidth = driver.manage().window().getSize().getWidth();
        deviceScreenHeight = driver.manage().window().getSize().getHeight();

        // calculate the ratio between actual screen dimensions and webview dimensions
        float ratioWidth = deviceScreenWidth / webviewWidth.intValue();
        float ratioHeight = deviceScreenHeight / webviewHeight.intValue();

        // calculate the actual element location on the screen
        float elementCenterActualX = elementWidthCenterLocation * ratioWidth;
        float elementCenterActualY = (elementHeightCenterLocation * ratioHeight) + offset;
        float[] elementLocation = {elementCenterActualX, elementCenterActualY};

        // switch back to webview context
        driver.context(WEBVIEW);
        return elementLocation;
    }

    @Test
    public void testSingleTap() throws InterruptedException {
        int beforeActionResult = Integer.parseInt(actionResult.getText());

        float[] elementLocation = getElementCenter(driver, singleTapBtn);
        elementCoordinateX = Math.round(elementLocation[0]);
        elementCoordinateY = Math.round(elementLocation[1]);
        driver.context("NATIVE_APP");
        TouchAction action = new TouchAction(driver);
        action.tap(elementCoordinateX, elementCoordinateY).perform();

        driver.context(WEBVIEW);
        int afterActionResult = Integer.parseInt(actionResult.getText());
        Assert.assertEquals(beforeActionResult + 1, afterActionResult);

        Thread.sleep(2000);
    }

    @Test
    public void testLongTap() throws InterruptedException {
        float[] elementLocation = getElementCenter(driver, longTapBtn);
        elementCoordinateX = Math.round(elementLocation[0]);
        elementCoordinateY = Math.round(elementLocation[1]);
        driver.context("NATIVE_APP");
        TouchAction action = new TouchAction(driver);
        action.press(elementCoordinateX, elementCoordinateY).waitAction(4000).release().perform();

        driver.context(WEBVIEW);
        Assert.assertEquals("rgba(255, 0, 0, 1)", actionResult.getCssValue("color"));

        Thread.sleep(2000);
    }

    @Test
    public void testDoubleTap() throws InterruptedException {
        float[] elementLocation = getElementCenter(driver, doubleTapBtn);
        elementCoordinateX = (int) Math.round(elementLocation[0]);
        elementCoordinateY = (int) Math.round(elementLocation[1]);
        driver.context("NATIVE_APP");
        TouchAction action = new TouchAction(driver);
        action.tap(elementCoordinateX, elementCoordinateY).tap(elementCoordinateX, elementCoordinateY).perform();

        driver.context(WEBVIEW);
        Thread.sleep(2000);
    }

    @Test
    public void testSwipe() throws InterruptedException {
        float[] elementLocation = getElementCenter(driver, bunchOfNumbersTextarea);
        elementCoordinateX = Math.round(elementLocation[0]);
        elementCoordinateY = Math.round(elementLocation[1]);
        driver.context("NATIVE_APP");
        driver.swipe(elementCoordinateX, elementCoordinateY,elementCoordinateX, elementCoordinateY-120,1000);

        driver.context(WEBVIEW);
        Thread.sleep(2000);
    }

    @Test
    public void testFlick() throws InterruptedException {
        // swipe
        float[] elementLocation = getElementCenter(driver, bunchOfNumbersTextarea);
        elementCoordinateX = Math.round(elementLocation[0]);
        elementCoordinateY = Math.round(elementLocation[1]);
        driver.context("NATIVE_APP");
        driver.swipe(elementCoordinateX, elementCoordinateY,elementCoordinateX, elementCoordinateY-120,100);

        driver.context(WEBVIEW);
        Thread.sleep(2000);
    }

    @Test
    public void testPinchOutThenPinchIn() throws InterruptedException {
        float[] elementLocation = getElementCenter(driver, actionResult);
        elementCoordinateX = Math.round(elementLocation[0]);
        elementCoordinateY = Math.round(elementLocation[1]);
        driver.context("NATIVE_APP");
        // pinch out
        driver.zoom(elementCoordinateX, elementCoordinateY);
        Thread.sleep(2000);
        // pinch in
        driver.pinch(elementCoordinateX, elementCoordinateY);

        driver.context(WEBVIEW);
        Thread.sleep(2000);
    }

    @After
    public void tearDown() throws Exception {
        driver.quit();
    }
}
