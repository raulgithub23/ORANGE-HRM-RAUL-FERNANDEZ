package pageObjects;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

// page object de leave types (para configurar los tipos de licencia)
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

    // crea un tipo de licencia de cero. lo uso cuando existetipo() avisa que la licencia para el 
    // cp-14 ya no esta en la demo publica (como la resetean a cada rato te borran todo). le meti 
    // un intento con js por las dudas si el boton add queda tapado por algun cartelito o tooltip 
    // y selenium no le puede hacer clic normal
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
            // no lo hago fallar aca porque el cartel verde (toast) es nomas visual y si no sale 
            // no significa que fallo todo (capaz se fue muy rapido). el step que llama a esto despues 
            // se encarga de ver si todo el flujo anduvo bien o no
            System.out.println("ENTITLEMENTS: Sin toast de éxito al crear '" + nombreTipo
                + "', verificar manualmente.");
        }
    }

    // marca el primer radio button de la pantalla de define leave type, que es el de situational = yes. 
    // lo hago por posicion (el primero) y no por texto porque el html de orange es re mañoso y no le 
    // pone un id o label directo al input, solo al texto que se ve al lado
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

    // punto de entrada salvador: si la licencia ya existe no hace nada (asi evitamos duplicados y que 
    // el back tire error), y si no esta la crea. con esto zafamos y la suite anda de diez tanto 
    // en una demo recien limpiada como en una que ya corrimos antes
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