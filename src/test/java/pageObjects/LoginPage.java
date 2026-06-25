package pageObjects;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;

// page object del form de login de orangehrm
public class LoginPage extends BasePage {

    private static final By INPUT_USERNAME = By.name("username");
    private static final By INPUT_PASSWORD = By.name("password");
    private static final By BTN_SUBMIT = By.cssSelector("button[type='submit']");
    private static final By ALERTA_ERROR = By.cssSelector(".oxd-alert-content-text");

    public LoginPage(WebDriver driver) {
        super(driver);
    }

    public void abrir(String url) {
        driver.get(url);
        wait.until(ExpectedConditions.visibilityOfElementLocated(INPUT_USERNAME));
    }

    public void ingresarCredenciales(String usuario, String contrasena) {
        waitVisible(INPUT_USERNAME).sendKeys(usuario);
        driver.findElement(INPUT_PASSWORD).sendKeys(contrasena);
    }

    public void clickLogin() {
        driver.findElement(BTN_SUBMIT).click();
    }

    public void login(String usuario, String contrasena) {
        ingresarCredenciales(usuario, contrasena);
        clickLogin();
    }

    // login de una con las credenciales de admin de la demo (Admin / admin123). lo uso como 
    // precondicion en los steps de los modulos que no estan testeando el login en si (onda empleados, 
    // perfil, licencias, etc), asi no tengo que repetir la movida de "ir al login + escribir + click" 
    // a cada rato en cada step definition distinto
    public void loginPorDefecto() {
        abrir(BASE_URL + "/auth/login");
        login("Admin", "admin123");
        waitForUrlContains("/dashboard/index");
    }

    public String obtenerMensajeError() {
        return waitVisible(ALERTA_ERROR).getText();
    }

    public void esperarPantallaLogin() {
        waitForUrlContains("/auth/login");
    }

    public boolean tituloContiene(String texto) {
        return driver.getTitle().contains(texto);
    }
}