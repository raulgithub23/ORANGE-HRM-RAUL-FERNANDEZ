package runner;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

// caso 15: runner de la suite completa. la idea era correr solo los escenarios 
// que tengan el tag @smoke o @regression para hacer una pasada de regresion mas filtrada. 
// te saca los reportes en html, json y xml aparte del testrunner normal, mas que nada 
// para engancharlo despues con ci/cd.
//
// igual en la practica, como TODOS los escenarios de los 6 features ya tienen 
// el tag @smoke o @regression (fijate en los .feature), este filtro al final 
// no deja nada afuera . termina corriendo exactamente lo mismo que el testrunner comun. 
// por eso cuando le mandas "mvn clean verify" te corre toda la suite dos veces 
// (ahi deje la nota en TestRunner.java tambien).
//
// para correrlo:
//   - en eclipse: clic derecho a la clase -> run as -> junit test
//   - por consola con maven: mvn test -Dtest=SuiteCompleta
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