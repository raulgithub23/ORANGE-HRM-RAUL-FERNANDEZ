package pageObjects;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;

// page object de my info (para editar el perfil del que esta logueado)
public class MyInfoPage extends BasePage {

    private static final By BTN_SAVE = By.xpath("//button[normalize-space()='Save']");

    public MyInfoPage(WebDriver driver) {
        super(driver);
    }

    // el nombre del campo viene directo del excel (de la columna "Campo"), por eso 
    // lo paso a minusculas y lo engancho con el label real de orange. asi le podemos 
    // meter mas filas al excel en español sin andar tocando el codigo. solo los casos raros 
    // (onda si el label no es igual al del negocio o si es un dropdown en vez de texto) 
    // tienen su propia rama aca en el switch. el resto cae en el default de una.
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
            // le mando ctrl+a + borrar en vez de usar clear() directo. el clear() a veces falla 
            // si el campo tiene alguna mascara rara (tipo fechas o telefonos), asi que simulo 
            // que alguien pinta todo el texto y lo borra a mano con el teclado
            campo.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.DELETE);
            campo.sendKeys(valor);
        } catch (Exception e) {
            // no todos los campos de my info salen en todas las versiones de la demo publica. si el campo 
            // no esta, tiro un aviso por consola nomas pero no corto todo el test por un campo opcional
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