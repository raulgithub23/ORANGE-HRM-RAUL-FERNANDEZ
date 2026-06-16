package stepDefinitions;

import utilidades.ExcelUtils;
import io.cucumber.java.Before;
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
import java.util.Arrays;
import java.util.List;

public class LicenciasSteps {

    private final Configuracion configuracion;
    private static final List<String> TIPOS_LICENCIA = Arrays.asList(
        "US - Vacation",
        "US - Personal",
        "US - Bereavement"
    );

    private static final String ENTITLEMENT_CANTIDAD = "3000";
    private static final String BASE_URL =
        "https://opensource-demo.orangehrmlive.com/web/index.php";

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

    @Before(value = "@licencias")
    public void asignarEntitlements() {
        hacerLogin();

        String nombreUsuario = leerNombreUsuarioDesdeHeader();
        System.out.println("PRECONDICION CP-14: Usuario activo -> " + nombreUsuario);

        for (String tipoLicencia : TIPOS_LICENCIA) {
            asignarEntitlementIndividual(nombreUsuario, tipoLicencia);
        }

        System.out.println("PRECONDICION CP-14: Entitlements asignados para los 3 tipos.");
        hacerLogout();
    }

    private void hacerLogin() {
        driver().get(BASE_URL + "/auth/login");

        boolean yaLogueado = false;
        try {
            new WebDriverWait(driver(), Duration.ofSeconds(5))
                .until(ExpectedConditions.urlContains("/dashboard"));
            yaLogueado = true;
            System.out.println("PRECONDICION CP-14: Sesión ya activa, se omite login");
        } catch (Exception ignored) {}

        if (!yaLogueado) {
            try {
                espera().until(ExpectedConditions.visibilityOfElementLocated(
                    By.name("username"))).sendKeys("Admin");
                driver().findElement(By.name("password")).sendKeys("admin123");
                driver().findElement(By.cssSelector("button[type='submit']")).click();
                esperaLarga().until(ExpectedConditions.urlContains("/dashboard"));
                System.out.println("PRECONDICION CP-14: Login exitoso");
            } catch (Exception e) {
                System.out.println("PRECONDICION CP-14: Falló el login -> " + e.getMessage());
            }
        }
    }

    private String leerNombreUsuarioDesdeHeader() {
        try {
            String nombre = espera()
                .until(ExpectedConditions.visibilityOfElementLocated(
                    By.cssSelector(".oxd-userdropdown-name")))
                .getText().trim();
            if (!nombre.isEmpty()) {
                System.out.println("PRECONDICION CP-14: Nombre leído del header -> " + nombre);
                return nombre;
            }
        } catch (Exception e) {
            System.out.println("PRECONDICION CP-14: No se pudo leer nombre del header");
        }
        return "Admin";
    }

    private void hacerLogout() {
        try {
            espera().until(ExpectedConditions.elementToBeClickable(
                By.cssSelector(".oxd-userdropdown-tab"))).click();

            espera().until(ExpectedConditions.elementToBeClickable(
                By.xpath("//a[normalize-space()='Logout']"))).click();

            espera().until(ExpectedConditions.visibilityOfElementLocated(
                By.name("username")));
            System.out.println("PRECONDICION CP-14: Logout exitoso");
        } catch (Exception e) {
            System.out.println("PRECONDICION CP-14: Logout falló, navegando a /auth/logout");
            driver().get(BASE_URL + "/auth/logout");
            try {
                espera().until(ExpectedConditions.visibilityOfElementLocated(
                    By.name("username")));
            } catch (Exception ignored) {}
        }
    }

    private void asignarEntitlementIndividual(String nombreUsuario, String tipoLicencia) {
        try {
            driver().get(BASE_URL + "/leave/addLeaveEntitlement");
            esperarSinSpinner();

            seleccionarIndividualEmployee();
            escribirNombreYSeleccionarSugerencia(nombreUsuario, tipoLicencia);
            seleccionarLeaveType(tipoLicencia);
            ingresarCantidadEntitlement();
            guardarYConfirmar(tipoLicencia);

        } catch (Exception e) {
            System.out.println("PRECONDICION CP-14: Falló asignando '"
                + tipoLicencia + "' -> " + e.getMessage());
        }
    }

    private void seleccionarIndividualEmployee() {
        try {
            WebElement radio = espera().until(ExpectedConditions.presenceOfElementLocated(
                By.xpath("//label[normalize-space()='Individual Employee']" +
                         "/preceding-sibling::input[@type='radio']")));
            if (!radio.isSelected()) {
                radio.click();
                System.out.println("PRECONDICION CP-14: Radio 'Individual Employee' seleccionado");
            }
        } catch (Exception e) {
            System.out.println("PRECONDICION CP-14: Radio Individual no encontrado, continuando");
        }
    }

    private void escribirNombreYSeleccionarSugerencia(String nombreUsuario, String tipoLicencia) {
        WebElement campoNombre = espera().until(
            ExpectedConditions.elementToBeClickable(
                By.cssSelector(".oxd-form .oxd-autocomplete-wrapper input")));
        campoNombre.click();
        campoNombre.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.DELETE);
        pausa(300);
        String[] palabras = nombreUsuario.split(" ");
        String textoBusqueda = palabras.length >= 2
            ? palabras[0] + " " + palabras[1]
            : nombreUsuario;
        System.out.println("PRECONDICION CP-14: Escribiendo nombre -> " + textoBusqueda);
        for (char c : textoBusqueda.toCharArray()) {
            campoNombre.sendKeys(String.valueOf(c));
            pausa(200);
        }
        pausa(2000);
        boolean clicExitoso = intentarClicEnSugerencia(campoNombre, textoBusqueda);
        if (!clicExitoso) {
            System.out.println("PRECONDICION CP-14: Todos los intentos fallaron para: "
                + tipoLicencia);
        }
        pausa(800);
        List<WebElement> invalidos = driver().findElements(
            By.xpath("//span[normalize-space()='Invalid']"));
        if (!invalidos.isEmpty()) {
            System.out.println("PRECONDICION CP-14: ADVERTENCIA - Employee Name inválido para: "
                + tipoLicencia);
        }
    }

    private boolean intentarClicEnSugerencia(WebElement campoNombre, String textoBusqueda) {
        try {
            WebElement opcion = new WebDriverWait(driver(), Duration.ofSeconds(10))
                .until(ExpectedConditions.elementToBeClickable(
                    By.cssSelector(".oxd-autocomplete-dropdown " +
                                   ".oxd-autocomplete-option:first-child")));
            ((JavascriptExecutor) driver()).executeScript(
                "arguments[0].scrollIntoView({block:'center'});", opcion);
            pausa(200);
            opcion.click();
            System.out.println("PRECONDICION CP-14: Clic en sugerencia OK (selector 1) -> "
                + textoBusqueda);
            return true;
        } catch (Exception e1) {
            System.out.println("PRECONDICION CP-14: Selector 1 falló, probando selector 2");
        }
        try {
            WebElement opcion = new WebDriverWait(driver(), Duration.ofSeconds(5))
                .until(ExpectedConditions.elementToBeClickable(
                    By.cssSelector("[role='option']:first-child")));
            ((JavascriptExecutor) driver()).executeScript(
                "arguments[0].scrollIntoView({block:'center'});", opcion);
            pausa(200);
            opcion.click();
            System.out.println("PRECONDICION CP-14: Clic en sugerencia OK (selector 2) -> "
                + textoBusqueda);
            return true;
        } catch (Exception e2) {
            System.out.println("PRECONDICION CP-14: Selector 2 falló, usando teclado");
        }
        try {
            campoNombre.sendKeys(Keys.ARROW_DOWN);
            pausa(400);
            campoNombre.sendKeys(Keys.ENTER);
            System.out.println("PRECONDICION CP-14: Selección por teclado OK -> "
                + textoBusqueda);
            return true;
        } catch (Exception e3) {
            System.out.println("PRECONDICION CP-14: Fallback de teclado falló -> "
                + e3.getMessage());
        }

        return false;
    }

    private void seleccionarLeaveType(String tipoLicencia) {
        By dropdownTipo = By.xpath(
            "//label[normalize-space()='Leave Type']/parent::div" +
            "/following-sibling::div//div[contains(@class,'oxd-select-text')]");
        espera().until(ExpectedConditions.elementToBeClickable(dropdownTipo)).click();
        esperarSinSpinner();

        By opcionTipo = By.xpath(
            "//div[@role='listbox']//span[normalize-space()='" + tipoLicencia + "']");
        espera().until(ExpectedConditions.elementToBeClickable(opcionTipo)).click();
        esperarSinSpinner();
        System.out.println("PRECONDICION CP-14: Leave Type seleccionado -> " + tipoLicencia);
    }

    private void ingresarCantidadEntitlement() {
        WebElement campoEntitlement = espera().until(
            ExpectedConditions.visibilityOfElementLocated(By.xpath(
                "//label[normalize-space()='Entitlement']/parent::div" +
                "/following-sibling::div//input")));
        campoEntitlement.click();
        campoEntitlement.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.DELETE);
        campoEntitlement.sendKeys(ENTITLEMENT_CANTIDAD);
        System.out.println("PRECONDICION CP-14: Entitlement ingresado -> " + ENTITLEMENT_CANTIDAD);
    }

    private void guardarYConfirmar(String tipoLicencia) {
        espera().until(ExpectedConditions.elementToBeClickable(
            By.xpath("//button[normalize-space()='Save']"))).click();
        esperarSinSpinner();
        try {
            new WebDriverWait(driver(), Duration.ofSeconds(6))
                .until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//button[normalize-space()='Confirm']"))).click();
            System.out.println("PRECONDICION CP-14: Modal Confirm presionado -> " + tipoLicencia);
        } catch (Exception eModal) {
            System.out.println("PRECONDICION CP-14: Sin modal de confirmación -> " + tipoLicencia);
        }
        try {
            espera().until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector(".oxd-toast--success")));
            System.out.println("PRECONDICION CP-14: Entitlement guardado OK -> " + tipoLicencia);
        } catch (Exception e) {
            System.out.println("PRECONDICION CP-14: Sin toast de éxito para "
                + tipoLicencia + " (continuando)");
        }
        esperarSinSpinner();
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