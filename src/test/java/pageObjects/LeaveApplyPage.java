package pageObjects;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

/**
 * Page Object del formulario Apply Leave (solicitud de licencias).
 */
public class LeaveApplyPage extends BasePage {

    private static final By SELECT_LEAVE_TYPE = By.xpath(
        "//label[contains(text(),'Leave Type')]/parent::div/following-sibling::div" +
        "//div[contains(@class,'oxd-select-text')]");
    private static final By INPUT_FROM_DATE = By.xpath(
        "//label[normalize-space()='From Date']/following::input[1]");
    private static final By INPUT_TO_DATE = By.xpath(
        "//label[normalize-space()='To Date']/following::input[1]");
    private static final By BTN_APPLY = By.xpath("//button[normalize-space()='Apply']");
    private static final By FORM = By.cssSelector(".oxd-form");
    private static final By DROPDOWN = By.cssSelector("div[role='listbox']");
    private static final By ERRORES_VALIDACION = By.cssSelector(".oxd-input-field-error-message");
    private static final By ALERTA = By.cssSelector(".oxd-alert-content-text");
    private static final By TOAST_EXITO = By.cssSelector(".oxd-toast--success");

    public LeaveApplyPage(WebDriver driver) {
        super(driver, 15, 30);
    }

    public void abrir() {
        driver.get(BASE_URL + "/leave/applyLeave");
        esperarSinSpinner();
        waitVisible(By.xpath(
            "//label[normalize-space()='Leave Type']" +
            "/following::div[contains(@class,'oxd-select-wrapper')][1]"));
    }

    public void seleccionarTipoLicencia(String tipo) {
        esperarSinSpinner();
        for (int intento = 0; intento < 3; intento++) {
            try {
                waitClickable(SELECT_LEAVE_TYPE).click();
                wait.until(ExpectedConditions.visibilityOfElementLocated(DROPDOWN));

                By opcionLocator = By.xpath(
                    "//div[@role='listbox']//span[contains(text(),'" + tipo + "')]");
                WebElement opcion = wait.until(
                    ExpectedConditions.presenceOfElementLocated(opcionLocator));
                scrollIntoView(opcion);
                waitClickable(opcionLocator).click();
                wait.until(ExpectedConditions.invisibilityOfElementLocated(DROPDOWN));
                esperarLeaveBalanceCargado();
                return;
            } catch (Exception e) {
                if (intento == 2) {
                    throw new RuntimeException("No se pudo seleccionar la licencia: " + tipo, e);
                }
                try { driver.findElement(By.cssSelector("body")).click(); } catch (Exception ignored) {}
                esperarSinSpinner();
            }
        }
    }

    public void ingresarFechas(String desde, String hasta) {
        escribirFecha(INPUT_FROM_DATE, desde);
        driver.findElement(FORM).click();
        esperarSinSpinner();
        escribirFecha(INPUT_TO_DATE, hasta);
        driver.findElement(FORM).click();
        esperarSinSpinner();
    }

    private void escribirFecha(By locator, String fecha) {
        WebElement input = waitVisible(locator);
        input.click();
        ((JavascriptExecutor) driver).executeScript("arguments[0].value = '';", input);
        input.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.DELETE);
        input.sendKeys(fecha);
        input.sendKeys(Keys.TAB);
    }

    private void esperarLeaveBalanceCargado() {
        try {
            esperarSinSpinner();
            WebDriverWait espera10s = new WebDriverWait(driver, Duration.ofSeconds(10));
            espera10s.until(d -> {
                try {
                    WebElement balanceArea = d.findElement(
                        By.cssSelector(".oxd-form-row .oxd-input-group"));
                    String texto = balanceArea.getText().trim();
                    return !texto.isEmpty() && !texto.equals("Leave Balance");
                } catch (Exception ex) {
                    return false;
                }
            });
            System.out.println("CASO 14: Leave Balance cargado correctamente.");
        } catch (Exception e) {
            System.out.println("CASO 14: Timeout esperando Leave Balance, usando pausa de 2s.");
            pausa(2000);
        }
    }

    public void aplicar() {
        esperarSinSpinner();
        waitClickable(BTN_APPLY).click();
    }

    public List<WebElement> obtenerErroresValidacion() {
        return driver.findElements(ERRORES_VALIDACION);
    }

    public List<WebElement> obtenerAlertas() {
        return driver.findElements(ALERTA);
    }

    public void esperarToastExito() {
        longWait.until(ExpectedConditions.visibilityOfElementLocated(TOAST_EXITO));
    }
}
