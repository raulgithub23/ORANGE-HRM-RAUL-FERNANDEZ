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

// page object de add leave entitlement (para meter dias de licencia)
public class AddLeaveEntitlementPage extends BasePage {

    // le clavo 3000 dias a proposito. la idea no es que sea un numero real, es nomas para que el 
    // admin no se quede sin saldo cuando corremos el cp-14 a cada rato mientras arreglamos la suite
    private static final String ENTITLEMENT_CANTIDAD = "3000";
    private static final By RADIO_INDIVIDUAL = By.xpath(
        "//label[normalize-space()='Individual Employee']" +
        "/preceding-sibling::input[@type='radio']");
    private static final By INPUT_AUTOCOMPLETE = By.cssSelector(
        ".oxd-form .oxd-autocomplete-wrapper input");
    private static final By DROPDOWN_LEAVE_TYPE = By.xpath(
        "//label[normalize-space()='Leave Type']/parent::div" +
        "/following-sibling::div//div[contains(@class,'oxd-select-text')]");
    private static final By INPUT_ENTITLEMENT = By.xpath(
        "//label[normalize-space()='Entitlement']/parent::div" +
        "/following-sibling::div//input");
    private static final By BTN_SAVE = By.xpath("//button[normalize-space()='Save']");
    private static final By BTN_CONFIRM = By.xpath("//button[normalize-space()='Confirm']");
    private static final By TOAST_EXITO = By.cssSelector(".oxd-toast--success");

    public AddLeaveEntitlementPage(WebDriver driver) {
        super(driver);
    }

    public void abrir() {
        driver.get(BASE_URL + "/leave/addLeaveEntitlement");
        esperarSinSpinner();
    }

    // hace todo el flujo de una: abre el form, marca que es para un solo empleado (no toda la empresa), 
    // busca al loco, elige que licencia darle y guarda. esto se llama una vez por cada tipo de licencia (son 3 para el cp-14)
    public void asignarEntitlementIndividual(String nombreUsuario, String tipoLicencia) {
        abrir();
        seleccionarIndividualEmployee();
        escribirNombreYSeleccionarSugerencia(nombreUsuario, tipoLicencia);
        seleccionarLeaveType(tipoLicencia);
        ingresarCantidadEntitlement();
        guardarYConfirmar(tipoLicencia);
    }

    private void seleccionarIndividualEmployee() {
        try {
            WebElement radio = wait.until(ExpectedConditions.presenceOfElementLocated(RADIO_INDIVIDUAL));
            if (!radio.isSelected()) {
                radio.click();
                System.out.println("ENTITLEMENTS: Radio 'Individual Employee' seleccionado");
            }
        } catch (Exception e) {
            System.out.println("ENTITLEMENTS: Radio Individual no encontrado, continuando");
        }
    }

    // aca escribo el nombre letra por letra y le meto una pausa chiquita entre medio para que parezca 
    // que lo tipea alguien. orangehrm es medio mañoso y busca con cada tecla que tocas, si le pegas 
    // todo de una se marea y a veces no te carga el dropdown. y solo agarro las dos primeras palabras 
    // porque con nombre y apellido ya lo encuentra, si le meto mas texto capaz falla si es un nombre muy largo
    private void escribirNombreYSeleccionarSugerencia(String nombreUsuario, String tipoLicencia) {
        WebElement campoNombre = waitClickable(INPUT_AUTOCOMPLETE);
        campoNombre.click();
        campoNombre.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.DELETE);
        pausa(300);

        String[] palabras = nombreUsuario.split(" ");
        String textoBusqueda = palabras.length >= 2
            ? palabras[0] + " " + palabras[1]
            : nombreUsuario;
        System.out.println("ENTITLEMENTS: Escribiendo nombre -> " + textoBusqueda);

        for (char c : textoBusqueda.toCharArray()) {
            campoNombre.sendKeys(String.valueOf(c));
            pausa(200);
        }
        pausa(2000);

        if (!intentarClicEnSugerencia(campoNombre, textoBusqueda)) {
            System.out.println("ENTITLEMENTS: Todos los intentos fallaron para: " + tipoLicencia);
        }
        pausa(800);

        // despues de elegir, me fijo que orange no haya tirado un "invalid" en el campo 
        // (pasa si el nombre que escribimos no existe en el sistema)
        List<WebElement> invalidos = driver.findElements(
            By.xpath("//span[normalize-space()='Invalid']"));
        if (!invalidos.isEmpty()) {
            System.out.println("ENTITLEMENTS: ADVERTENCIA - Employee Name inválido para: "
                + tipoLicencia);
        }
    }

    // meto 3 formas de agarrar la sugerencia, de la mas exacta a la mas rustica. orange a veces 
    // cambia las clases css del dropdown asi que si fallan los selectores le doy con el 
    // teclado nomas (flechita abajo y enter) que te salva siempre si falla el mouse
    private boolean intentarClicEnSugerencia(WebElement campoNombre, String textoBusqueda) {
        try {
            WebElement opcion = new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.elementToBeClickable(
                    By.cssSelector(".oxd-autocomplete-dropdown .oxd-autocomplete-option:first-child")));
            scrollIntoView(opcion);
            pausa(200);
            opcion.click();
            System.out.println("ENTITLEMENTS: Clic en sugerencia OK -> " + textoBusqueda);
            return true;
        } catch (Exception e1) {
            System.out.println("ENTITLEMENTS: Selector 1 falló, probando selector 2");
        }
        try {
            WebElement opcion = new WebDriverWait(driver, Duration.ofSeconds(5))
                .until(ExpectedConditions.elementToBeClickable(
                    By.cssSelector("[role='option']:first-child")));
            scrollIntoView(opcion);
            pausa(200);
            opcion.click();
            return true;
        } catch (Exception e2) {
            System.out.println("ENTITLEMENTS: Selector 2 falló, usando teclado");
        }
        try {
            campoNombre.sendKeys(Keys.ARROW_DOWN);
            pausa(400);
            campoNombre.sendKeys(Keys.ENTER);
            return true;
        } catch (Exception e3) {
            return false;
        }
    }

    private void seleccionarLeaveType(String tipoLicencia) {
        waitClickable(DROPDOWN_LEAVE_TYPE).click();
        esperarSinSpinner();
        By opcionTipo = By.xpath(
            "//div[@role='listbox']//span[normalize-space()='" + tipoLicencia + "']");
        waitClickable(opcionTipo).click();
        esperarSinSpinner();
        System.out.println("ENTITLEMENTS: Leave Type seleccionado -> " + tipoLicencia);
    }

    private void ingresarCantidadEntitlement() {
        WebElement campoEntitlement = waitVisible(INPUT_ENTITLEMENT);
        campoEntitlement.click();
        campoEntitlement.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.DELETE);
        campoEntitlement.sendKeys(ENTITLEMENT_CANTIDAD);
        System.out.println("ENTITLEMENTS: Entitlement ingresado -> " + ENTITLEMENT_CANTIDAD);
    }

    // el modal para confirmar solo sale si el empleado ya tenia dias asignados antes en esa licencia. 
    // si es la primera vez lo guarda de una sin preguntar. por eso espero el boton de confirm un par de segundos 
    // nomas, y si no aparece sigo de largo porque no es error
    private void guardarYConfirmar(String tipoLicencia) {
        waitClickable(BTN_SAVE).click();
        esperarSinSpinner();
        try {
            new WebDriverWait(driver, Duration.ofSeconds(6))
                .until(ExpectedConditions.elementToBeClickable(BTN_CONFIRM)).click();
            System.out.println("ENTITLEMENTS: Modal Confirm presionado -> " + tipoLicencia);
        } catch (Exception eModal) {
            System.out.println("ENTITLEMENTS: Sin modal de confirmación -> " + tipoLicencia);
        }
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(TOAST_EXITO));
            System.out.println("ENTITLEMENTS: Entitlement guardado OK -> " + tipoLicencia);
        } catch (Exception e) {
            System.out.println("ENTITLEMENTS: Sin toast de éxito para " + tipoLicencia);
        }
        esperarSinSpinner();
    }
}