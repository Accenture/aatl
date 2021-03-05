package runners;

import automation.library.cucumber.selenium.RunnerClassParallel;
import automation.library.cucumber.selenium.RunnerClassSequential;
import automation.library.reporting.AllureScreenShotPub;
import automation.library.reporting.TextReport;
import io.cucumber.testng.CucumberOptions;
import org.testng.annotations.AfterTest;

@CucumberOptions(
        plugin = {"io.qameta.allure.cucumber4jvm.AllureCucumber4Jvm","json:RunReports/cucumberJson/cucumber-report.json"},
        features = {"classpath:features"},
        glue = {"steps", "hooks"}
        )

public class BaseRunner extends RunnerClassSequential {

        @AfterTest
        public void teardown() {
                TextReport tr = new TextReport();
                tr.createReport(true);
        }
}