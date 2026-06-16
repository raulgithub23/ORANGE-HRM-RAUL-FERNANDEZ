package runner;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

/**
 * PPT 3.1.1 + 3.2.1 + 3.3.1 - Caso 15: Runner de Suite Completa.
 * Ejecuta todos los escenarios marcados con @smoke o @regression.
 * Genera HTML + JSON + XML para integración CI/CD.
 *
 * Para ejecutar:
 *   - Eclipse: clic derecho -> Run As -> JUnit Test
 *   - Maven:   mvn clean verify -Dtest=SuiteCompleta
 */
@RunWith(Cucumber.class)
@CucumberOptions(
    features = "src/test/resources/features",
    glue     = {"stepDefinitions", "hooks"},
    tags     = "@smoke or @regression",
    plugin   = {
        "pretty",
        "html:target/cucumber-reports/suite-completa.html",
        "json:target/cucumber-reports/suite-completa.json",
        "junit:target/cucumber-reports/suite-completa.xml"
    },
    monochrome = true
)
public class SuiteCompleta {
}
