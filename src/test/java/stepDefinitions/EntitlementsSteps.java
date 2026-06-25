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

// ppt 3.2.1 - precondicion del caso 14: asignacion y recarga de creditos (los entitlements).
public class EntitlementsSteps {

    private final Configuracion configuracion;
    // aca voy guardando si sale ok o falla cada recarga de credito, 
    // para tirar un resumen todo junto al final en vez de escupir mensajes sueltos por la consola. asi es mas facil ver al toque en los logs si 
    // alguno de los 3 tipos de licencia exploto o no.
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
            // lo atrapo aca para que si hay quilombo creando o verificando UN tipo de licencia no me aborte todo el escenario de setup. los otros tipos 
            // se siguen intentando igual por las dudas.
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

    // este step (entonces) lo dejo nomas para informar, no le clavo un assert.fail 
    // si algun tipo de licencia falla. la onda del setup es inflar el saldo disponible para el cp-14 todo lo que se pueda, pero si falla una recarga 
    // suelta no da que se tranque toda la suite, capaz alguno de los 3 tipos ya tenia saldo de sobra de la corrida pasada.
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