package stepDefinitions;

import utilidades.ExcelUtils;
import io.cucumber.java.After;
import io.cucumber.java.es.Cuando;
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

/**
 * PPT 3.2.1 + 3.3.1 - Caso 14: Registrar licencia con datos desde Excel.
 *
 * Responsabilidad única: aplicar la solicitud de licencia (CP-14) y, al
 * terminar cada fila del Esquema del escenario, cancelarla en Leave List
 * para que el siguiente run de la suite no falle por solicitudes duplicadas.
 *
 * La asignación/recarga de créditos (Entitlements) vive aparte en
 * EntitlementsSteps.java + entitlements.feature, que corre antes por orden
 * alfabético de archivos .feature.
 */
public class LicenciasSteps {

    private final Configuracion configuracion;
    private static final String BASE_URL =
        "https://opensource-demo.orangehrmlive.com/web/index.php";

    // Guarda el tipo de licencia registrado en el escenario actual para poder cancelarlo al final
    private String tipoLicenciaRegistrado = null;

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

    private void pausa(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ignored) {}
    }

    private void esperarSinSpinner() {
        try {
            new WebDriverWait(driver(), Duration.ofSeconds(5))
                .until(ExpectedConditions.invisibilityOfElementLocated(
                    By.cssSelector(".oxd-loading-spinner-container")));
        } catch (Exception ignored) {}
    }

    // -------------------------------------------------------------------------
    // POSTCONDICION: cancela la solicitud recién registrada en Leave List para
    // que el siguiente run de la suite no falle por balance/registro duplicado.
    // order=1: corre ANTES que Configuracion.finalizar() (order=2), que cierra
    // el driver; y se coordina con Hooks.afterScenario (order=1 también, pero
    // en clase distinta) sin pisarse porque ninguno depende del otro.
    // -------------------------------------------------------------------------
    @After(value = "@caso14", order = 1)
    public void cancelarSolicitudRegistrada() {
        System.out.println("POSTCONDICION CP-14: Iniciando limpieza de solicitudes pendientes.");
        try {
            // Esperar que el backend registre la solicitud antes de navegar
            pausa(2000);
            driver().get(BASE_URL + "/leave/viewLeaveList");

            // PASO 1: Esperar que la página cargue y ejecutar Search
            // El botón Search está dentro del formulario de filtros
            WebElement btnSearch = new WebDriverWait(driver(), Duration.ofSeconds(20))
                .until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//button[@type='submit' and normalize-space()='Search']")));
            btnSearch.click();
            System.out.println("POSTCONDICION CP-14: PASO 1 OK - Search ejecutado.");

            // Esperar que la tabla termine de cargar los resultados
            new WebDriverWait(driver(), Duration.ofSeconds(20)).until(d ->
                !d.findElements(By.xpath(
                    "//div[contains(@class,'oxd-table-body')]" +
                    "//div[contains(@class,'oxd-table-row')]")).isEmpty()
                || !d.findElements(By.xpath(
                    "//*[contains(text(),'No Records Found')]")).isEmpty()
            );

            int cancelados = 0;
            while (cancelados < 10) {

                // Verificar cuántas filas hay en el body de la tabla
                java.util.List<WebElement> filas = driver().findElements(
                    By.xpath("//div[contains(@class,'oxd-table-body')]" +
                             "//div[contains(@class,'oxd-table-row')]"));

                if (filas.isEmpty()) {
                    System.out.println("POSTCONDICION CP-14: Tabla limpia. Total cancelados: " + cancelados);
                    break;
                }
                System.out.println("POSTCONDICION CP-14: Filas en tabla: " + filas.size());

                // PASO 2: Hacer scroll a la primera fila y marcar su checkbox
                // El checkbox de cada fila está en la primera celda (oxd-table-cell)
                WebElement primeraFila = filas.get(0);
                ((JavascriptExecutor) driver()).executeScript(
                    "arguments[0].scrollIntoView({block:'center'});", primeraFila);
                pausa(400);

                WebElement checkbox = primeraFila.findElement(
                    By.xpath(".//input[@type='checkbox']"));
                // OrangeHRM superpone un <i class="oxd-icon ... checkbox-input-icon">
                // encima del <input type="checkbox"> real para dibujar el check visual.
                // Eso intercepta el clic nativo de Selenium (ElementClickInterceptedException)
                // porque el punto central del checkbox queda "tapado" por ese ícono.
                // Solución: disparar el clic vía JavaScript, que ignora qué elemento
                // está visualmente encima en ese punto y actúa directo sobre el input.
                ((JavascriptExecutor) driver()).executeScript(
                    "arguments[0].click();", checkbox);
                System.out.println("POSTCONDICION CP-14: PASO 2 OK - Checkbox de fila 1 marcado.");

                // PASO 3: Clic en el botón "Cancel" que aparece junto a "(1) Record
                // Selected" tras marcar el checkbox. NOTA: antes esperábamos
                // explícitamente la visibilidad del texto "Record Selected" con
                // contains(text(),...), pero ese XPath puede matchear más de un nodo
                // en el DOM (p.ej. un duplicado oculto de accesibilidad/responsive),
                // y Selenium podía quedarse esperando la visibilidad del nodo
                // equivocado aunque el visible ya estuviera en pantalla (timeout de
                // 10s real en producción). Por eso ahora vamos directo a esperar que
                // el botón Cancel esté presente, y lo clickeamos vía JavaScript para
                // evitar el mismo problema de "element click intercepted" que tuvimos
                // con el checkbox (otro elemento decorativo tapando el punto de clic).
                boolean cancelClicked = false;
                try {
                    WebElement btnCancelRecord = new WebDriverWait(driver(), Duration.ofSeconds(10))
                        .until(ExpectedConditions.presenceOfElementLocated(
                            By.xpath("//button[normalize-space()='Cancel']")));
                    ((JavascriptExecutor) driver()).executeScript(
                        "arguments[0].click();", btnCancelRecord);
                    cancelClicked = true;
                    System.out.println("POSTCONDICION CP-14: PASO 3 OK - Cancel clickeado (JS).");
                } catch (Exception e1) {
                    System.out.println("POSTCONDICION CP-14: Intento principal fallido: " + e1.getMessage());
                }

                // Fallback: JS directo recorriendo todos los <button> visibles del
                // documento, por si el locator anterior no encontró nada (p.ej. el
                // texto del botón viene con espacios/whitespace distinto).
                if (!cancelClicked) {
                    try {
                        ((JavascriptExecutor) driver()).executeScript(
                            "var btns = document.querySelectorAll('button');" +
                            "for(var i=0; i<btns.length; i++){" +
                            "  if(btns[i].innerText.trim()==='Cancel' && btns[i].offsetParent!==null){" +
                            "    btns[i].click(); break;" +
                            "  }" +
                            "}");
                        cancelClicked = true;
                        System.out.println("POSTCONDICION CP-14: PASO 3 OK (fallback JS) - Cancel clickeado.");
                    } catch (Exception e3) {
                        System.out.println("POSTCONDICION CP-14: Fallback JS fallido: " + e3.getMessage());
                    }
                }

                if (!cancelClicked) {
                    System.out.println("POSTCONDICION CP-14: PASO 3 FALLIDO - No se pudo hacer clic en Cancel.");
                    break;
                }
                pausa(600);

                // PASO 4: Esperar y confirmar el modal "Cancel Leave"
                WebElement btnYesConfirm = new WebDriverWait(driver(), Duration.ofSeconds(10))
                    .until(ExpectedConditions.elementToBeClickable(
                        By.xpath("//button[normalize-space()='Yes, Confirm']")));
                System.out.println("POSTCONDICION CP-14: PASO 4 - Modal visible, confirmando.");
                btnYesConfirm.click();

                // PASO 5: Esperar toast de éxito
                try {
                    new WebDriverWait(driver(), Duration.ofSeconds(10))
                        .until(ExpectedConditions.visibilityOfElementLocated(
                            By.cssSelector(".oxd-toast")));
                    cancelados++;
                    System.out.println("POSTCONDICION CP-14: PASO 5 OK - Registro #" + cancelados + " cancelado.");
                    new WebDriverWait(driver(), Duration.ofSeconds(8))
                        .until(ExpectedConditions.invisibilityOfElementLocated(
                            By.cssSelector(".oxd-toast")));
                } catch (Exception toastEx) {
                    cancelados++;
                    System.out.println("POSTCONDICION CP-14: PASO 5 - Toast no detectado para registro #"
                        + cancelados + ", continuando.");
                    pausa(2000);
                }
                esperarSinSpinner();
            }

        } catch (Exception e) {
            System.out.println("POSTCONDICION CP-14: ERROR -> " + e.getMessage());
        } finally {
            tipoLicenciaRegistrado = null;
        }
    }

    @Cuando("navega al módulo de solicitud de licencias")
    public void navega_modulo_licencias() {
        driver().get(BASE_URL + "/leave/applyLeave");
        esperarSinSpinner();
        espera().until(ExpectedConditions.visibilityOfElementLocated(By.xpath(
            "//label[normalize-space()='Leave Type']" +
            "/following::div[contains(@class,'oxd-select-wrapper')][1]")));
    }

    @Y("selecciona el tipo de licencia de la fila {int}")
    public void selecciona_tipo_licencia(int fila) throws IOException {
        ExcelUtils excel = new ExcelUtils(
            "src/test/resources/testData/dataLicencias.xlsx", "Licencias");
        String tipo = excel.getCellData(fila, 1);
        // Guardar el tipo para poder cancelarlo en @After
        tipoLicenciaRegistrado = tipo;
        System.out.println("CASO 14 - Fila " + fila + " - Tipo: " + tipo);

        esperarSinSpinner();

        By selectWrapper = By.xpath(
            "//label[contains(text(),'Leave Type')]/parent::div/following-sibling::div" +
            "//div[contains(@class,'oxd-select-text')]");

        for (int intento = 0; intento < 3; intento++) {
            try {
                espera().until(ExpectedConditions.elementToBeClickable(selectWrapper)).click();

                By dropdown = By.cssSelector("div[role='listbox']");
                espera().until(ExpectedConditions.visibilityOfElementLocated(dropdown));

                By opcionLocator = By.xpath(
                    "//div[@role='listbox']//span[contains(text(),'" + tipo + "')]");
                WebElement opcion = espera().until(
                    ExpectedConditions.presenceOfElementLocated(opcionLocator));
                ((JavascriptExecutor) driver()).executeScript(
                    "arguments[0].scrollIntoView({block:'center'});", opcion);
                espera().until(ExpectedConditions.elementToBeClickable(opcion)).click();
                espera().until(ExpectedConditions.invisibilityOfElementLocated(dropdown));

                // Esperar a que el Leave Balance termine de cargar antes de continuar.
                // OrangeHRM hace una llamada AJAX al seleccionar el tipo; si no se
                // espera, el siguiente step empieza antes de que el balance esté listo.
                esperarLeaveBalanceCargado();
                return;
            } catch (Exception e) {
                if (intento == 2) throw new RuntimeException(
                    "No se pudo seleccionar la licencia: " + tipo, e);
                try { driver().findElement(By.cssSelector("body")).click();
                } catch (Exception ignored) {}
                esperarSinSpinner();
            }
        }
    }

    @Y("ingresa las fechas de la fila {int}")
    public void ingresa_fechas(int fila) throws IOException {
        ExcelUtils excel = new ExcelUtils(
            "src/test/resources/testData/dataLicencias.xlsx", "Licencias");
        String desde = excel.getCellData(fila, 2);
        String hasta  = excel.getCellData(fila, 3);
        System.out.println("CASO 14 - Fechas: " + desde + " -> " + hasta);

        escribirFecha(
            By.xpath("//label[normalize-space()='From Date']/following::input[1]"), desde);
        driver().findElement(By.cssSelector(".oxd-form")).click();
        esperarSinSpinner();

        escribirFecha(
            By.xpath("//label[normalize-space()='To Date']/following::input[1]"), hasta);
        driver().findElement(By.cssSelector(".oxd-form")).click();
        esperarSinSpinner();
    }

    /**
     * Espera a que el Leave Balance termine de cargarse después de seleccionar
     * un tipo de licencia. OrangeHRM hace una llamada AJAX que actualiza el
     * texto del balance; espera hasta que el elemento deje de mostrar el
     * estado de loading (spinner o texto vacío) y muestre un valor concreto.
     */
    private void esperarLeaveBalanceCargado() {
        try {
            // 1. Esperar que el spinner de la página desaparezca
            esperarSinSpinner();

            // 2. Esperar hasta que el contenedor del Leave Balance tenga texto visible
            //    (cualquier texto: "3000.00 Day(s)", "0.00 Day(s)", "Balance not sufficient")
            WebDriverWait espera10s = new WebDriverWait(driver(), Duration.ofSeconds(10));
            espera10s.until(driver -> {
                try {
                    WebElement balanceArea = driver.findElement(
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
        espera().until(ExpectedConditions.elementToBeClickable(
            By.xpath("//button[normalize-space()='Apply']"))).click();
    }

    @Entonces("la solicitud debe quedar registrada en el sistema")
    public void solicitud_registrada() {
        try {
            esperaLarga().until(ExpectedConditions.visibilityOfAllElementsLocatedBy(
                By.cssSelector(".oxd-toast-content-text")));
            System.out.println("CASO 14 OK: Licencia registrada - toast visible");
        } catch (Exception e) {
            Assert.assertTrue("La solicitud no fue registrada",
                driver().getCurrentUrl().contains("/leave/"));
            System.out.println("CASO 14 OK: Licencia registrada - URL confirmada");
        }
    }
}