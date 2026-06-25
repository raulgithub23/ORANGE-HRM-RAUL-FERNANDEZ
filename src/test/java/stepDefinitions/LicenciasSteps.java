package stepDefinitions;

import utilidades.ExcelUtils;
import io.cucumber.java.After;
import io.cucumber.java.es.Cuando;
import io.cucumber.java.es.Entonces;
import io.cucumber.java.es.Y;
import org.junit.Assert;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import pageObjects.LeaveApplyPage;
import pageObjects.LeaveListPage;

import java.io.IOException;

// ppt 3.2.1 + 3.3.1 - caso 14: registrar licencia con datos desde excel.
public class LicenciasSteps {

    private final Configuracion configuracion;
    // me guardo el tipo de licencia que elegimos en el escenario. por ahora no lo uso afuera de este flujo, pero lo dejo ahi por si mañana la postcondicion necesita 
    // filtrar y cancelar un tipo especifico en vez de volar la primera fila que encuentre asi nomas.
    private String tipoLicenciaRegistrado = null;

    public LicenciasSteps(Configuracion configuracion) {
        this.configuracion = configuracion;
    }

    private WebDriver driver() {
        return configuracion.obtenerDriver();
    }

    private LeaveApplyPage leaveApplyPage() {
        return new LeaveApplyPage(driver());
    }

    private LeaveListPage leaveListPage() {
        return new LeaveListPage(driver());
    }

    // postcondicion automatica del cp-14, enganchada al tag @caso14 del feature 
    // (no corre en otros lados). cancela las licencias que acabamos de meter en este 
    // escenario, asi la demo publica no se llena de licencias colgadas cada vez que 
    // corremos esto o entra alguien mas.
    @After(value = "@caso14", order = 1)
    public void cancelarSolicitudRegistrada() {
        System.out.println("POSTCONDICION CP-14: Iniciando limpieza de solicitudes pendientes.");
        try {
            leaveListPage().cancelarSolicitudesPendientes(10);
        } catch (Exception e) {
            // si falla la limpieza no tiene que tapar o pisar el resultado real del test (si paso o fallo), que ya se decidio antes de entrar a este hook.
            System.out.println("POSTCONDICION CP-14: ERROR -> " + e.getMessage());
        } finally {
            tipoLicenciaRegistrado = null;
        }
    }

    @Cuando("navega al módulo de solicitud de licencias")
    public void navega_modulo_licencias() {
        leaveApplyPage().abrir();
    }

    @Y("selecciona el tipo de licencia de la fila {int}")
    public void selecciona_tipo_licencia(int fila) throws IOException {
        ExcelUtils excel = new ExcelUtils(
            "src/test/resources/testData/dataLicencias.xlsx", "Licencias");
        String tipo = excel.getCellData(fila, 1);
        tipoLicenciaRegistrado = tipo;
        System.out.println("CASO 14 - Fila " + fila + " - Tipo: " + tipo);
        leaveApplyPage().seleccionarTipoLicencia(tipo);
    }

    @Y("ingresa las fechas de la fila {int}")
    public void ingresa_fechas(int fila) throws IOException {
        ExcelUtils excel = new ExcelUtils(
            "src/test/resources/testData/dataLicencias.xlsx", "Licencias");
        String desde = excel.getCellData(fila, 2);
        String hasta  = excel.getCellData(fila, 3);
        System.out.println("CASO 14 - Fechas: " + desde + " -> " + hasta);
        leaveApplyPage().ingresarFechas(desde, hasta);
    }

    @Y("hace clic en aplicar la solicitud")
    public void click_aplicar_solicitud() {
        leaveApplyPage().aplicar();
    }

    // le meti tres validaciones en cadena para ver si anduvo: (1) si el form tira errores 
    // en los campos, revienta al toque y avisa; (2) si orange tira alerta de "Failed" o 
    // "No Working Days" (que es rechazo del sistema, no de formato), tambien explota; (3) si pasamos todo eso, espero el cartelito verde (toast) para confirmar. armo esta 
    // cascada porque pedir una licencia puede fallar por mil cosas distintas (mal la fecha, sin saldo, dia no laborable) y necesito que el assert tire un mensaje claro para saber 
    // que paso cuando miremos el reporte.
    @Entonces("la solicitud debe quedar registrada en el sistema")
    public void solicitud_registrada() {
        if (!leaveApplyPage().obtenerErroresValidacion().isEmpty()) {
            String mensajeError = leaveApplyPage().obtenerErroresValidacion().stream()
                .map(WebElement::getText)
                .reduce((a, b) -> a + ", " + b).orElse("");
            Assert.fail("Formulario rechazado con error: " + mensajeError);
        }

        for (WebElement alerta : leaveApplyPage().obtenerAlertas()) {
            String textoAlerta = alerta.getText();
            if (textoAlerta.contains("Failed") || textoAlerta.contains("No Working Days")) {
                Assert.fail("OrangeHRM rechazó la solicitud: " + textoAlerta);
            }
        }

        try {
            leaveApplyPage().esperarToastExito();
            System.out.println("CASO 14 OK: Licencia registrada - toast de éxito visible");
        } catch (Exception e) {
            Assert.fail("No se recibió confirmación de registro (toast de éxito ausente). " +
                "URL actual: " + driver().getCurrentUrl());
        }
    }
}