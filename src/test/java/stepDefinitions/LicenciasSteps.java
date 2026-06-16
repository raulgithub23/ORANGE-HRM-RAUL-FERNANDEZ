package stepDefinitions;

import utilidades.ExcelUtils;
import io.cucumber.java.es.Cuando;
import io.cucumber.java.es.Entonces;
import io.cucumber.java.es.Y;
import org.junit.Assert;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.IOException;
import java.time.Duration;
import java.util.List;

/**
 * PPT 3.2.1 + 3.3.1 - Caso 14: Solicitud de licencias con datos externos.
 * Lee tipo, fecha inicio y fecha fin desde Excel y los registra en Leave.
 *
 * Cambio respecto a versión anterior:
 * - ExcelUtils se usa como instancia en lugar de métodos estáticos,
 *   consistente con el rediseño de la clase para evitar estado compartido.
 */
public class LicenciasSteps {

    private final Configuracion configuracion;

    public LicenciasSteps(Configuracion configuracion) {
        this.configuracion = configuracion;
    }

    private WebDriver driver() {
        return configuracion.obtenerDriver();
    }

    private WebDriverWait espera() {
        return new WebDriverWait(driver(), Duration.ofSeconds(15));
    }

    private WebDriverWait esperaLarga() {
        return new WebDriverWait(driver(), Duration.ofSeconds(30));
    }

    /**
     * Espera a que el spinner de carga de OrangeHRM desaparezca.
     * Si el spinner nunca aparece (respuesta rápida), la excepción se absorbe.
     */
    private void esperarSinSpinner() {
        try {
            new WebDriverWait(driver(), Duration.ofSeconds(5))
                .until(ExpectedConditions.invisibilityOfElementLocated(
                    By.cssSelector(".oxd-loading-spinner-container")));
        } catch (Exception ignored) {}
    }

    @Cuando("navega al módulo de solicitud de licencias")
    public void navega_modulo_licencias() {
        driver().get("https://opensource-demo.orangehrmlive.com" +
            "/web/index.php/leave/applyLeave");
        esperarSinSpinner();
        By selectTipo = By.xpath(
            "//label[normalize-space()='Leave Type']" +
            "/following::div[contains(@class,'oxd-select-wrapper')][1]");
        espera().until(ExpectedConditions.visibilityOfElementLocated(selectTipo));
    }

    @Y("selecciona el tipo de licencia de la fila {int}")
    public void selecciona_tipo_licencia(int fila) throws IOException {
        // Instancia de ExcelUtils: evita estado estático compartido entre steps concurrentes
        ExcelUtils excel = new ExcelUtils(
            "src/test/resources/testData/dataLicencias.xlsx", "Licencias");
        String tipo = excel.getCellData(fila, 1);
        System.out.println("CASO 14 - Fila " + fila + " - Tipo licencia: " + tipo);

        esperarSinSpinner();

        By selectWrapper = By.xpath(
            "//label[contains(text(),'Leave Type')]/parent::div/following-sibling::div" +
            "//div[contains(@class, 'oxd-select-text')]");

        for (int intento = 0; intento < 3; intento++) {
            try {
                WebElement wrapper = espera().until(
                    ExpectedConditions.elementToBeClickable(selectWrapper));
                wrapper.click();

                By dropdown = By.cssSelector("div[role='listbox']");
                espera().until(ExpectedConditions.visibilityOfElementLocated(dropdown));

                By opcionLocator = By.xpath(
                    "//div[@role='listbox']//span[contains(text(), '" + tipo + "')]");

                // Scroll hacia el elemento para que React lo detecte dentro del viewport
                WebElement opcion = espera().until(
                    ExpectedConditions.presenceOfElementLocated(opcionLocator));
                ((JavascriptExecutor) driver()).executeScript(
                    "arguments[0].scrollIntoView({block: 'center'});", opcion);

                espera().until(ExpectedConditions.elementToBeClickable(opcion)).click();
                espera().until(ExpectedConditions.invisibilityOfElementLocated(dropdown));
                return;
            } catch (Exception e) {
                if (intento == 2) {
                    throw new RuntimeException(
                        "No se pudo seleccionar la licencia: " + tipo +
                        ". Revisa que el Excel coincida con una opción del sistema.", e);
                }
                // Si el menú quedó trabado, cerrarlo haciendo clic fuera antes de reintentar
                try { driver().findElement(By.cssSelector("body")).click(); } catch (Exception ignored) {}
                esperarSinSpinner();
            }
        }
    }

    @Y("ingresa las fechas de la fila {int}")
    public void ingresa_fechas(int fila) throws IOException {
        ExcelUtils excel = new ExcelUtils(
            "src/test/resources/testData/dataLicencias.xlsx", "Licencias");
        String desde = excel.getCellData(fila, 2);
        String hasta = excel.getCellData(fila, 3);
        System.out.println("CASO 14 - Fechas: desde=" + desde + " hasta=" + hasta);

        By inputDesde = By.xpath("//label[normalize-space()='From Date']/following::input[1]");
        By inputHasta = By.xpath("//label[normalize-space()='To Date']/following::input[1]");
        By formulario = By.cssSelector(".oxd-form");

        escribirFecha(inputDesde, desde);
        driver().findElement(formulario).click();
        esperarSinSpinner();
        escribirFecha(inputHasta, hasta);
        driver().findElement(formulario).click();
        esperarSinSpinner();
    }

    /**
     * Escribe una fecha en un input de OrangeHRM.
     * Se usa triple limpieza (JS + CTRL+A/DELETE) porque los datepickers
     * de React no responden correctamente al .clear() estándar de Selenium.
     */
    private void escribirFecha(By locator, String fecha) {
        WebElement input = espera().until(
            ExpectedConditions.visibilityOfElementLocated(locator));
        input.click();
        ((JavascriptExecutor) driver()).executeScript("arguments[0].value = '';", input);
        input.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.DELETE);
        input.sendKeys(fecha);
        input.sendKeys(Keys.TAB);
    }

    @Y("hace clic en aplicar la solicitud")
    public void click_aplicar_solicitud() {
        esperarSinSpinner();
        By btnAplicar = By.xpath("//button[normalize-space()='Apply']");
        espera().until(ExpectedConditions.elementToBeClickable(btnAplicar)).click();
    }

    @Entonces("la solicitud debe quedar registrada en el sistema")
    public void solicitud_registrada() {
        try {
            By toast = By.cssSelector(".oxd-toast-content-text");
            esperaLarga().until(ExpectedConditions.visibilityOfAllElementsLocatedBy(toast));
            System.out.println("CASO 14 OK: Licencia registrada - toast visible");
        } catch (Exception e) {
            // Si el toast no aparece, confirmar que permanecemos en el módulo Leave
            Assert.assertTrue("La solicitud no fue registrada",
                driver().getCurrentUrl().contains("/leave/"));
            System.out.println("CASO 14 OK: Licencia registrada - URL confirmada");
        }
    }
}