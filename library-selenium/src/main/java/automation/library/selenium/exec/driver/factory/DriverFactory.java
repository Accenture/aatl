package automation.library.selenium.exec.driver.factory;

import automation.library.common.Property;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import automation.library.selenium.exec.driver.managers.*;

/**
 * Threadlocal instance of WebDriver using Factory pattern
 * Supports remote execution via Saucelabs / BrowserStack / Selenium Grid or local execution.
 * <p>
 * The use of either saucelabs/grid/local is set in the Context instance for
 * the execution thread (via the TestNG DataProvider). For saucelabs or grid then the address
 * of the target host and any credentials are specified in the project properties file.
 * <p>
 * For local execution the broswers currently supported are Firefox, Chrome, IE, Edge,
 * Safari, HTML Unit and Phantom JS but this can be extended as needed by adding further
 * DriverManager classes.
 * <p>
 * The locations of the required drivers/binaries are specified in the project properties file.
 */

public class DriverFactory {

    public enum ServerType {
        local, grid, saucelabs, browserstack, appium, smartbear, bitbar;
    }

    public enum BrowserType {
        chrome, firefox, ie, edge, safari, phantomjs, htmlunit;
    }

    protected DriverFactory() {
    }

    private static DriverFactory instance = new DriverFactory();

    public static DriverFactory getInstance() {
        return instance;
    }

    ThreadLocal<DriverManager> driverManager = new ThreadLocal<DriverManager>() {
        protected DriverManager initialValue() {
            return setDM();
        }
    };

    public DriverManager driverManager() {
        return driverManager.get();
    }

    public WebDriver getDriver() {
        if(Property.getVariable("cukes.techstack") !=null){
            return driverManager.get().getDriver();
        }
        return null;
    }

    public WebDriver returnDriver() {
        return driverManager.get().returnDriver();
    }

    public WebDriverWait getWait() {
        return driverManager.get().getWait();
    }

    public void quit() {
        driverManager.get().quitDriver();
        driverManager.remove();
    }

    public DriverManager setDM() {
        ServerType serverType = ServerType.valueOf(DriverContext.getInstance().getTechStack().get("seleniumServer"));
        String browserType = DriverContext.getInstance().getBrowserName();

        switch (serverType) {
            case grid:
                driverManager.set(new GridDriverManager());
                break;
            case saucelabs:
                driverManager.set(new SauceLabsDriverManager());
                break;
            case browserstack:
                driverManager.set(new BrowserStackDriverManager());
                break;
            case appium:
                driverManager.set(new AppiumDriverManager());
                break;
            case smartbear:
                //TODO - for next release
//                driverManager.set(new SmartBearDriverManager());
                break;
            case bitbar:
                driverManager.set(new BitBarDriverManager());
                break;
                default:
                switch (browserType) {
                    case "chrome":
                        driverManager.set(new ChromeDriverManager());
                        break;
                    case "firefox":
                        driverManager.set(new FirefoxDriverManager());
                        break;
                    case "internet explorer":
                        driverManager.set(new IEDriverManager());
                        break;
                    case "MicrosoftEdge":
                        driverManager.set(new EdgeDriverManager());
                        break;
                    case "safari":
                        driverManager.set(new SafariDriverManager());
                        break;
                    case "phantomjs":
                        driverManager.set(new PhantomJSDriverManager());
                        break;
                    case "htmlunit":
                        driverManager.set(new HTMLUnitDriverManager());
                        break;
                }
                break;
        }

        return driverManager.get();

    }
} 