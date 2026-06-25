package pageObjects;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

// page object del form para agregar un empleado nuevo (pim > add employee)
public class AddEmployeePage extends BasePage {

    private static final By BTN_ADD = By.cssSelector(".orangehrm-header-container .oxd-button--secondary");
    private static final By INPUT_FIRST_NAME = By.name("firstName");
    private static final By INPUT_LAST_NAME = By.name("lastName");
    private static final By BTN_SAVE = By.xpath("//button[normalize-space()='Save']");

    public AddEmployeePage(WebDriver driver) {
        super(driver);
    }

    public void clickAgregarEmpleado() {
        esperarSinSpinner();
        waitClickable(BTN_ADD).click();
        waitVisible(INPUT_FIRST_NAME);
    }

    public void completarNombre(String nombre, String apellido) {
        // le meto un clear() antes del sendkeys porque a veces el campo te trae un placeholder o el navegador te autocompleta algo, asi que lo limpio a mano para asegurarme 
        // de que quede tal cual lo que viene del excel y no se junte con basura
        WebElement inputNombre = waitVisible(INPUT_FIRST_NAME);
        inputNombre.clear();
        inputNombre.sendKeys(nombre);
        WebElement inputApellido = driver.findElement(INPUT_LAST_NAME);
        inputApellido.clear();
        inputApellido.sendKeys(apellido);
    }

    public void guardar() {
        esperarSinSpinner();
        waitClickable(BTN_SAVE).click();
        esperarSinSpinner();
    }

    // despues de darle a guardar, orange te tira directo a la ficha del loco que acabas de crear 
    // (personal details). espero a que cargue esa url asi confirmo que se guardo bien antes de seguir con otra cosa
    public void esperarFichaPersonal() {
        longWait.until(ExpectedConditions.urlContains("/pim/viewPersonalDetails"));
    }
}