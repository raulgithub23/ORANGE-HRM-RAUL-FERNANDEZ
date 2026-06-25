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

/**
 * PPT 3.2.1 + 3.3.1 - Caso 14: Registrar licencia con datos desde Excel.
 */
public class LicenciasSteps {

    private final Configuracion configuracion;
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

    @After(value = "@caso14", order = 1)
    public void cancelarSolicitudRegistrada() {
        System.out.println("POSTCONDICION CP-14: Iniciando limpieza de solicitudes pendientes.");
        try {
            leaveListPage().cancelarSolicitudesPendientes(10);
        } catch (Exception e) {
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
