package pageObjects;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

/**
 * Page Object de Leave List (búsqueda y cancelación de solicitudes).
 */
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
            pausa(2000);
        }
        esperarSinSpinner();
        return true;
    }

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
