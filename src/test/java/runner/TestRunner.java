package runner;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

// runner general para los casos 1 al 14 (todos los features de una, sin andar filtrando por tags). 
// te da los reportes en html, json y xml ahi en target/cucumber-reports/.
//
// nota: maven surefire es medio mañoso y por defecto te corre todas las clases que terminan en 
// "Test" (o que tienen el runwith) adentro de src/test/java cuando le mandas "mvn test" o 
// "mvn verify" a secas. asi que te termina agarrando tanto este testrunner como el de suitecompleta. 
// y como los dos apuntan a la misma carpeta de features y encima todo tiene los tags de smoke o 
// regression, al final el "mvn clean verify" te corre todos los escenarios DOS VECES (una por 
// cada runner). lo deje asi a proposito para la entrega de la facu (asi nos queda doble evidencia 
// y dos reportes distintos), aunque tarde el doble de tiempo en terminar de correr.
//
// para correr solo este: mvn test -Dtest=TestRunner
// para sacar el reporte de masterthought: mvn clean verify (fijate en el pom.xml, fase verify)
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