package stepDefinitions;

import io.cucumber.java.es.Cuando;
import io.cucumber.java.es.Dado;
import io.cucumber.java.es.Entonces;
import io.cucumber.java.es.Y;
import org.junit.Assert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.IOException;
import java.time.Duration;

/**
 * PPT 3.1.1 - Casos 1 al 4: Login, Login Fallido, Logout y Captura en Fallo.
 * (Capturas delegadas al Hook global)
 */
public class LoginSteps {

    private final Configuracion configuracion;

    public LoginSteps(Configuracion configuracion) {
        this.configuracion = configuracion;
    }

    private WebDriver driver() {
        return configuracion.obtenerDriver();
    }

    private WebDriverWait espera() {
        return new WebDriverWait(driver(), Duration.ofSeconds(15));
    }

    // ─── CASO 1 y 2: Login exitoso y fallido ──────────────────────────────────

    @Dado("el usuario navega a {string}")
    public void el_usuario_navega_a(String url) {
        driver().get(url);
        espera().until(ExpectedConditions.visibilityOfElementLocated(By.name("username")));
    }

    @Cuando("ingresa usuario {string} y contraseña {string}")
    public void ingresa_usuario_y_contrasena(String usuario, String clave) {
        driver().findElement(By.name("username")).sendKeys(usuario);
        driver().findElement(By.name("password")).sendKeys(clave);
    }

    @Y("hace click en el botón Login")
    public void hace_click_en_boton_login() {
        driver().findElement(By.cssSelector("button[type='submit']")).click();
    }

    @Entonces("debe visualizarse el Dashboard de OrangeHRM")
    public void debe_visualizarse_el_dashboard() throws IOException {
        espera().until(ExpectedConditions.urlContains("/dashboard/index"));
        Assert.assertTrue("No se cargó el Dashboard",
            driver().getTitle().contains("OrangeHRM"));
        System.out.println("CASO 1 OK: Login exitoso");
    }

    @Entonces("se muestra el mensaje {string}")
    public void se_muestra_el_mensaje(String mensajeEsperado) throws IOException {
        By alertaError = By.cssSelector(".oxd-alert-content-text");
        String mensajeReal = espera()
            .until(ExpectedConditions.visibilityOfElementLocated(alertaError))
            .getText();
        Assert.assertEquals("Mensaje de error incorrecto", mensajeEsperado, mensajeReal);
        System.out.println("CASO 2 OK: Mensaje de error verificado");
    }

    // ─── CASO 3: Logout ───────────────────────────────────────────────────────

    @Dado("el usuario está autenticado en OrangeHRM")
    public void el_usuario_esta_autenticado() {
        driver().get("https://opensource-demo.orangehrmlive.com/web/index.php/auth/login");
        espera().until(ExpectedConditions.visibilityOfElementLocated(By.name("username")))
            .sendKeys("Admin");
        driver().findElement(By.name("password")).sendKeys("admin123");
        driver().findElement(By.cssSelector("button[type='submit']")).click();
        espera().until(ExpectedConditions.urlContains("/dashboard/index"));
    }

    @Cuando("hace click en el menú de usuario")
    public void hace_click_en_menu_usuario() throws IOException {
        By menuUsuario = By.cssSelector(".oxd-userdropdown-tab");
        espera().until(ExpectedConditions.elementToBeClickable(menuUsuario)).click();
    }

    @Y("selecciona la opción Logout")
    public void selecciona_opcion_logout() {
        By opcionLogout = By.xpath("//a[normalize-space()='Logout']");
        espera().until(ExpectedConditions.elementToBeClickable(opcionLogout)).click();
    }

    @Entonces("debe redirigirse a la pantalla de Login")
    public void debe_redirigirse_a_login() throws IOException {
        espera().until(ExpectedConditions.urlContains("/auth/login"));
        Assert.assertTrue("No redirigió al login",
            driver().getCurrentUrl().contains("login"));
        System.out.println("CASO 3 OK: Logout exitoso");
    }

    // ─── CASO 4: Verificación que activa el hook @After ───────────────────────

    @Cuando("navega al módulo About")
    public void navega_al_modulo_about() {
        driver().get("https://opensource-demo.orangehrmlive.com/web/index.php/dashboard/index");
        espera().until(ExpectedConditions.urlContains("/dashboard/index"));
    }

    @Entonces("el título de la página debe contener {string}")
    public void el_titulo_debe_contener(String tituloEsperado) throws IOException {
        String tituloReal = driver().getTitle();
        // Este assert pasa normalmente (OrangeHRM está en el título)
        Assert.assertTrue(
            "Título incorrecto. Esperado contener: '" + tituloEsperado + "', Real: '" + tituloReal + "'",
            tituloReal.contains(tituloEsperado));
        System.out.println("CASO 4 OK: Título verificado.");
    }
}