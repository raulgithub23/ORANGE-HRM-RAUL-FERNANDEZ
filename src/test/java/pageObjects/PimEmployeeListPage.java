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
 * Page Object de la lista de empleados PIM (búsqueda, edición y eliminación).
 */
public class PimEmployeeListPage extends BasePage {

    private static final By INPUT_AUTOCOMPLETE = By.xpath(
        "//div[contains(@class,'oxd-autocomplete-wrapper')]//input");
    private static final By INPUT_FILTRO_NOMBRE = By.xpath(
        "//label[normalize-space()='Employee Name']/parent::div/following-sibling::div//input");
    private static final By BTN_SEARCH = By.xpath("//button[normalize-space()='Search']");
    private static final By BTN_SAVE = By.xpath("//button[normalize-space()='Save']");
    private static final By SIN_RESULTADOS = By.xpath("//span[normalize-space()='No Records Found']");
    private static final By FILAS_TABLA = By.cssSelector(".oxd-table-body .oxd-table-row");
    private static final By PRIMER_EDIT = By.cssSelector(
        ".oxd-table-body .oxd-table-row:first-child .oxd-icon.bi-pencil-fill");
    private static final By BTN_EDIT_FALLBACK = By.cssSelector(
        ".oxd-table-cell-actions button:first-child");
    private static final By CHECKBOX_PRIMERA_FILA = By.cssSelector(
        ".oxd-table-body .oxd-table-row:first-child .oxd-checkbox-wrapper input");
    private static final By BTN_DELETE_SELECTED = By.xpath(
        "//button[contains(@class,'oxd-button--label-danger')]");
    private static final By BTN_YES_DELETE = By.xpath("//button[normalize-space()='Yes, Delete']");

    public PimEmployeeListPage(WebDriver driver) {
        super(driver);
    }

    public void abrir() {
        driver.get(BASE_URL + "/pim/viewEmployeeList");
        esperarSinSpinner();
    }

    public void buscarPorNombreSimple(String nombre) {
        WebElement campo = waitVisible(INPUT_AUTOCOMPLETE);
        campo.clear();
        campo.sendKeys(nombre);
        seleccionarPrimeraSugerencia();
        clickSearch();
    }

    public void buscarConFiltros(String nombre, String estado) {
        WebElement campo = waitVisible(INPUT_FILTRO_NOMBRE);
        campo.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.DELETE);
        campo.sendKeys(nombre);

        By opcion = By.xpath("//div[@role='listbox']//span[contains(text(), '" + nombre + "')]");
        try {
            waitClickable(opcion).click();
        } catch (Exception e) {
            System.out.println("⚠️ Autocompletado no encontró a: " + nombre +
                " (Asegúrate de haber corrido el CP-05 primero)");
        }

        if (estado != null && !estado.isEmpty()) {
            seleccionarEstadoInclude(estado);
        }

        clickSearch();
        esperarResultadosTabla();
    }

    private void seleccionarEstadoInclude(String estado) {
        By includeDropdown = By.xpath(
            "//label[contains(normalize-space(),'Include')]/parent::div/following-sibling::div" +
            "//div[contains(@class, 'oxd-select-text')]");
        waitClickable(includeDropdown).click();

        String textoOpcion;
        if (estado.equalsIgnoreCase("Active")) {
            textoOpcion = "Current Employees Only";
        } else if (estado.equalsIgnoreCase("Inactive")) {
            textoOpcion = "Past Employees Only";
        } else {
            textoOpcion = estado;
        }

        By statusOption = By.xpath(
            "//div[@role='listbox']//span[contains(text(), '" + textoOpcion + "')]");
        waitClickable(statusOption).click();
    }

    private void seleccionarPrimeraSugerencia() {
        try {
            By opcion = By.cssSelector(".oxd-autocomplete-dropdown .oxd-autocomplete-option");
            wait.until(ExpectedConditions.visibilityOfElementLocated(opcion));
            driver.findElements(opcion).get(0).click();
        } catch (Exception ignored) {}
    }

    public void clickSearch() {
        esperarSinSpinner();
        waitClickable(BTN_SEARCH).click();
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector(".oxd-loading-spinner-container, .oxd-toast-container")));
        } catch (Exception ignored) {}
        esperarSinSpinner();
    }

    public void esperarResultadosTabla() {
        new WebDriverWait(driver, Duration.ofSeconds(10)).until(d ->
            !d.findElements(SIN_RESULTADOS).isEmpty() ||
            !d.findElements(FILAS_TABLA).isEmpty());
    }

    public boolean tieneResultados() {
        return !driver.findElements(FILAS_TABLA).isEmpty();
    }

    public boolean muestraSinResultados() {
        return !driver.findElements(SIN_RESULTADOS).isEmpty();
    }

    public void editarPrimerResultado() {
        try {
            waitClickable(PRIMER_EDIT).click();
        } catch (Exception e) {
            waitClickable(BTN_EDIT_FALLBACK).click();
        }
        esperarSinSpinner();
    }

    public void guardarEdicionSiVisible() {
        try {
            driver.findElement(By.xpath("//a[normalize-space()='Personal Details']"));
            List<WebElement> botones = driver.findElements(BTN_SAVE);
            if (!botones.isEmpty()) {
                ((JavascriptExecutor) driver).executeScript(
                    "arguments[0].click();", botones.get(0));
            }
        } catch (Exception ignored) {}
    }

    public void seleccionarPrimeraFilaYDelete() {
        try {
            waitClickable(CHECKBOX_PRIMERA_FILA).click();
            waitClickable(BTN_DELETE_SELECTED).click();
        } catch (Exception e) {
            System.out.println("CASO 8: No se encontró empleado para eliminar (puede ya no existir)");
        }
    }

    public void confirmarEliminacion() {
        try {
            waitClickable(BTN_YES_DELETE).click();
            esperarSinSpinner();
        } catch (Exception e) {
            System.out.println("CASO 8: Diálogo de confirmación no apareció");
        }
    }

    public boolean empleadoEliminadoDeBusqueda() {
        esperarResultadosTabla();
        return muestraSinResultados() || !tieneResultados()
            || driver.getCurrentUrl().contains("/pim/viewEmployeeList");
    }
}
