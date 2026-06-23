package hooks;

import utilidades.Utility;
import io.cucumber.java.After;
import io.cucumber.java.Scenario;
import stepDefinitions.Configuracion;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.Duration;
import javax.imageio.ImageIO;

public class Hooks {

    private final Configuracion configuracion;

    public Hooks(Configuracion configuracion) {
        this.configuracion = configuracion;
    }

    @After(order = 10)
    public void afterScenario(Scenario scenario) throws IOException {
        if (configuracion.obtenerDriver() == null) return;

        new WebDriverWait(configuracion.obtenerDriver(), Duration.ofSeconds(3))
            .until(driver -> ((JavascriptExecutor) driver)
                .executeScript("return document.readyState").equals("complete"));

        String estado = scenario.isFailed() ? "FAIL" : "PASS";
        String nombre = scenario.getName().replaceAll("[^a-zA-Z0-9_\\-]", "_");
        String timestamp = Utility.GetTimeStampValue();
        String nombreArchivo = estado + "_" + nombre + "_" + timestamp;

        try {
            Robot robot = new Robot();
            Rectangle screenRect = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
            BufferedImage captura = robot.createScreenCapture(screenRect);

            File carpeta = new File("evidencias");
            if (!carpeta.exists()) carpeta.mkdirs();

            File archivo = new File("evidencias/" + nombreArchivo + ".png");
            ImageIO.write(captura, "PNG", archivo);

            byte[] bytes = java.nio.file.Files.readAllBytes(archivo.toPath());
            scenario.attach(bytes, "image/png", nombreArchivo);

            System.out.println("Evidencia guardada (" + estado + ") -> evidencias/" + nombreArchivo + ".png");

        } catch (Exception e) {
            System.out.println("Error capturando screenshot: " + e.getMessage());
        }
    }
}