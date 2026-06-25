package stepDefinitions;

import io.cucumber.java.es.Cuando;
import io.cucumber.java.es.Dado;
import io.cucumber.java.es.Entonces;
import io.cucumber.java.es.Y;
import org.junit.Assert;
import org.openqa.selenium.WebDriver;
import pageObjects.DashboardPage;
import pageObjects.LoginPage;

import java.io.IOException;

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

    private LoginPage loginPage() {
        return new LoginPage(driver());
    }

    private DashboardPage dashboardPage() {
        return new DashboardPage(driver());
    }

    // ─── CASO 1 y 2: login válido redirige al Dashboard; login inválido
    // muestra el mensaje de error de OrangeHRM sin redirigir ────────────────

    @Dado("el usuario navega a {string}")
    public void el_usuario_navega_a(String url) {
        loginPage().abrir(url);
    }

    @Cuando("ingresa usuario {string} y contraseña {string}")
    public void ingresa_usuario_y_contrasena(String usuario, String clave) {
        loginPage().ingresarCredenciales(usuario, clave);
    }

    @Y("hace click en el botón Login")
    public void hace_click_en_boton_login() {
        loginPage().clickLogin();
    }

    @Entonces("debe visualizarse el Dashboard de OrangeHRM")
    public void debe_visualizarse_el_dashboard() throws IOException {
        dashboardPage().esperarCarga();
        Assert.assertTrue("No se cargó el Dashboard", dashboardPage().estaCargado());
        System.out.println("CASO 1 OK: Login exitoso");
    }

    @Entonces("se muestra el mensaje {string}")
    public void se_muestra_el_mensaje(String mensajeEsperado) throws IOException {
        String mensajeReal = loginPage().obtenerMensajeError();
        Assert.assertEquals("Mensaje de error incorrecto", mensajeEsperado, mensajeReal);
        System.out.println("CASO 2 OK: Mensaje de error verificado");
    }

    // ─── CASO 3: tras autenticarse, se cierra sesión desde el menú de
    // usuario y se valida que la URL regrese a la pantalla de login ────────

    @Dado("el usuario está autenticado en OrangeHRM")
    public void el_usuario_esta_autenticado() {
        loginPage().loginPorDefecto();
    }

    @Cuando("hace click en el menú de usuario")
    public void hace_click_en_menu_usuario() throws IOException {
        dashboardPage().abrirMenuUsuario();
    }

    @Y("selecciona la opción Logout")
    public void selecciona_opcion_logout() {
        dashboardPage().seleccionarLogout();
    }

    @Entonces("debe redirigirse a la pantalla de Login")
    public void debe_redirigirse_a_login() throws IOException {
        loginPage().esperarPantallaLogin();
        Assert.assertTrue("No redirigió al login",
            driver().getCurrentUrl().contains("login"));
        System.out.println("CASO 3 OK: Logout exitoso");
    }

    // ─── CASO 4: este escenario está diseñado para fallar a propósito (el
    // título real de la página nunca contendrá "ModuloAbout"), con el único
    // fin de verificar que el hook global de Hooks.java efectivamente toma
    // una captura de pantalla cuando un assert revienta, marcándola como
    // FAIL_ en el nombre de archivo ─────────────────────────────────────────

    @Cuando("navega al módulo About")
    public void navega_al_modulo_about() {
        dashboardPage().abrir();
    }

    @Entonces("el título de la página debe contener {string}")
    public void el_titulo_debe_contener(String tituloEsperado) throws IOException {
        String tituloReal = driver().getTitle();
        Assert.assertTrue(
            "Título incorrecto. Esperado contener: '" + tituloEsperado + "', Real: '" + tituloReal + "'",
            tituloReal.contains(tituloEsperado));
        System.out.println("CASO 4 OK: Título verificado.");
    }
}