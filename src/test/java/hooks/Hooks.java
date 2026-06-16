package hooks;

import utilidades.Utility;
import io.cucumber.java.After;
import io.cucumber.java.Scenario;
import stepDefinitions.Configuracion;

import java.io.IOException;

/**
 * PPT 3.1.1 - Caso 4: Hook @After que captura screenshot automáticamente
 * cuando cualquier escenario falla.
 * El nombre del archivo incluye el nombre del escenario y timestamp.
 */
public class Hooks {

    private final Configuracion configuracion;

    public Hooks(Configuracion configuracion) {
        this.configuracion = configuracion;
    }

    @After(order = 1)
    public void afterScenario(Scenario scenario) throws IOException {
        if (scenario.isFailed() && configuracion.obtenerDriver() != null) {
            // Limpiar caracteres especiales del nombre del escenario
            String nombre = scenario.getName().replaceAll("[^a-zA-Z0-9_\\-]", "_");
            String ruta = "evidencias/FALLO_" + nombre + "_" + Utility.GetTimeStampValue() + ".png";
            Utility.captureScreenShot(configuracion.obtenerDriver(), ruta);
            System.out.println("CP-04 Aplicado: Screenshot de fallo guardado -> " + ruta);
        }
    }
}
