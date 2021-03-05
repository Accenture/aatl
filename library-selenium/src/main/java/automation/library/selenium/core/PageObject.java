package automation.library.selenium.core;

import automation.library.common.Property;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.asserts.SoftAssert;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static automation.library.selenium.core.Locator.Loc;
import static automation.library.selenium.core.Locator.getLocator;


/**
 * Utility class providing set of selenium wrapper methods for finding web elements with build in waits
 */
public class PageObject {
    protected Logger log = LogManager.getLogger(this.getClass().getName());
    protected WebDriver driver;
    protected WebDriverWait wait;
    protected SoftAssert sa = null;

    public PageObject(WebDriver driver){
        this.driver = driver;
        wait = new WebDriverWait(driver, getWaitDuration());
    }

    /**
     * return web driver for the current thread
     */
    public WebDriver getDriver() {
        log.debug("obtaining the driver object for current thread");
        return driver;
    }

    /**
     * return web driver wait for the current thread
     */
    public WebDriverWait getWait() {
        log.debug("obtaining the wait object for current thread");
        return wait;
    }

    public void initialise(Object obj) {
        PageFactory.initElements(getDriver(), obj);
    }

    /**
     * Set of common methods for Page Objects which are defined with either
     * standard By locators or PageFactory
     * @param url
     * @return
     */
    public PageObject gotoURL(String url) {
        log.debug("navigating to url:" + url);
        getDriver().get(url);
        return this;
    }

    /**
     * Wait for page to load
     */
    public PageObject waitPageToLoad() {
        domLoaded();
        jqueryLoaded();
        //angularLoaded();
        return this;
    }

    /**
     * Wait for page to load based on document.readyState=complete
     */
    public void domLoaded() {
        log.debug("checking that the DOM is loaded");
        final JavascriptExecutor js = (JavascriptExecutor) getDriver();
        boolean domReady = js.executeScript("return document.readyState").equals("complete");

        if (!domReady) {
            getWait().until(new ExpectedCondition<Boolean>() {
                public Boolean apply(WebDriver d) {
                    return (js.executeScript("return document.readyState").equals("complete"));
                }
            });
        }
    }

    /**
     * Wait for JQuery to load
     */
    private void jqueryLoaded() {
        log.debug("checking that any JQuery operations complete");
        final JavascriptExecutor js = (JavascriptExecutor) getDriver();

        if ((Boolean) js.executeScript("return typeof jQuery != 'undefined'")) {
            boolean jqueryReady = (Boolean) js.executeScript("return jQuery.active==0");

            if (!jqueryReady) {
                getWait().until(new ExpectedCondition<Boolean>() {
                    public Boolean apply(WebDriver d) {
                        return (Boolean) js.executeScript("return window.jQuery.active === 0");
                    }
                });
            }
        }
    }

    /**
     * Wait for AngularJs to load
     */
    public void angularLoaded() {
        log.debug("checking that any AngularJS operations complete");
        final JavascriptExecutor js = (JavascriptExecutor) getDriver();
        if ((Boolean) js.executeScript("if (window.angular){return true;}")) {
            Boolean angularInjectorUndefined = (Boolean) js.executeScript("return angular.element(document).find('body').injector() === undefined");

            if (!angularInjectorUndefined) {
                Boolean angularReady = (Boolean) js.executeScript("return angular.element(document).find('body').injector().get('$http').pendingRequests.length === 0");

                if (!angularReady) {
                    getWait().until(new ExpectedCondition<Boolean>() {
                        public Boolean apply(WebDriver d) {
                            return js.executeScript("return angular.element(document).find('body').injector().get('$http').pendingRequests.length === 0").equals(true);
                        }
                    });
                }
            } else {
                log.debug("no AngularJS injector defined so cannot wait");
            }
        }
    }

    /*
     * Further methods for Page Objects when using Standard By Locators (non PageFactory)
     * These utilise an additional Element class that wraps WebElement to provide additional
     * functionality or composite functions (see Element class for details).
     * These methods include in-built waits when finding elements as needed.
     */

    /**
     * Returns first element occurrence matching the supplied locator if an element exists in DOM
     * @param by
     * @return
     */
    public Element $(By by) {
        return findElement(by);
    }

    /**
     * return first element using a locator type (from enum), string locator value and optional tokens to
     * substitute into the locator (this is useful for dynamic locators)
     * @param type
     * @param locator
     * @param variables
     * @return
     */
    public Element $(Loc type, String locator, Object... variables) {
        Element el = new Element(driver, getLocator(type, locator, variables));
        return el.scroll();
    }

    /**
     * Returns first element occurrence matching the supplied locator based on the supplied wait condition
     * @param exp
     * @param delay
     * @return
     */
    public Element $(ExpectedCondition<?> exp, int... delay) {
        Element el = new Element(driver, exp, delay);
        return el.scroll();
    }

    /**
     * Finds first nested element within current element matching the supplied locator
     * @param by
     * @param sub
     * @param delay
     * @return
     */
    public Element $(By by, By sub, int... delay) {
        Element el = new Element(driver, ExpectedConditions.presenceOfNestedElementLocatedBy(by, sub), delay);
        return el.scroll();
    }

    /**
     * Returns all element occurrences matching the supplied locator if the elements exist in DOM
     * @param by
     * @return
     */
    public List<Element> $$(By by) {
        WebDriverWait wait = new WebDriverWait(getDriver(), getWaitDuration());
        List<WebElement> els = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(by));
        List<Element> elements = setElements(els);
        if (elements.size() > 0) {
            elements.get(0).scroll();
        }
        return elements;
    }

    /**
     * Returns all elements using a locator type (from enum), string locator value and optional tokens to substitute
     * into the locator (this is useful for dynamic locators)
     * @param type
     * @param locator
     * @param variables
     * @return
     */
    public List<Element> $$(Loc type, String locator, Object[]... variables) {
        WebDriverWait wait = new WebDriverWait(getDriver(), getWaitDuration());
        List<WebElement> els = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(getLocator(type, locator, variables)));
        List<Element> elements = setElements(els);
        if (elements.size() > 0) {
            elements.get(0).scroll();
        }
        return elements;
    }

    /**
     * Returns all element occurrences matching the supplied locator if the elements exist in DOM
     */
    public List<Element> $$(ExpectedCondition<List<WebElement>> exp, int... delay) {
        WebDriverWait wait = new WebDriverWait(getDriver(), delay.length > 0 ? delay[0] : getWaitDuration());
        List<WebElement> els = wait.until(exp);
        List<Element> elements = setElements(els);
        if (elements.size() > 0) {
            elements.get(0).scroll();
        }
        return elements;
    }

    /**
     * Finds all elements within current element matching the supplied locator
     * @param by locator
     * @param sub sublocator
     * @param delay web driver wait delay
     * @return list of elements
     */
    public List<Element> $$(By by, By sub, int... delay) {
        WebDriverWait wait = new WebDriverWait(getDriver(), delay.length > 0 ? delay[0] : getWaitDuration());
        List<WebElement> els = wait.until(ExpectedConditions.presenceOfNestedElementsLocatedBy(by, sub));
        List<Element> elements = setElements(els);
        if (elements.size() > 0) {
            elements.get(0).scroll();
        }
        return elements;
    }

    /**
     * Returns first element occurrence matching the supplied locator if an element exists in DOM
     */
    public Element findElement(By by) {
        Element el = new Element(driver, by);
        return el.scroll();
    }

    /**
     * return first element using a locator type (from enum), string locator value and optional tokens to
     * substitute into the locator (this is useful for dynamic locators)
     * @param type
     * @param locator
     * @param variables
     * @return
     */
    public Element findElement(Loc type, String locator, Object... variables) {
        Element el = new Element(driver, getLocator(type, locator, variables));
        return el.scroll();
    }

    /**
     * Returns first element occurrence matching the supplied locator if an element exists in DOM
     * @param exp
     * @param delay
     * @return
     */
    public Element findElement(ExpectedCondition<?> exp, int... delay) {
        Element el = new Element(driver, exp, delay);
        return el.scroll();
    }

    /**
     * Finds first nested element within current element matching the supplied locator
     */
    public Element findElement(By by, By sub, int... delay) {
        Element el = new Element(driver, ExpectedConditions.presenceOfNestedElementLocatedBy(by, sub), delay);
        return el.scroll();
    }

    /**
     * wait for elements to present, returns all element occurrences matching the supplied locator if the elements exist
     * else empty list
     */
    public List<Element> findElements(By by,int...delay) {
        WebDriverWait wait = new WebDriverWait(getDriver(), delay.length > 0 ? delay[0] : getWaitDuration());
        try{
            List<WebElement> els = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(by));
            ExpectedConditions.numberOfElementsToBeMoreThan(by, 2);
            List<Element> elements = setElements(els);
            if (elements.size() > 0) {
                elements.get(0).scroll();
            }
            return elements;
        }catch (Exception e){
            return Collections.emptyList();
        }
    }

    /**
     * Wait for elements to present, Returns all element occurrences matching the supplied locator if the elements exist in DOM
     * @param type one of the locator type from one of the enum in automation.library.selenium.core.Locator
     * @param locator locator
     * @param variables optional variable if need to be parsed in locator
     * @return all elements occurance matching the locator or empty list
     */
    public List<Element> findElements(Loc type, String locator, Object[]... variables) {
        WebDriverWait wait = new WebDriverWait(getDriver(), getWaitDuration());
        try{
            List<WebElement> els = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(getLocator(type, locator, variables)));
            List<Element> elements = setElements(els);
            if (elements.size() > 0) {
                elements.get(0).scroll();
            }
            return elements;
        }catch (Exception e){
            return Collections.emptyList();
        }
    }

    /**
     * Wait for elements to present, returns all element occurrences matching the supplied exp if the elements exist in DOM
     */
    public List<Element> findElements(ExpectedCondition<List<WebElement>> exp, int... delay) {
        WebDriverWait wait = new WebDriverWait(getDriver(), delay.length > 0 ? delay[0] : getWaitDuration());
        try{
            List<WebElement> els = wait.until(exp);
            List<Element> elements = setElements(els);
            if (elements.size() > 0) {
                elements.get(0).scroll();
            }
            return elements;
        }catch (Exception e){
            return Collections.emptyList();
        }
    }

    /**
     * Finds all elements within current element matching the supplied locator
     */
    public List<Element> findElements(By by, By sub, int... delay) {
        WebDriverWait wait = new WebDriverWait(getDriver(), delay.length > 0 ? delay[0] : getWaitDuration());
        try{
            List<WebElement> els = wait.until(ExpectedConditions.presenceOfNestedElementsLocatedBy(by, sub));
            List<Element> elements = setElements(els);
            if (elements.size() > 0) {
                elements.get(0).scroll();
            }
            return elements;
        }catch(Exception e){
            log.error(e.toString());
            return Collections.emptyList();
        }
    }

    /**
     * Returns first element occurrence matching the supplied locator if an element is clickable
     */
    //TODO - Describe what happens when not found any clickable
    public Element findClickable(By by, int... delay) {
        Element el = new Element(driver, by, delay).clickable();
        return el.scroll();
    }

    /**
     * Builds and returns list of nested elements
     * @param els
     * @return
     */
    public List<Element> setElements(List<WebElement> els) {
        List<Element> list = new ArrayList<Element>();
        for (WebElement el : els) {
            list.add(new Element(driver, el));
        }
        return list;
    }

    /**
     * Checks for element existence
     * @param by
     * @param delay
     * @return
     */
    public boolean exist(By by, int... delay) {
        Element el = new Element(driver, by, delay);
        return el.element() != null;
    }

    /**
     * is this element displayed or not?
     * @param by
     * @return
     */
    public boolean isVisible(By by) {
            return findElement(by).element().isDisplayed();
    }

    /**
     * is this element displayed or not?
     * @param by
     * @return
     */
    public boolean isDisplayed(By by) {return findElement(by).element().isDisplayed();}

     /** is thsi element enabled or not?
     * @param by locator of element
     * @return true if enabled
     */
    public boolean isEnables(By by) { return findElement(by).element().isEnabled();}


    /**
     * Returns duration for specified waits as defined in "/src/test/resources/config/selenium/runtime.properties" or
     * default wait
     */
    public int getWaitDuration() {
        final int defaultWait = 10;
        int duration;
        try {
            duration = Property.getProperties(Constants.SELENIUMRUNTIMEPATH).getInt("defaultWait");
            log.debug("selenium getDriver() getWait() time set from environment properties: " + duration);
        } catch (Exception e) {
            duration = defaultWait;
            log.debug("selenium getDriver() getWait() time not available from environment properties...default applied : " + defaultWait);
        }
        return duration;
    }
    /**
     * Switch the focus of future commands for this driver to the window with the given handle.
     * @param parent he name of the window or the handle which can be used to iterate over all open windows
     */
    public void switchWindow(String parent) {
        log.debug("parent window handle:" + parent);
        switching:
        while (true) {
            for (String handle : getDriver().getWindowHandles()) {
                if (!handle.equals(parent)) {
                    log.debug("switching to window handle:" + handle);
                    getDriver().switchTo().window(handle);
                    break switching;
                }
            }
        }
    }

    /**
     * An expectation for checking whether the given frame is available to switch to. <p> If the frame
     * is available it switches the given driver to the specified frameIndex.
     * @param frameLocator used to find the frame (index)
     */
    public void switchFrame(int frameLocator) {
        getWait().until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(frameLocator));
    }

    /**
     * An expectation for checking whether the given frame is available to switch to. <p> If the frame
     * is available it switches the given driver to the specified frame.
     * @param frameLocator used to find the frame (id or name)
     */
    public void switchFrame(String frameLocator) {
        getWait().until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(frameLocator));
    }

    /**
     * An expectation for checking whether the given frame is available to switch to. <p> If the frame
     * is available it switches the given driver to the specified frame.
     * @param by used to find the frame
     */
    public void switchFrame(By by) {
        getWait().until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(by));
    }

    /**
     * An expectation for checking whether the given frame is available to switch to. <p> If the frame
     * is available it switches the given driver to the specified webelement.
     * @param el used to find the frame (webelement)
     */
    public void switchFrame(Element el) {
        getWait().until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(el.element()));
    }

    /**
     * Selects either the first frame on the page, or the main document when a page contains iframes.
     */
    public void switchToDefaultContent() {
        getDriver().switchTo().defaultContent();
    }

    public SoftAssert sa() {
        if (sa == null) {
            sa = new SoftAssert();
        }
        return sa;
    }

    /**
     * capture displayed area or scrolling screenshot and return a file object.
     * to capture scrolling screenshot property scrollingScreenshot = true has to be set in runtime.properties file
     */
    public File grabScreenshot() {
        return Screenshot.grabScreenshot(driver);
    }
}