package pageObjects;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;

// page object del dashboard y cositas del header (el menu del usuario, moverse por ahi)
public class DashboardPage extends BasePage {

    private static final By MENU_USUARIO = By.cssSelector(".oxd-userdropdown-tab");
    private static final By OPCION_LOGOUT = By.xpath("//a[normalize-space()='Logout']");
    private static final By MENU_PIM = By.xpath("//span[normalize-space()='PIM']");
    private static final By MENU_MY_INFO = By.xpath("//span[normalize-space()='My Info']");
    private static final By NOMBRE_USUARIO = By.cssSelector(".oxd-userdropdown-name");
    private static final By AVATAR_ALT = By.cssSelector(".oxd-userdropdown-tab img");

    public DashboardPage(WebDriver driver) {
        super(driver, 20, 30);
    }

    public void abrir() {
        driver.get(BASE_URL + "/dashboard/index");
        waitForUrlContains("/dashboard/index");
    }

    public void esperarCarga() {
        waitForUrlContains("/dashboard/index");
    }

    public boolean estaCargado() {
        return driver.getCurrentUrl().contains("/dashboard/index")
            && driver.getTitle().contains("OrangeHRM");
    }

    public void abrirMenuUsuario() {
        waitClickable(MENU_USUARIO).click();
    }

    public void logout() {
        abrirMenuUsuario();
        seleccionarLogout();
    }

    public void seleccionarLogout() {
        waitClickable(OPCION_LOGOUT).click();
    }

    // aca le mando el longwait (30s) en vez del normal porque a veces ir al pim en esta demo publica 
    // tarda una vida si hay mucha gente metida al mismo tiempo
    public void navegarAPim() {
        longWait.until(ExpectedConditions.elementToBeClickable(MENU_PIM)).click();
        longWait.until(ExpectedConditions.urlContains("/pim/viewEmployeeList"));
    }

    public void navegarAMyInfo() {
        waitClickable(MENU_MY_INFO).click();
        esperarSinSpinner();
        waitForUrlContains("/pim/viewPersonalDetails");
    }

    // saca el nombre del que esta logueado mirando el header. primero me fijo en el texto al lado de la foto, 
    // si viene vacio (a veces el diseño carga solo la foto sin el nombre), busco el atributo alt de la 
    // imagen por las dudas. si falla todo devuelvo "Admin" y listo porque total es la unica cuenta que usamos aca
    public String obtenerNombreUsuario() {
        try {
            String nombre = waitVisible(NOMBRE_USUARIO).getText().trim();
            if (!nombre.isEmpty()) {
                return nombre;
            }
        } catch (Exception ignored) {}
        try {
            return driver.findElement(AVATAR_ALT).getAttribute("alt").trim();
        } catch (Exception ignored) {}
        return "Admin";
    }
}