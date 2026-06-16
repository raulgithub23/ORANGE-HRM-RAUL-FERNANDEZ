package runner;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

/**
 * PPT 3.3.1 - Runner general para los Casos 1 al 14.
 * Genera reportes HTML, JSON y XML en target/cucumber-reports/.
 * Para ejecutar: clic derecho -> Run As -> JUnit Test
 * Para reporte Maven: Run As -> Maven Build -> goals: clean verify
 */
@RunWith(Cucumber.class)
@CucumberOptions(
    features = "src/test/resources/features",
    glue     = {"stepDefinitions", "hooks"},
    plugin   = {
        "pretty",
        "html:target/cucumber-reports/reporte-general.html",
        "json:target/cucumber-reports/reporte-general.json",
        "junit:target/cucumber-reports/reporte-general.xml"
    },
    monochrome = true
)
public class TestRunner {
}
