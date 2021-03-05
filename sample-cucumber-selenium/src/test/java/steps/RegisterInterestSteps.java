package steps;

import automation.library.cucumber.selenium.BaseSteps;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import pageobjects.RegisterInterest;

import java.util.Map;

public class RegisterInterestSteps extends BaseSteps {
	
	RegisterInterest ri;
	
@When("^a trainee registers for an event$")
public void launchApplication(DataTable table) throws Throwable {
	Map<String, String> map = table.asMap(String.class, String.class);
	ri = new RegisterInterest();
	ri.registerInterest(map);	
}

@Then("^a confirmation message is displayed \"(.*)\"$")
public void checkPageDisplay(String expectedMsg) throws Throwable {
	ri.waitPageToLoad();
	sa().assertEquals(ri.getResponse(), expectedMsg);
	sa().assertAll();
}
}	
