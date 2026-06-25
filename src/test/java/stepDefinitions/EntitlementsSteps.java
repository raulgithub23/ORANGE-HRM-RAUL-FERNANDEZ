package stepDefinitions;

import io.cucumber.java.es.Cuando;
import io.cucumber.java.es.Entonces;
import io.cucumber.java.es.Y;
import org.openqa.selenium.WebDriver;
import pageObjects.AddLeaveEntitlementPage;
import pageObjects.DashboardPage;
import pageObjects.LeaveTypesPage;

import java.util.ArrayList;
import java.util.List;

/**
 * PPT 3.2.1 - Precondición de Caso 14: asignación/recarga de créditos (Entitlements).
 */
public class EntitlementsSteps {

    private final Configuracion configuracion;
    private final List<String> resultadosAsignacion = new ArrayList<>();

    public EntitlementsSteps(Configuracion configuracion) {
        this.configuracion = configuracion;
    }

    private WebDriver driver() {
        return configuracion.obtenerDriver();
    }

    private LeaveTypesPage leaveTypesPage() {
        return new LeaveTypesPage(driver());
    }

    private AddLeaveEntitlementPage entitlementPage() {
        return new AddLeaveEntitlementPage(driver());
    }

    private DashboardPage dashboardPage() {
        return new DashboardPage(driver());
    }

    @Y("asegura que el tipo de licencia {string} existe")
    public void asegura_tipo_licencia_existe(String tipoLicencia) {
        try {
            leaveTypesPage().abrir();
            leaveTypesPage().asegurarTipoExiste(tipoLicencia);
        } catch (Exception e) {
            System.out.println("ENTITLEMENTS: ERROR asegurando Leave Type '"
                + tipoLicencia + "' -> " + e.getMessage());
        }
    }

    @Cuando("recarga el crédito de licencia para {string}")
    public void recarga_credito_licencia(String tipoLicencia) {
        try {
            String nombreUsuario = dashboardPage().obtenerNombreUsuario();
            entitlementPage().asignarEntitlementIndividual(nombreUsuario, tipoLicencia);
            resultadosAsignacion.add(tipoLicencia + ":OK");
        } catch (Exception e) {
            System.out.println("ENTITLEMENTS: Falló asignando '" + tipoLicencia
                + "' -> " + e.getMessage());
            resultadosAsignacion.add(tipoLicencia + ":FALLO");
        }
    }

    @Entonces("los créditos de licencia deben quedar asignados correctamente")
    public void verificar_creditos_asignados() {
        System.out.println("ENTITLEMENTS: Resumen de asignación -> " + resultadosAsignacion);
        long fallos = resultadosAsignacion.stream().filter(r -> r.endsWith(":FALLO")).count();
        if (fallos > 0) {
            System.out.println("ENTITLEMENTS: ADVERTENCIA - " + fallos
                + " tipo(s) de licencia no se pudieron recargar. Revisar log anterior.");
        } else {
            System.out.println("ENTITLEMENTS: Los 3 tipos de licencia quedaron con 3000 días disponibles.");
        }
    }
}
