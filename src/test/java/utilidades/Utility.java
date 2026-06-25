package utilidades;

import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * PPT 3.1.1 - Utilidad para capturas de pantalla con timestamp.
 */
public class Utility {

    /**
     * Genera un timestamp con formato yyyyMMdd_HHmmss.
     */
    public static String GetTimeStampValue() {
        return new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
    }

    /**
     * Toma screenshot usando el método nativo de Selenium (TakesScreenshot)
     * y lo guarda en la ruta indicada. Crea el directorio automáticamente
     * si no existe.
     *
     * Nota: este método NO se usa actualmente en ningún step ni hook de la
     * suite. Se mantiene en el código como referencia histórica: fue el
     * mecanismo original de captura de evidencia, pero se reemplazó por
     * java.awt.Robot en Hooks.java porque TakesScreenshot devolvía imágenes
     * en blanco en esta máquina (ver el comentario de clase en Hooks.java
     * para el detalle completo del problema y la solución).
     *
     * @param driver       WebDriver activo
     * @param rutaDestino  Ruta relativa, p.ej. "evidencias/CP01_LoginExitoso_20260101_120000.png"
     */
    public static void captureScreenShot(WebDriver driver, String rutaDestino) throws IOException {
        File carpeta = new File(rutaDestino).getParentFile();
        if (carpeta != null && !carpeta.exists()) {
            carpeta.mkdirs();
        }
        File src = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
        Files.copy(src.toPath(), Paths.get(rutaDestino));
        System.out.println("Screenshot guardado: " + rutaDestino);
    }
}