package stepDefinitions;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.util.HashMap;
import java.util.Map;

// maneja cuando arranca y muere el webdriver. lo crea antes del escenario y lo mata al final. 
// esto se inyecta en todas las clases de steps (usando picocontainer de cucumber) asi 
// comparten exactamente el MISMO driver durante toda la prueba y no levantan mil chromes.
public class Configuracion {

    private WebDriver driver;

    // corre antes de cada escenario. el webdrivermanager se baja (o usa el que ya bajo) 
    // el chromedriver que le vaya al chrome que esta instalado, asi no ando ruteando a mano el exe.
    //
    // le clavo el idioma a ingles (--lang=en-US) porque un monton de locators buscan los textos 
    // crudos de orange (tipo "Save" o "Search"). si el chrome se hace el vivo y me traduce la pagina, 
    // no encuentra nada y rompe todo.
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

    // con order = 0 me aseguro de que este @After corra DESPUES del de Hooks.afterScenario 
    // (que tiene order = 10), cucumber los corre de mayor a menor. asi le doy tiempo a que saque 
    // la foto de evidencia con el browser abierto y recien despues lo cierro y limpio.
    @After(order = 0)
    public void finalizar() {
        if (driver != null) {
            driver.quit();
            driver = null;
        }
    }

    // de aca saco el driver vivo para que lo usen las demas clases y page objects
    public WebDriver obtenerDriver() {
        return driver;
    }
}