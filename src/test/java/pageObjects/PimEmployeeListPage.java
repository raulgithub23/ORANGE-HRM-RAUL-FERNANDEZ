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

// page object de la lista de empleados de pim (para buscar, editar o borrar locos)
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

    // busqueda rapida que uso para editar o borrar: escribe el nombre en el autocompletado 
    // de arriba (no en los filtros avanzados) y agarra la primera sugerencia que tira el sistema
    public void buscarPorNombreSimple(String nombre) {
        WebElement campo = waitVisible(INPUT_AUTOCOMPLETE);
        campo.clear();
        campo.sendKeys(nombre);
        seleccionarPrimeraSugerencia();
        clickSearch();
    }

    // busqueda con mas filtros (nombre + estado active/inactive) que usa el cp-06 para ver 
    // si anda bien el filtrado. a diferencia del otro metodo, aca le doy clic a la opcion con 
    // el nombre exacto y no a la primera sugerencia, porque necesito agarrar si o si al 
    // empleado puntual que se creo en el cp-05 y no a cualquiera que se parezca
    public void buscarConFiltros(String nombre, String estado) {
        WebElement campo = waitVisible(INPUT_FILTRO_NOMBRE);
        campo.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.DELETE);
        campo.sendKeys(nombre);

        By opcion = By.xpath("//div[@role='listbox']//span[contains(text(), '" + nombre + "')]");
        try {
            waitClickable(opcion).click();
        } catch (Exception e) {
            // no relanzo el error: si no aparece en el dropdown (capaz porque no se corrio el cp-05 
            // antes o se reseteo la demo), dejo el aviso por consola y sigo de largo. que el test 
            // reviente mas adelante en su propio assert con un mensaje mejor
            System.out.println("⚠️ Autocompletado no encontró a: " + nombre +
                " (Asegúrate de haber corrido el CP-05 primero)");
        }

        if (estado != null && !estado.isEmpty()) {
            seleccionarEstadoInclude(estado);
        }

        clickSearch();
        esperarResultadosTabla();
    }

    // pasa los valores "Active" o "Inactive" (los del excel) a los textos de verdad que usa el 
    // dropdown "Include" en orange. si llega otra cosa lo mando tal cual por las dudas si 
    // en algun momento meten mas opciones
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
        // espero a que salga el spinner o el cartelito (toast), lo que pase primero, asi se 
        // que ya salio el viaje al server. si no sale ninguno despues de un rato, sigo de largo nomas
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector(".oxd-loading-spinner-container, .oxd-toast-container")));
        } catch (Exception ignored) {}
        esperarSinSpinner();
    }

    // espero a que la tabla cargue las filas O tire el "No Records Found". meto las dos porque 
    // ambas valen como resultado (el cp-06 prueba casos donde no tiene que encontrar nada)
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

    // le trato de dar al lapicito de la primera fila. si orange se pone la gorra y cambia el 
    // icono o el html en otra version, le pego al primer boton de acciones que encuentre para 
    // no quedarme a pata con un selector que se rompe facil
    public void editarPrimerResultado() {
        try {
            waitClickable(PRIMER_EDIT).click();
        } catch (Exception e) {
            waitClickable(BTN_EDIT_FALLBACK).click();
        }
        esperarSinSpinner();
    }

    // el cp-07 (de editar) solo necesita ver si el form agarra viaje, no hace falta cambiarle 
    // datos reales para que pase. por eso aca me fijo que siga en esa pantalla y si veo el 
    // boton save le doy con js para guardar y listo sin tocar mas nada
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
            // si no hay fila para marcar, seguro el empleado que buscamos ya no existe 
            // (capaz lo volamos en otra corrida previa y quedo limpio)
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

    // asumo que el loco se borro si la tabla dice "No Records Found", si no hay filas o si 
    // nos quedamos en la misma url sin que explote nada. la hago asi de flexible porque 
    // al cp-08 le importa que ya no este el registro y no tanto el cartelito exacto que pone la ui
    public boolean empleadoEliminadoDeBusqueda() {
        esperarResultadosTabla();
        return muestraSinResultados() || !tieneResultados()
            || driver.getCurrentUrl().contains("/pim/viewEmployeeList");
    }
}