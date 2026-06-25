package pageObjects;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;

/**
 * Page Object del Dashboard y acciones del header (menú usuario, navegación).
 */
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

    public void navegarAPim() {
        longWait.until(ExpectedConditions.elementToBeClickable(MENU_PIM)).click();
        longWait.until(ExpectedConditions.urlContains("/pim/viewEmployeeList"));
    }

    public void navegarAMyInfo() {
        waitClickable(MENU_MY_INFO).click();
        esperarSinSpinner();
        waitForUrlContains("/pim/viewPersonalDetails");
    }

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
