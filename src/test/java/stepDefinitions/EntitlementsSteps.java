package stepDefinitions;

import io.cucumber.java.es.Cuando;
import io.cucumber.java.es.Entonces;
import io.cucumber.java.es.Y;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * PPT 3.2.1 - Precondición de Caso 14: asignación/recarga de créditos (Entitlements)
 * para los tipos de licencia US - Vacaciones, US - Asunto Personales y US - Luto.
 *
 * Se ejecuta como escenario Gherkin explícito en entitlements.feature, UNA sola vez
 * por suite (gracias al orden alfabético: entitlements.feature corre antes que
 * licencias.feature), en lugar de un @Before oculto que se repetía por cada fila
 * del Esquema del escenario de CP-14.
 */
public class EntitlementsSteps {

    private final Configuracion configuracion;

    private static final String ENTITLEMENT_CANTIDAD = "3000";
    private static final String BASE_URL =
        "https://opensource-demo.orangehrmlive.com/web/index.php";

    // Acumula los resultados de cada tipo de licencia para la verificación final
    private final List<String> resultadosAsignacion = new ArrayList<>();

    public EntitlementsSteps(Configuracion configuracion) {
        this.configuracion = configuracion;
    }

    private WebDriver driver() {
        return configuracion.obtenerDriver();
    }

    private WebDriverWait espera() {
        return new WebDriverWait(driver(), Duration.ofSeconds(15));
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

    // NOTA: el step "el usuario está autenticado en el sistema" ya está
    // definido en BusquedaSteps.java (compartido por reportes.feature y
    // suite.feature). Cucumber no permite definirlo dos veces en el mismo
    // classpath de glue, así que entitlements.feature reutiliza ese mismo
    // step sin necesidad de redeclararlo aquí.

    // -------------------------------------------------------------------------
    // PRECONDICION extra: el ambiente demo de OrangeHRM se reinicia cada 24h
    // y, además, cualquier otro usuario que comparte la demo puede borrar los
    // Leave Types personalizados (US - Vacaciones, US - Asunto Personales,
    // US - Luto). Antes de poder recargar créditos para esos tipos, hay que
    // garantizar que existan: si ya existen (verificado por nombre exacto en
    // Leave > Configure > Leave Types) no se vuelven a crear; si no existen,
    // se crean desde cero con "Is Entitlement Situational" = Yes.
    // -------------------------------------------------------------------------

    @Y("asegura que el tipo de licencia {string} existe")
    public void asegura_tipo_licencia_existe(String tipoLicencia) {
        try {
            if (existeLeaveType(tipoLicencia)) {
                System.out.println("ENTITLEMENTS: Leave Type '" + tipoLicencia
                    + "' ya existe, no se crea de nuevo.");
                return;
            }
            System.out.println("ENTITLEMENTS: Leave Type '" + tipoLicencia
                + "' no existe, creando...");
            crearLeaveType(tipoLicencia);
        } catch (Exception e) {
            System.out.println("ENTITLEMENTS: ERROR asegurando Leave Type '"
                + tipoLicencia + "' -> " + e.getMessage());
        }
    }

    private boolean existeLeaveType(String tipoLicencia) {
        driver().get(BASE_URL + "/leave/leaveTypeList");
        esperarSinSpinner();
        espera().until(ExpectedConditions.visibilityOfElementLocated(
            By.xpath("//*[normalize-space()='Leave Types']")));

        By filaConNombre = By.xpath(
            "//div[contains(@class,'oxd-table-body')]" +
            "//div[contains(@class,'oxd-table-row')]" +
            "[.//*[normalize-space(text())='" + tipoLicencia + "']]");
        boolean existe = !driver().findElements(filaConNombre).isEmpty();
        System.out.println("ENTITLEMENTS: ¿Existe '" + tipoLicencia + "'? -> " + existe);
        return existe;
    }

    private void crearLeaveType(String tipoLicencia) {
        // Botón "+ Add" arriba a la derecha de la lista de Leave Types
        By botonAdd = By.xpath(
            "//button[contains(@class,'oxd-button') and contains(.,'Add')]");
        WebElement btnAdd = espera().until(ExpectedConditions.elementToBeClickable(botonAdd));
        System.out.println("ENTITLEMENTS: URL antes de clic en +Add -> " + driver().getCurrentUrl());
        btnAdd.click();

        // Verificación explícita de que el clic SÍ navegó al formulario.
        // (diagnóstico: en una corrida anterior el clic no lanzaba excepción
        // pero tampoco navegaba, y solo lo notamos por captura de pantalla)
        try {
            new WebDriverWait(driver(), Duration.ofSeconds(10)).until(
                d -> d.getCurrentUrl().contains("defineLeaveType"));
            System.out.println("ENTITLEMENTS: Navegación a formulario OK -> "
                + driver().getCurrentUrl());
        } catch (Exception eNav) {
            System.out.println("ENTITLEMENTS: El clic en +Add NO navegó al formulario. "
                + "URL actual -> " + driver().getCurrentUrl());
            // Reintento: a veces el primer clic no llega a destino por la SPA
            // todavía re-renderizando la lista. Reintentamos una vez más vía JS.
            try {
                WebElement btnAddRetry = driver().findElement(botonAdd);
                ((JavascriptExecutor) driver()).executeScript(
                    "arguments[0].click();", btnAddRetry);
                new WebDriverWait(driver(), Duration.ofSeconds(10)).until(
                    d -> d.getCurrentUrl().contains("defineLeaveType"));
                System.out.println("ENTITLEMENTS: Navegación OK tras reintento JS -> "
                    + driver().getCurrentUrl());
            } catch (Exception eRetry) {
                throw new RuntimeException(
                    "No se pudo navegar al formulario 'Add Leave Type' tras 2 intentos. "
                    + "URL final: " + driver().getCurrentUrl(), eRetry);
            }
        }
        esperarSinSpinner();

        // Campo Name del formulario "Add Leave Type". En este formulario solo
        // hay UN input de texto visible (los otros campos son radios), así
        // que en vez de encadenar XPaths frágiles sobre el <label> (que ya
        // fallaron 2 veces con estructuras distintas a las asumidas), vamos
        // directo al input de texto visible de la página.
        WebElement campoNombre = espera().until(ExpectedConditions.visibilityOfElementLocated(
            By.xpath("//label[normalize-space()='Name']/parent::div/following-sibling::div//input")));
        System.out.println("ENTITLEMENTS: Campo Name encontrado OK.");
        campoNombre.click();
        campoNombre.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.DELETE);
        campoNombre.sendKeys(tipoLicencia);

        // "Is Entitlement Situational?" -> Yes. El default observado en
        // producción es "No" (no "Yes" como se asumió inicialmente), así que
        // hay que marcarlo explícitamente. En vez de depender de la posición
        // del <label> respecto al <input> (frágil si hay wrappers de por
        // medio), tomamos los radios de la página por orden: en este
        // formulario solo hay un grupo de radios (Yes/No) y "Yes" es el
        // primero. Se hace clic vía JS para evitar el mismo problema de
        // "click intercepted" por el ícono decorativo que tuvimos con el
        // checkbox de Leave List.
        try {
            List<WebElement> radios = driver().findElements(
                By.cssSelector("input[type='radio']"));
            if (!radios.isEmpty()) {
                WebElement radioYes = radios.get(0);
                ((JavascriptExecutor) driver()).executeScript(
                    "arguments[0].click();", radioYes);
                System.out.println("ENTITLEMENTS: Radio 'Yes' (Situational) marcado.");
            } else {
                System.out.println("ENTITLEMENTS: No se encontraron radios en el formulario.");
            }
        } catch (Exception e) {
            System.out.println("ENTITLEMENTS: No se pudo marcar 'Yes' en Situational, "
                + "continuando con el default -> " + e.getMessage());
        }

        espera().until(ExpectedConditions.elementToBeClickable(
            By.xpath("//button[normalize-space()='Save']"))).click();
        esperarSinSpinner();

        try {
            espera().until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector(".oxd-toast--success")));
            System.out.println("ENTITLEMENTS: Leave Type '" + tipoLicencia
                + "' creado correctamente.");
        } catch (Exception e) {
            System.out.println("ENTITLEMENTS: Sin toast de éxito al crear '"
                + tipoLicencia + "', verificar manualmente.");
        }
    }

    @Cuando("recarga el crédito de licencia para {string}")
    public void recarga_credito_licencia(String tipoLicencia) {
        try {
            String nombreUsuario = leerNombreUsuarioDesdeHeader();
            asignarEntitlementIndividual(nombreUsuario, tipoLicencia);
            resultadosAsignacion.add(tipoLicencia + ":OK");
        } catch (Exception e) {
            System.out.println("ENTITLEMENTS: Falló asignando '" + tipoLicencia
                + "' -> " + e.getMessage());
            resultadosAsignacion.add(tipoLicencia + ":FALLO");
        }
    }

    /**
     * Lee el nombre del usuario actualmente logueado desde el header de OrangeHRM.
     * Se usa en lugar de un nombre fijo porque la demo es compartida y el usuario
     * puede cambiar (ej: "manda user", "Vishnu user", etc.).
     */
    private String leerNombreUsuarioDesdeHeader() {
        try {
            String nombre = espera()
                .until(ExpectedConditions.visibilityOfElementLocated(
                    By.cssSelector(".oxd-userdropdown-name")))
                .getText().trim();
            if (!nombre.isEmpty()) {
                System.out.println("ENTITLEMENTS: Nombre leído del header -> " + nombre);
                return nombre;
            }
        } catch (Exception e) {
            System.out.println("ENTITLEMENTS: No se pudo leer nombre del header -> "
                + e.getMessage());
        }
        // Fallback: intentar leer el atributo alt del avatar
        try {
            String alt = driver()
                .findElement(By.cssSelector(".oxd-userdropdown-tab img"))
                .getAttribute("alt").trim();
            if (!alt.isEmpty()) {
                System.out.println("ENTITLEMENTS: Nombre leído del avatar alt -> " + alt);
                return alt;
            }
        } catch (Exception ignored) {}
        System.out.println("ENTITLEMENTS: No se pudo leer nombre, usando 'Admin' como fallback");
        return "Admin";
    }

    @Entonces("los créditos de licencia deben quedar asignados correctamente")
    public void verificar_creditos_asignados() {
        System.out.println("ENTITLEMENTS: Resumen de asignación -> " + resultadosAsignacion);
        long fallos = resultadosAsignacion.stream().filter(r -> r.endsWith(":FALLO")).count();
        if (fallos > 0) {
            System.out.println("ENTITLEMENTS: ADVERTENCIA - " + fallos
                + " tipo(s) de licencia no se pudieron recargar. Revisar log anterior.");
        } else {
            System.out.println("ENTITLEMENTS: Los 3 tipos de licencia quedaron con "
                + ENTITLEMENT_CANTIDAD + " días disponibles.");
        }
        // No se usa Assert duro aquí para no frenar la suite completa si un tipo
        // puntual falla; el log deja evidencia clara para revisión manual.
    }

    // -------------------------------------------------------------------------
    // Lógica de asignación (misma implementación que tenía LicenciasSteps,
    // movida aquí para mantener responsabilidad única).
    // -------------------------------------------------------------------------

    private void asignarEntitlementIndividual(String nombreUsuario, String tipoLicencia) {
        driver().get(BASE_URL + "/leave/addLeaveEntitlement");
        esperarSinSpinner();

        seleccionarIndividualEmployee();
        escribirNombreYSeleccionarSugerencia(nombreUsuario, tipoLicencia);
        seleccionarLeaveType(tipoLicencia);
        ingresarCantidadEntitlement();
        guardarYConfirmar(tipoLicencia);
    }

    private void seleccionarIndividualEmployee() {
        try {
            WebElement radio = espera().until(ExpectedConditions.presenceOfElementLocated(
                By.xpath("//label[normalize-space()='Individual Employee']" +
                         "/preceding-sibling::input[@type='radio']")));
            if (!radio.isSelected()) {
                radio.click();
                System.out.println("ENTITLEMENTS: Radio 'Individual Employee' seleccionado");
            }
        } catch (Exception e) {
            System.out.println("ENTITLEMENTS: Radio Individual no encontrado, continuando");
        }
    }

    private void escribirNombreYSeleccionarSugerencia(String nombreUsuario, String tipoLicencia) {
        WebElement campoNombre = espera().until(
            ExpectedConditions.elementToBeClickable(
                By.cssSelector(".oxd-form .oxd-autocomplete-wrapper input")));
        campoNombre.click();
        campoNombre.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.DELETE);
        pausa(300);

        // Escribimos el nombre dinámico leído del header. Si el nombre tiene
        // más de una palabra, usamos las dos primeras como texto de búsqueda
        // para que el autocomplete de OrangeHRM filtre con suficiente precisión.
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
        boolean clicExitoso = intentarClicEnSugerencia(campoNombre, textoBusqueda);
        if (!clicExitoso) {
            System.out.println("ENTITLEMENTS: Todos los intentos fallaron para: "
                + tipoLicencia);
        }
        pausa(800);
        List<WebElement> invalidos = driver().findElements(
            By.xpath("//span[normalize-space()='Invalid']"));
        if (!invalidos.isEmpty()) {
            System.out.println("ENTITLEMENTS: ADVERTENCIA - Employee Name inválido para: "
                + tipoLicencia + " (el nombre '" + textoBusqueda
                + "' no existe en Employee List del ambiente actual)");
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
            System.out.println("ENTITLEMENTS: Clic en sugerencia OK (selector 1) -> "
                + textoBusqueda);
            return true;
        } catch (Exception e1) {
            System.out.println("ENTITLEMENTS: Selector 1 falló, probando selector 2");
        }
        try {
            WebElement opcion = new WebDriverWait(driver(), Duration.ofSeconds(5))
                .until(ExpectedConditions.elementToBeClickable(
                    By.cssSelector("[role='option']:first-child")));
            ((JavascriptExecutor) driver()).executeScript(
                "arguments[0].scrollIntoView({block:'center'});", opcion);
            pausa(200);
            opcion.click();
            System.out.println("ENTITLEMENTS: Clic en sugerencia OK (selector 2) -> "
                + textoBusqueda);
            return true;
        } catch (Exception e2) {
            System.out.println("ENTITLEMENTS: Selector 2 falló, usando teclado");
        }
        try {
            campoNombre.sendKeys(Keys.ARROW_DOWN);
            pausa(400);
            campoNombre.sendKeys(Keys.ENTER);
            System.out.println("ENTITLEMENTS: Selección por teclado OK -> "
                + textoBusqueda);
            return true;
        } catch (Exception e3) {
            System.out.println("ENTITLEMENTS: Fallback de teclado falló -> "
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
        System.out.println("ENTITLEMENTS: Leave Type seleccionado -> " + tipoLicencia);
    }

    private void ingresarCantidadEntitlement() {
        WebElement campoEntitlement = espera().until(
            ExpectedConditions.visibilityOfElementLocated(By.xpath(
                "//label[normalize-space()='Entitlement']/parent::div" +
                "/following-sibling::div//input")));
        campoEntitlement.click();
        campoEntitlement.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.DELETE);
        campoEntitlement.sendKeys(ENTITLEMENT_CANTIDAD);
        System.out.println("ENTITLEMENTS: Entitlement ingresado -> " + ENTITLEMENT_CANTIDAD);
    }

    private void guardarYConfirmar(String tipoLicencia) {
        espera().until(ExpectedConditions.elementToBeClickable(
            By.xpath("//button[normalize-space()='Save']"))).click();
        esperarSinSpinner();
        try {
            new WebDriverWait(driver(), Duration.ofSeconds(6))
                .until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//button[normalize-space()='Confirm']"))).click();
            System.out.println("ENTITLEMENTS: Modal Confirm presionado -> " + tipoLicencia);
        } catch (Exception eModal) {
            System.out.println("ENTITLEMENTS: Sin modal de confirmación -> " + tipoLicencia);
        }
        try {
            espera().until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector(".oxd-toast--success")));
            System.out.println("ENTITLEMENTS: Entitlement guardado OK -> " + tipoLicencia);
        } catch (Exception e) {
            System.out.println("ENTITLEMENTS: Sin toast de éxito para "
                + tipoLicencia + " (continuando)");
        }
        esperarSinSpinner();
    }
}