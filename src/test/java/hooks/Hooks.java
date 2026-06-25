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

// hook global que corre despues de CADA escenario (no importa de que modulo sea) y saca la foto para el reporte de qa. por que uso robot en vez del screenshot de selenium: el nativo (driver.getscreenshotas) 
// solo saca lo de adentro del chrome y en esta pc salia todo negro o blanco (como de 7kb y nada mas), seguro por algun drama con la aceleracion de video. el de robot en cambio saca una captura 
// a toda la pantalla del sistema asi que agarra si o si lo que se ve. lo malo es que si tenes otra ventana abierta encima del browser va a salir en la foto jaja.
public class Hooks {

    private final Configuracion configuracion;

    public Hooks(Configuracion configuracion) {
        this.configuracion = configuracion;
    }

    // le pongo order = 10 para que corra ANTES del de configuracion.java que tiene 0, cucumber los corre de mayor a menor. esta hecho a proposito asi el driver sigue 
    // abierto para sacarle la foto antes de que se cierre todo.
    @After(order = 10)
    public void afterScenario(Scenario scenario) throws IOException {
        if (configuracion.obtenerDriver() == null) return;

        // espero a que cargue toda la pagina antes de gatillar la foto, para no sacar 
        // una pantalla por la mitad o con el spinner
        new WebDriverWait(configuracion.obtenerDriver(), Duration.ofSeconds(3))
            .until(driver -> ((JavascriptExecutor) driver)
                .executeScript("return document.readyState").equals("complete"));

        // armo el nombre del archivo con el pass/fail, limpio el nombre del escenario (sin tildes ni cosas raras porque windows llora) y le clavo un timestamp 
        // para que no me pise fotos viejas si lo corro de nuevo
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

            // aparte de guardar el png ahi en el disco, lo meto como attachment en el escenario para que salga metido directo en el html de cucumber (asi no queda solo tirado en la carpeta)
            byte[] bytes = java.nio.file.Files.readAllBytes(archivo.toPath());
            scenario.attach(bytes, "image/png", nombreArchivo);

            System.out.println("Evidencia guardada (" + estado + ") -> evidencias/" + nombreArchivo + ".png");

        } catch (Exception e) {
            // atrapo cualquier error a proposito: si falla la foto NO tiene que reventar el test que capaz si habia pasado bien, es tema aparte de la evidencia
            System.out.println("Error capturando screenshot: " + e.getMessage());
        }
    }
}