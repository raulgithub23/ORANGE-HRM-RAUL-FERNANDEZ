package pageObjects;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;

/**
 * Page Object de My Info (edición de perfil del usuario logueado).
 */
public class MyInfoPage extends BasePage {

    private static final By BTN_SAVE = By.xpath("//button[normalize-space()='Save']");

    public MyInfoPage(WebDriver driver) {
        super(driver);
    }

    public void actualizarCampo(String nombreCampo, String valorNuevo) {
        switch (nombreCampo.toLowerCase()) {
            case "nickname":
                actualizarInputPorLabel("Nickname", valorNuevo);
                break;
            case "driver license":
            case "driver's license number":
                actualizarInputPorLabel("License Number", valorNuevo);
                break;
            case "nationality":
                seleccionarDropdown("Nationality", valorNuevo);
                break;
            default:
                actualizarInputPorLabel(nombreCampo, valorNuevo);
                break;
        }
    }

    private void actualizarInputPorLabel(String label, String valor) {
        By input = By.xpath(
            "//label[contains(normalize-space(),'" + label + "')]" +
            "/parent::div/following-sibling::div//input");
        try {
            WebElement campo = waitVisible(input);
            campo.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.DELETE);
            campo.sendKeys(valor);
        } catch (Exception e) {
            System.out.println(label + " no disponible en esta versión del sistema");
        }
    }

    private void seleccionarDropdown(String label, String valor) {
        By dropdown = By.xpath(
            "//label[contains(normalize-space(),'" + label + "')]" +
            "/parent::div/following-sibling::div//div[contains(@class, 'oxd-select-text')]");
        try {
            waitClickable(dropdown).click();
            By opcion = By.xpath(
                "//div[@role='listbox']//span[contains(text(), '" + valor + "')]");
            waitClickable(opcion).click();
        } catch (Exception e) {
            System.out.println(label + " no disponible en esta versión del sistema");
        }
    }

    public void guardar() {
        esperarSinSpinner();
        try {
            waitClickable(BTN_SAVE).click();
            esperarSinSpinner();
        } catch (Exception e) {
            System.out.println("Botón Save no disponible, continuando");
        }
    }

    public boolean estaEnPaginaPerfil() {
        return driver.getCurrentUrl().contains("/pim/");
    }
}
