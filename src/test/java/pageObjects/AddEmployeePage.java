package pageObjects;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

/**
 * Page Object del formulario de alta de empleado (PIM > Add Employee).
 */
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

    public void esperarFichaPersonal() {
        longWait.until(ExpectedConditions.urlContains("/pim/viewPersonalDetails"));
    }
}
