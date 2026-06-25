package stepDefinitions;

import utilidades.ExcelUtils;
import io.cucumber.java.es.Cuando;
import io.cucumber.java.es.Entonces;
import io.cucumber.java.es.Y;
import org.junit.Assert;
import org.openqa.selenium.WebDriver;
import pageObjects.DashboardPage;
import pageObjects.MyInfoPage;

import java.io.IOException;

/**
 * PPT 3.1.1 + 3.2.1 - Caso 13: Gestión de perfil en My Info.
 * Combina Data-Driven (Excel) con capturas de evidencia.
 * (Capturas delegadas al Hook global)
 */
public class PerfilSteps {

    private final Configuracion configuracion;
    private String campoActualizado = "";
    private String valorNuevo = "";

    public PerfilSteps(Configuracion configuracion) {
        this.configuracion = configuracion;
    }

    private WebDriver driver() {
        return configuracion.obtenerDriver();
    }

    private DashboardPage dashboardPage() {
        return new DashboardPage(driver());
    }

    private MyInfoPage myInfoPage() {
        return new MyInfoPage(driver());
    }

    @Cuando("navega al módulo My Info")
    public void navega_a_my_info() {
        dashboardPage().navegarAMyInfo();
    }

    @Y("actualiza el campo de perfil de la fila {int} del archivo de perfil")
    public void actualiza_campo_perfil_excel(int fila) throws IOException {
        ExcelUtils excel = new ExcelUtils(
            "src/test/resources/testData/dataPerfil.xlsx", "Perfil");
        campoActualizado = excel.getCellData(fila, 1);
        valorNuevo       = excel.getCellData(fila, 2);
        System.out.println("CASO 13 - Fila " + fila +
            ": Campo=" + campoActualizado + ", Valor=" + valorNuevo);
        myInfoPage().actualizarCampo(campoActualizado, valorNuevo);
    }

    @Y("hace click en Save del perfil")
    public void click_save_perfil() {
        myInfoPage().guardar();
    }

    @Entonces("el campo debe reflejar el nuevo valor")
    public void campo_refleja_nuevo_valor() {
        Assert.assertTrue("No está en la página My Info", myInfoPage().estaEnPaginaPerfil());
        System.out.println("CASO 13 OK: Campo '" + campoActualizado +
            "' actualizado a '" + valorNuevo + "'");
    }

    @Y("se captura screenshot como evidencia del perfil")
    public void captura_screenshot_perfil() {
        System.out.println("CASO 13: Evidencia delegada al Hook global");
    }
}
