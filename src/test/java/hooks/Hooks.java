package hooks;

import utilidades.Utility;
import io.cucumber.java.After;
import io.cucumber.java.Scenario;
import stepDefinitions.Configuracion;

import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;

import java.io.IOException;

/**
 * Hook @After que captura screenshot al finalizar cada escenario (PASS o FAIL).
 * Guarda la imagen en disco bajo evidencias/ y la adjunta al reporte HTML de Cucumber.
 * El nombre incluye: estado (PASS/FAIL), nombre del escenario y timestamp.
 */
public class Hooks {

    private final Configuracion configuracion;

    public Hooks(Configuracion configuracion) {
        this.configuracion = configuracion;
    }

    @After(order = 1)
    public void afterScenario(Scenario scenario) throws IOException {
        if (configuracion.obtenerDriver() == null) return;

        String estado = scenario.isFailed() ? "FAIL" : "PASS";
        String nombre = scenario.getName().replaceAll("[^a-zA-Z0-9_\\-]", "_");
        String timestamp = Utility.GetTimeStampValue();
        String nombreArchivo = estado + "_" + nombre + "_" + timestamp;

        // Capturar una sola vez y reutilizar para disco y reporte
        byte[] screenshot = ((TakesScreenshot) configuracion.obtenerDriver())
                .getScreenshotAs(OutputType.BYTES);

        // 1. Guardar en disco
        Utility.captureScreenShot(configuracion.obtenerDriver(), "evidencias/" + nombreArchivo + ".png");

        // 2. Adjuntar al reporte HTML de Cucumber
        scenario.attach(screenshot, "image/png", nombreArchivo);

        System.out.println("Evidencia guardada (" + estado + ") -> evidencias/" + nombreArchivo + ".png");
    }
}