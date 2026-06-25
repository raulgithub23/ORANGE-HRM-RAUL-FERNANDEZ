package pageObjects;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

/**
 * Page Object de Leave Types (configuración de tipos de licencia).
 */
public class LeaveTypesPage extends BasePage {

    private static final By TITULO = By.xpath("//*[normalize-space()='Leave Types']");
    private static final By BTN_ADD = By.xpath(
        "//button[contains(@class,'oxd-button') and contains(.,'Add')]");
    private static final By INPUT_NAME = By.xpath(
        "//label[normalize-space()='Name']/parent::div/following-sibling::div//input");
    private static final By BTN_SAVE = By.xpath("//button[normalize-space()='Save']");
    private static final By TOAST_EXITO = By.cssSelector(".oxd-toast--success");

    public LeaveTypesPage(WebDriver driver) {
        super(driver);
    }

    public void abrir() {
        driver.get(BASE_URL + "/leave/leaveTypeList");
        esperarSinSpinner();
        waitVisible(TITULO);
    }

    public boolean existeTipo(String nombreTipo) {
        By filaConNombre = By.xpath(
            "//div[contains(@class,'oxd-table-body')]" +
            "//div[contains(@class,'oxd-table-row')]" +
            "[.//*[normalize-space(text())='" + nombreTipo + "']]");
        boolean existe = !driver.findElements(filaConNombre).isEmpty();
        System.out.println("ENTITLEMENTS: ¿Existe '" + nombreTipo + "'? -> " + existe);
        return existe;
    }

    public void crearTipo(String nombreTipo) {
        WebElement btnAdd = waitClickable(BTN_ADD);
        btnAdd.click();

        try {
            new WebDriverWait(driver, Duration.ofSeconds(10)).until(
                d -> d.getCurrentUrl().contains("defineLeaveType"));
        } catch (Exception eNav) {
            clickWithJs(driver.findElement(BTN_ADD));
            new WebDriverWait(driver, Duration.ofSeconds(10)).until(
                d -> d.getCurrentUrl().contains("defineLeaveType"));
        }
        esperarSinSpinner();

        WebElement campoNombre = waitVisible(INPUT_NAME);
        campoNombre.click();
        campoNombre.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.DELETE);
        campoNombre.sendKeys(nombreTipo);

        marcarRadioSituationalYes();
        waitClickable(BTN_SAVE).click();
        esperarSinSpinner();

        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(TOAST_EXITO));
            System.out.println("ENTITLEMENTS: Leave Type '" + nombreTipo + "' creado correctamente.");
        } catch (Exception e) {
            System.out.println("ENTITLEMENTS: Sin toast de éxito al crear '" + nombreTipo
                + "', verificar manualmente.");
        }
    }

    private void marcarRadioSituationalYes() {
        try {
            List<WebElement> radios = driver.findElements(By.cssSelector("input[type='radio']"));
            if (!radios.isEmpty()) {
                clickWithJs(radios.get(0));
                System.out.println("ENTITLEMENTS: Radio 'Yes' (Situational) marcado.");
            }
        } catch (Exception e) {
            System.out.println("ENTITLEMENTS: No se pudo marcar 'Yes' en Situational -> "
                + e.getMessage());
        }
    }

    public void asegurarTipoExiste(String nombreTipo) {
        if (existeTipo(nombreTipo)) {
            System.out.println("ENTITLEMENTS: Leave Type '" + nombreTipo
                + "' ya existe, no se crea de nuevo.");
        } else {
            System.out.println("ENTITLEMENTS: Leave Type '" + nombreTipo
                + "' no existe, creando...");
            crearTipo(nombreTipo);
        }
    }
}
