package pageObjects;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

// page object de leave list (para buscar y cancelar las licencias)
public class LeaveListPage extends BasePage {

    private static final By BTN_SEARCH = By.xpath(
        "//button[@type='submit' and normalize-space()='Search']");
    private static final By FILAS = By.xpath(
        "//div[contains(@class,'oxd-table-body')]" +
        "//div[contains(@class,'oxd-table-row')]");
    private static final By BTN_CANCEL = By.xpath("//button[normalize-space()='Cancel']");
    private static final By BTN_YES_CONFIRM = By.xpath("//button[normalize-space()='Yes, Confirm']");
    private static final By TOAST = By.cssSelector(".oxd-toast");

    public LeaveListPage(WebDriver driver) {
        super(driver, 20, 30);
    }

    public void abrir() {
        driver.get(BASE_URL + "/leave/viewLeaveList");
    }

    public void buscar() {
        WebElement btnSearch = new WebDriverWait(driver, Duration.ofSeconds(20))
            .until(ExpectedConditions.elementToBeClickable(BTN_SEARCH));
        btnSearch.click();
        esperarTablaCargada();
    }

    private void esperarTablaCargada() {
        new WebDriverWait(driver, Duration.ofSeconds(20)).until(d ->
            !d.findElements(FILAS).isEmpty()
            || !d.findElements(By.xpath("//*[contains(text(),'No Records Found')]")).isEmpty());
    }

    public int contarFilas() {
        return driver.findElements(FILAS).size();
    }

    // cancela la primera licencia que aparece en la tabla. lo uso para limpiar despues de correr 
    // el cp-14 (mira el metodo cancelarSolicitudesPendientes): cada cosa que mete el cp-14 hay que 
    // volarla despues para que la demo publica no se llene de basura de otras pruebas y nos deje sin saldo
    public boolean cancelarPrimeraSolicitud() {
        List<WebElement> filas = driver.findElements(FILAS);
        if (filas.isEmpty()) {
            return false;
        }

        WebElement primeraFila = filas.get(0);
        scrollIntoView(primeraFila);
        pausa(400);

        WebElement checkbox = primeraFila.findElement(By.xpath(".//input[@type='checkbox']"));
        clickWithJs(checkbox);

        if (!clickCancelButton()) {
            return false;
        }
        pausa(600);

        WebElement btnYesConfirm = new WebDriverWait(driver, Duration.ofSeconds(10))
            .until(ExpectedConditions.elementToBeClickable(BTN_YES_CONFIRM));
        btnYesConfirm.click();

        try {
            new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.visibilityOfElementLocated(TOAST));
            new WebDriverWait(driver, Duration.ofSeconds(8))
                .until(ExpectedConditions.invisibilityOfElementLocated(TOAST));
        } catch (Exception e) {
            // si el cartelito verde (toast) no sale o se queda pegado, le clavo una pausa de un par 
            // de segundos por las dudas. prefiero perder tiempo aca a que se me rompa el test por 
            // intentar hacer otra cosa mientras todavia esta cargando
            pausa(2000);
        }
        esperarSinSpinner();
        return true;
    }

    // meto dos formas de buscar y darle al boton cancel. la primera es con el xpath normal. si orange 
    // se pone mañoso o tarda en renderizar, la segunda busca todos los botones con js hasta encontrar 
    // el que dice "Cancel". hago esto porque el boton recien aparece de la nada cuando marcas el checkbox
    private boolean clickCancelButton() {
        try {
            WebElement btnCancel = new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.presenceOfElementLocated(BTN_CANCEL));
            clickWithJs(btnCancel);
            return true;
        } catch (Exception e1) {
            try {
                ((JavascriptExecutor) driver).executeScript(
                    "var btns = document.querySelectorAll('button');" +
                    "for(var i=0; i<btns.length; i++){" +
                    "  if(btns[i].innerText.trim()==='Cancel' && btns[i].offsetParent!==null){" +
                    "    btns[i].click(); break;" +
                    "  }" +
                    "}");
                return true;
            } catch (Exception e3) {
                return false;
            }
        }
    }

    // postcondicion del cp-14: vuela todas las licencias que hayan quedado colgadas, le pongo un tope 
    // (maxCancelaciones) para no quedarme trabado en un loop infinito si falla algo. hay que limpiar si o si 
    // porque al ser compartida la demo publica, si no borramos se nos acumulan los datos de prueba y 
    // rompe todo para los demas o para cuando queramos correr la suite de nuevo
    public void cancelarSolicitudesPendientes(int maxCancelaciones) {
        pausa(2000);
        abrir();
        buscar();

        int cancelados = 0;
        while (cancelados < maxCancelaciones) {
            if (contarFilas() == 0) {
                System.out.println("POSTCONDICION CP-14: Tabla limpia. Total cancelados: " + cancelados);
                break;
            }
            System.out.println("POSTCONDICION CP-14: Filas en tabla: " + contarFilas());
            if (!cancelarPrimeraSolicitud()) {
                System.out.println("POSTCONDICION CP-14: No se pudo cancelar la solicitud.");
                break;
            }
            cancelados++;
            System.out.println("POSTCONDICION CP-14: Registro #" + cancelados + " cancelado.");
        }
    }
}