package es.osoco.bbva.ats.forms.cucumber

import cucumber.api.CucumberOptions
import cucumber.api.junit.Cucumber
import org.junit.runner.RunWith

@RunWith(Cucumber.class)
@CucumberOptions(
        plugin=["pretty", "html:build/reports/cucumber", "json:build/reports/cucumber/cucumber.json"],
        strict=true,
        features=["src/test/cucumber/features"],
        glue=["classpath:es.osoco.bbva.ats.forms.cucumber.steps"]
)

public class RunCukesTest {

}
