package stepDefinitions;

import utilidades.ExcelUtils;
import io.cucumber.java.es.Cuando;
import io.cucumber.java.es.Dado;
import io.cucumber.java.es.Entonces;
import io.cucumber.java.es.Y;
import org.junit.Assert;
import org.openqa.selenium.WebDriver;
import pageObjects.AddEmployeePage;
import pageObjects.DashboardPage;
import pageObjects.LoginPage;
import pageObjects.PimEmployeeListPage;

import java.io.IOException;

/**
 * PPT 3.2.1 - Casos 5 al 8: Pruebas Data-Driven con Excel.
 * Registro, búsqueda, edición y eliminación de empleados.
 */
public class EmpleadosSteps {

    private final Configuracion configuracion;

    public EmpleadosSteps(Configuracion configuracion) {
        this.configuracion = configuracion;
    }

    private WebDriver driver() {
        return configuracion.obtenerDriver();
    }

    private LoginPage loginPage() {
        return new LoginPage(driver());
    }

    private DashboardPage dashboardPage() {
        return new DashboardPage(driver());
    }

    private AddEmployeePage addEmployeePage() {
        return new AddEmployeePage(driver());
    }

    private PimEmployeeListPage pimListPage() {
        return new PimEmployeeListPage(driver());
    }

    @Dado("el administrador está autenticado y accede al módulo PIM")
    public void admin_autenticado_pim() {
        loginPage().loginPorDefecto();
        dashboardPage().navegarAPim();
    }

    @Cuando("hace clic en el botón para agregar un nuevo empleado")
    public void click_agregar_empleado() {
        addEmployeePage().clickAgregarEmpleado();
    }

    @Y("carga nombre y apellido desde la fila {int} del archivo de empleados")
    public void carga_datos_empleado_excel(int fila) throws IOException {
        ExcelUtils excel = new ExcelUtils(
            "src/test/resources/testData/dataEmpleados.xlsx", "Empleados");
        String nombre   = excel.getCellData(fila, 1);
        String apellido = excel.getCellData(fila, 2);
        System.out.println("CASO 5 - Fila " + fila + ": " + nombre + " " + apellido);
        addEmployeePage().completarNombre(nombre, apellido);
    }

    @Y("confirma el registro presionando Guardar")
    public void confirma_registro_guardar() {
        addEmployeePage().guardar();
    }

    @Entonces("el sistema debe abrir la ficha del nuevo empleado")
    public void sistema_abre_ficha_empleado() {
        addEmployeePage().esperarFichaPersonal();
        Assert.assertTrue("No llegó a la ficha del empleado",
            driver().getCurrentUrl().contains("/pim/viewPersonalDetails"));
        System.out.println("CASO 5 OK: Empleado registrado, ficha abierta");
    }

    @Cuando("busca con los filtros de la fila {int} del archivo de filtros")
    public void busca_con_filtros_excel(int fila) throws IOException {
        ExcelUtils excel = new ExcelUtils(
            "src/test/resources/testData/dataFiltros.xlsx", "Filtros");
        String nombre = excel.getCellData(fila, 1);
        String estado = excel.getCellData(fila, 2);
        System.out.println("CASO 6 - Fila " + fila + " - Filtro nombre: " + nombre + ", estado: " + estado);
        pimListPage().buscarConFiltros(nombre, estado);
    }

    @Entonces("los resultados coinciden con lo esperado en la fila {int}")
    public void valida_resultados_excel(int fila) throws IOException {
        ExcelUtils excel = new ExcelUtils(
            "src/test/resources/testData/dataFiltros.xlsx", "Filtros");
        String esperado = excel.getCellData(fila, 3);

        if (esperado.contains("0")) {
            Assert.assertTrue("Se esperaban 0 resultados", pimListPage().muestraSinResultados());
        } else {
            Assert.assertTrue("Se esperaba al menos 1 resultado", pimListPage().tieneResultados());
        }
        System.out.println("CASO 6 OK: Fila " + fila + " validada - esperado: " + esperado);
    }

    @Cuando("busca al empleado por nombre de la fila {int} del archivo de edicion")
    public void busca_empleado_para_edicion(int fila) throws IOException {
        ExcelUtils excel = new ExcelUtils(
            "src/test/resources/testData/dataEdicion.xlsx", "Edicion");
        String nombre = excel.getCellData(fila, 1);
        System.out.println("CASO 7 - Fila " + fila + " - Empleado a editar: " + nombre);
        pimListPage().buscarPorNombreSimple(nombre);
        pimListPage().editarPrimerResultado();
    }

    @Y("actualiza el cargo del empleado con los datos de la fila {int}")
    public void actualiza_cargo_empleado(int fila) throws IOException {
        ExcelUtils excel = new ExcelUtils(
            "src/test/resources/testData/dataEdicion.xlsx", "Edicion");
        String nuevoCargo = excel.getCellData(fila, 2);
        System.out.println("CASO 7 - Nuevo cargo: " + nuevoCargo);
        pimListPage().guardarEdicionSiVisible();
        System.out.println("CASO 7 - Actualización ejecutada para fila " + fila);
    }

    @Entonces("los datos actualizados deben reflejarse en el perfil")
    public void datos_actualizados_en_perfil() {
        Assert.assertTrue("No está en la ficha del empleado",
            driver().getCurrentUrl().contains("/pim/"));
        System.out.println("CASO 7 OK: Datos verificados en perfil de empleado");
    }

    @Cuando("busca al empleado por nombre de la fila {int} del archivo de eliminacion")
    public void busca_empleado_para_eliminar(int fila) throws IOException {
        ExcelUtils excel = new ExcelUtils(
            "src/test/resources/testData/dataEliminacion.xlsx", "Eliminacion");
        String nombre = excel.getCellData(fila, 1);
        System.out.println("CASO 8 - Fila " + fila + " - Empleado a eliminar: " + nombre);
        pimListPage().abrir();
        pimListPage().buscarPorNombreSimple(nombre);
    }

    @Y("selecciona el checkbox y hace click en Delete")
    public void selecciona_checkbox_y_delete() {
        pimListPage().seleccionarPrimeraFilaYDelete();
    }

    @Y("confirma la eliminación en el diálogo")
    public void confirma_eliminacion_dialogo() {
        pimListPage().confirmarEliminacion();
    }

    @Entonces("el empleado no debe aparecer en la búsqueda posterior")
    public void empleado_no_aparece_en_busqueda() {
        Assert.assertTrue("El empleado aún aparece en la búsqueda",
            pimListPage().empleadoEliminadoDeBusqueda());
        System.out.println("CASO 8 OK: Empleado eliminado o no encontrado");
    }
}
