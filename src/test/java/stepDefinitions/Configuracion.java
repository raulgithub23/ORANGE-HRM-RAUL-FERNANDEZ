package stepDefinitions;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.util.HashMap;
import java.util.Map;

/**
 * Gestiona el ciclo de vida del WebDriver con PicoContainer.
 * El driver NO es estático: garantiza aislamiento entre escenarios.
 */
public class Configuracion {

    private WebDriver driver;

    @Before
    public void iniciar() {
        WebDriverManager.chromedriver().setup();
        ChromeOptions opciones = new ChromeOptions();
        opciones.addArguments("--lang=en-US");
        Map<String, Object> prefs = new HashMap<>();
        prefs.put("intl.accept_languages", "en-US,en");
        opciones.setExperimentalOption("prefs", prefs);
        driver = new ChromeDriver(opciones);
        driver.manage().window().maximize();
    }

    @After(order = 0)
    public void finalizar() {
        if (driver != null) {
            driver.quit();
            driver = null;
        }
    }

    public WebDriver obtenerDriver() {
        return driver;
    }
}
