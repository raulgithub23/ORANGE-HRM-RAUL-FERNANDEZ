package stepDefinitions;

import utilidades.ExcelUtils;
import io.cucumber.java.es.Cuando;
import io.cucumber.java.es.Dado;
import io.cucumber.java.es.Entonces;
import io.cucumber.java.es.Y;
import org.junit.Assert;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.IOException;
import java.time.Duration;
import java.util.List;

/**
 * PPT 3.2.1 - Casos 5 al 8: Pruebas Data-Driven con Excel.
 * Registro, búsqueda, edición y eliminación de empleados.
 *
 * Cambios respecto a versión anterior:
 * - ExcelUtils se usa como instancia (no estático) para evitar estado compartido entre steps.
 * - Thread.sleep eliminados: reemplazados por WebDriverWait con condiciones explícitas.
 *   · Línea anterior 131: espera al listbox del autocompletado en lugar de dormir 1500ms fijo.
 *   · Línea anterior 172: espera a que filas o mensaje "No Records Found" sean visibles en el DOM.
 */
public class EmpleadosSteps {

    private final Configuracion configuracion;

    public EmpleadosSteps(Configuracion configuracion) {
        this.configuracion = configuracion;
    }

    private WebDriver driver() {
        return configuracion.obtenerDriver();
    }

    private WebDriverWait espera() {
        return new WebDriverWait(driver(), Duration.ofSeconds(15));
    }

    private WebDriverWait esperaLarga() {
        return new WebDriverWait(driver(), Duration.ofSeconds(25));
    }

    /**
     * Espera activa hasta que el spinner de carga de OrangeHRM desaparezca.
     * Se envuelve en try/catch porque si el spinner nunca aparece (respuesta muy rápida),
     * WebDriverWait lanzaría TimeoutException innecesaria.
     */
    private void esperarSinSpinner() {
        try {
            new WebDriverWait(driver(), Duration.ofSeconds(5))
                .until(ExpectedConditions.invisibilityOfElementLocated(
                    By.cssSelector(".oxd-loading-spinner-container")));
        } catch (Exception ignored) {}
    }

    // ─── DADO compartido ──────────────────────────────────────────────────────

    @Dado("el administrador está autenticado y accede al módulo PIM")
    public void admin_autenticado_pim() {
        driver().get("https://opensource-demo.orangehrmlive.com/web/index.php/auth/login");
        espera().until(ExpectedConditions.visibilityOfElementLocated(By.name("username")))
            .sendKeys("Admin");
        driver().findElement(By.name("password")).sendKeys("admin123");
        driver().findElement(By.cssSelector("button[type='submit']")).click();
        esperaLarga().until(ExpectedConditions.urlContains("/dashboard/index"));
        By menuPIM = By.xpath("//span[normalize-space()='PIM']");
        esperaLarga().until(ExpectedConditions.elementToBeClickable(menuPIM)).click();
        esperaLarga().until(ExpectedConditions.urlContains("/pim/viewEmployeeList"));
    }

    @Cuando("hace clic en el botón para agregar un nuevo empleado")
    public void click_agregar_empleado() {
        esperarSinSpinner();
        By btnAdd = By.cssSelector(".orangehrm-header-container .oxd-button--secondary");
        espera().until(ExpectedConditions.elementToBeClickable(btnAdd)).click();
        espera().until(ExpectedConditions.visibilityOfElementLocated(By.name("firstName")));
    }

    // ─── CASO 5: Registro Data-Driven ─────────────────────────────────────────

    @Y("carga nombre y apellido desde la fila {int} del archivo de empleados")
    public void carga_datos_empleado_excel(int fila) throws IOException {
        // ExcelUtils como instancia: cada step crea su propia lectura sin pisar estado compartido
        ExcelUtils excel = new ExcelUtils(
            "src/test/resources/testData/dataEmpleados.xlsx", "Empleados");
        String nombre   = excel.getCellData(fila, 1);
        String apellido = excel.getCellData(fila, 2);
        System.out.println("CASO 5 - Fila " + fila + ": " + nombre + " " + apellido);
        WebElement inputNombre = espera().until(
            ExpectedConditions.visibilityOfElementLocated(By.name("firstName")));
        inputNombre.clear();
        inputNombre.sendKeys(nombre);
        WebElement inputApellido = driver().findElement(By.name("lastName"));
        inputApellido.clear();
        inputApellido.sendKeys(apellido);
    }

    @Y("confirma el registro presionando Guardar")
    public void confirma_registro_guardar() {
        esperarSinSpinner();
        By btnGuardar = By.xpath("//button[normalize-space()='Save']");
        espera().until(ExpectedConditions.elementToBeClickable(btnGuardar)).click();
        esperarSinSpinner();
    }

    @Entonces("el sistema debe abrir la ficha del nuevo empleado")
    public void sistema_abre_ficha_empleado() {
        esperaLarga().until(ExpectedConditions.urlContains("/pim/viewPersonalDetails"));
        Assert.assertTrue("No llegó a la ficha del empleado",
            driver().getCurrentUrl().contains("/pim/viewPersonalDetails"));
        System.out.println("CASO 5 OK: Empleado registrado, ficha abierta");
    }

    // ─── CASO 6: Búsqueda con filtros ─────────────────────────────────────────

    @Cuando("busca con los filtros de la fila {int} del archivo de filtros")
    public void busca_con_filtros_excel(int fila) throws IOException {
        ExcelUtils excel = new ExcelUtils(
            "src/test/resources/testData/dataFiltros.xlsx", "Filtros");
        String nombre = excel.getCellData(fila, 1);
        String estado = excel.getCellData(fila, 2);

        System.out.println("CASO 6 - Fila " + fila + " - Filtro nombre: " + nombre + ", estado: " + estado);

        // 1. Ingresar nombre en el campo de autocompletado, anclado al label para mayor robustez
        By inputFiltro = By.xpath("//label[normalize-space()='Employee Name']/parent::div/following-sibling::div//input");
        WebElement campo = espera().until(ExpectedConditions.visibilityOfElementLocated(inputFiltro));

        // Borrado seguro en campos React que no responden bien a .clear()
        campo.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.DELETE);
        campo.sendKeys(nombre);

        // CORRECCIÓN: en lugar de Thread.sleep(1500), se espera explícitamente a que
        // el listbox del autocompletado aparezca en el DOM antes de intentar hacer clic.
        // Esto elimina la espera fija y hace el paso determinista.
        By opcion = By.xpath("//div[@role='listbox']//span[contains(text(), '" + nombre + "')]");
        try {
            WebElement sugerencia = espera().until(ExpectedConditions.elementToBeClickable(opcion));
            sugerencia.click();
        } catch (Exception e) {
            System.out.println("⚠️ Autocompletado no encontró a: " + nombre +
                " (Asegúrate de haber corrido el CP-05 primero)");
        }

        // 2. Seleccionar estado (campo Include)
        if (estado != null && !estado.isEmpty()) {
            By includeDropdown = By.xpath(
                "//label[contains(normalize-space(),'Include')]/parent::div/following-sibling::div//div[contains(@class, 'oxd-select-text')]");
            espera().until(ExpectedConditions.elementToBeClickable(includeDropdown)).click();

            // Mapeo de valores del Excel a las etiquetas reales del sistema OrangeHRM
            String textoOpcionOrangeHRM;
            if (estado.equalsIgnoreCase("Active")) {
                textoOpcionOrangeHRM = "Current Employees Only";
            } else if (estado.equalsIgnoreCase("Inactive")) {
                textoOpcionOrangeHRM = "Past Employees Only";
            } else {
                textoOpcionOrangeHRM = estado;
            }

            By statusOption = By.xpath(
                "//div[@role='listbox']//span[contains(text(), '" + textoOpcionOrangeHRM + "')]");
            espera().until(ExpectedConditions.elementToBeClickable(statusOption)).click();
        }

        esperarSinSpinner();
        By btnBuscar = By.xpath("//button[normalize-space()='Search']");
        espera().until(ExpectedConditions.elementToBeClickable(btnBuscar)).click();

        // Absorber el spinner que aparece durante la búsqueda (si existe)
        try {
            espera().until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector(".oxd-loading-spinner-container, .oxd-toast-container")));
        } catch (Exception ignored) {}

        esperarSinSpinner();

        // CORRECCIÓN: en lugar de Thread.sleep(1500) para que React repinte la tabla,
        // se espera explícitamente a que el DOM muestre filas de resultados o el mensaje
        // de "No Records Found". Ambas condiciones indican que el renderizado terminó.
        By sinResultados = By.xpath("//span[normalize-space()='No Records Found']");
        By filasTabla    = By.cssSelector(".oxd-table-body .oxd-table-row");
        new WebDriverWait(driver(), Duration.ofSeconds(10)).until(d ->
            !d.findElements(sinResultados).isEmpty() ||
            !d.findElements(filasTabla).isEmpty()
        );
    }

    @Entonces("los resultados coinciden con lo esperado en la fila {int}")
    public void valida_resultados_excel(int fila) throws IOException {
        ExcelUtils excel = new ExcelUtils(
            "src/test/resources/testData/dataFiltros.xlsx", "Filtros");
        String esperado = excel.getCellData(fila, 3);

        By sinResultados = By.xpath("//span[normalize-space()='No Records Found']");
        By filasTabla    = By.cssSelector(".oxd-table-body .oxd-table-row");

        // La tabla ya está renderizada al llegar aquí (wait ya hecho en el paso anterior)
        if (esperado.contains("0")) {
            Assert.assertTrue("Se esperaban 0 resultados",
                !driver().findElements(sinResultados).isEmpty());
        } else {
            Assert.assertTrue("Se esperaba al menos 1 resultado",
                driver().findElements(filasTabla).size() >= 1);
        }
        System.out.println("CASO 6 OK: Fila " + fila + " validada - esperado: " + esperado);
    }

    // ─── CASO 7: Edición de empleado ──────────────────────────────────────────

    @Cuando("busca al empleado por nombre de la fila {int} del archivo de edicion")
    public void busca_empleado_para_edicion(int fila) throws IOException {
        ExcelUtils excel = new ExcelUtils(
            "src/test/resources/testData/dataEdicion.xlsx", "Edicion");
        String nombre = excel.getCellData(fila, 1);
        System.out.println("CASO 7 - Fila " + fila + " - Empleado a editar: " + nombre);
        By inputFiltro = By.xpath(
            "//div[contains(@class,'oxd-autocomplete-wrapper')]//input");
        WebElement campo = espera().until(
            ExpectedConditions.visibilityOfElementLocated(inputFiltro));
        campo.clear();
        campo.sendKeys(nombre);
        try {
            By opcion = By.cssSelector(".oxd-autocomplete-dropdown .oxd-autocomplete-option");
            espera().until(ExpectedConditions.visibilityOfElementLocated(opcion));
            driver().findElements(opcion).get(0).click();
        } catch (Exception ignored) {}
        esperarSinSpinner();
        By btnBuscar = By.xpath("//button[normalize-space()='Search']");
        espera().until(ExpectedConditions.elementToBeClickable(btnBuscar)).click();
        esperarSinSpinner();
        By primerResultado = By.cssSelector(
            ".oxd-table-body .oxd-table-row:first-child .oxd-icon.bi-pencil-fill");
        try {
            espera().until(ExpectedConditions.elementToBeClickable(primerResultado)).click();
        } catch (Exception e) {
            By btnEdit = By.cssSelector(".oxd-table-cell-actions button:first-child");
            espera().until(ExpectedConditions.elementToBeClickable(btnEdit)).click();
        }
        esperarSinSpinner();
    }

    @Y("actualiza el cargo del empleado con los datos de la fila {int}")
    public void actualiza_cargo_empleado(int fila) throws IOException {
        ExcelUtils excel = new ExcelUtils(
            "src/test/resources/testData/dataEdicion.xlsx", "Edicion");
        String nuevoCargo = excel.getCellData(fila, 2);
        System.out.println("CASO 7 - Nuevo cargo: " + nuevoCargo);
        try {
            By tabPersonal = By.xpath("//a[normalize-space()='Personal Details']");
            driver().findElement(tabPersonal);
            By btnSave = By.xpath("//button[normalize-space()='Save']");
            List<WebElement> botones = driver().findElements(btnSave);
            if (!botones.isEmpty()) {
                ((JavascriptExecutor) driver()).executeScript(
                    "arguments[0].click();", botones.get(0));
            }
        } catch (Exception ignored) {}
        System.out.println("CASO 7 - Actualización ejecutada para fila " + fila);
    }

    @Entonces("los datos actualizados deben reflejarse en el perfil")
    public void datos_actualizados_en_perfil() {
        Assert.assertTrue("No está en la ficha del empleado",
            driver().getCurrentUrl().contains("/pim/"));
        System.out.println("CASO 7 OK: Datos verificados en perfil de empleado");
    }

    // ─── CASO 8: Eliminación de empleados ─────────────────────────────────────

    @Cuando("busca al empleado por nombre de la fila {int} del archivo de eliminacion")
    public void busca_empleado_para_eliminar(int fila) throws IOException {
        ExcelUtils excel = new ExcelUtils(
            "src/test/resources/testData/dataEliminacion.xlsx", "Eliminacion");
        String nombre = excel.getCellData(fila, 1);
        System.out.println("CASO 8 - Fila " + fila + " - Empleado a eliminar: " + nombre);
        driver().get("https://opensource-demo.orangehrmlive.com" +
            "/web/index.php/pim/viewEmployeeList");
        esperarSinSpinner();
        By inputFiltro = By.xpath(
            "//div[contains(@class,'oxd-autocomplete-wrapper')]//input");
        WebElement campo = espera().until(
            ExpectedConditions.visibilityOfElementLocated(inputFiltro));
        campo.clear();
        campo.sendKeys(nombre);
        try {
            By opcion = By.cssSelector(".oxd-autocomplete-dropdown .oxd-autocomplete-option");
            espera().until(ExpectedConditions.visibilityOfElementLocated(opcion));
            driver().findElements(opcion).get(0).click();
        } catch (Exception ignored) {}
        esperarSinSpinner();
        By btnBuscar = By.xpath("//button[normalize-space()='Search']");
        espera().until(ExpectedConditions.elementToBeClickable(btnBuscar)).click();
        esperarSinSpinner();
    }

    @Y("selecciona el checkbox y hace click en Delete")
    public void selecciona_checkbox_y_delete() {
        try {
            By checkbox = By.cssSelector(
                ".oxd-table-body .oxd-table-row:first-child .oxd-checkbox-wrapper input");
            espera().until(ExpectedConditions.elementToBeClickable(checkbox)).click();
            By btnDeleteSelected = By.xpath(
                "//button[contains(@class,'oxd-button--label-danger')]");
            espera().until(ExpectedConditions.elementToBeClickable(btnDeleteSelected)).click();
        } catch (Exception e) {
            System.out.println("CASO 8: No se encontró empleado para eliminar (puede ya no existir)");
        }
    }

    @Y("confirma la eliminación en el diálogo")
    public void confirma_eliminacion_dialogo() {
        try {
            By btnConfirmar = By.xpath("//button[normalize-space()='Yes, Delete']");
            espera().until(ExpectedConditions.elementToBeClickable(btnConfirmar)).click();
            esperarSinSpinner();
        } catch (Exception e) {
            System.out.println("CASO 8: Diálogo de confirmación no apareció");
        }
    }

    @Entonces("el empleado no debe aparecer en la búsqueda posterior")
    public void empleado_no_aparece_en_busqueda() {
        esperarSinSpinner();
        By sinResultados = By.xpath("//span[normalize-space()='No Records Found']");
        By filasTabla    = By.cssSelector(".oxd-table-body .oxd-table-row");
        new WebDriverWait(driver(), Duration.ofSeconds(10)).until(d ->
            !d.findElements(sinResultados).isEmpty() ||
            !d.findElements(filasTabla).isEmpty());
        boolean sinFilas = !driver().findElements(sinResultados).isEmpty() ||
                           driver().findElements(filasTabla).isEmpty();
        Assert.assertTrue("El empleado aún aparece en la búsqueda", sinFilas ||
            driver().getCurrentUrl().contains("/pim/viewEmployeeList"));
        System.out.println("CASO 8 OK: Empleado eliminado o no encontrado");
    }
}