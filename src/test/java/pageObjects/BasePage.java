package pageObjects;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

// clase base con cosas que comparten todos los page objects
public abstract class BasePage {

    public static final String BASE_URL =
        "https://opensource-demo.orangehrmlive.com/web/index.php";

    protected final WebDriver driver;
    protected final WebDriverWait wait;
    protected final WebDriverWait longWait;

    protected BasePage(WebDriver driver) {
        this(driver, 15, 25);
    }

    protected BasePage(WebDriver driver, int waitSeconds, int longWaitSeconds) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(waitSeconds));
        this.longWait = new WebDriverWait(driver, Duration.ofSeconds(longWaitSeconds));
    }

    protected void esperarSinSpinner() {
        // orange muestra un spinner (.oxd-loading-spinner-container) cuando carga cosas ajax. si le damos clic mientras gira, selenium puede fallar 
        // porque el elemento aun no se puede tocar. el catch vacio lo deje asi a proposito, si el spinner no sale (onda cargo muy rapido) no pasa nada, no es error
        try {
            new WebDriverWait(driver, Duration.ofSeconds(5))
                .until(ExpectedConditions.invisibilityOfElementLocated(
                    By.cssSelector(".oxd-loading-spinner-container")));
        } catch (Exception ignored) {}
    }

    protected void waitForUrlContains(String fragment) {
        wait.until(ExpectedConditions.urlContains(fragment));
    }

    protected WebElement waitVisible(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    protected WebElement waitClickable(By locator) {
        return wait.until(ExpectedConditions.elementToBeClickable(locator));
    }

    protected void click(By locator) {
        waitClickable(locator).click();
    }

    // clic con js en vez del click normal: salva bastante cuando el elemento esta ahi pero algo de la ui de orangehrm lo tapa 
    // (un tooltip o algo) y selenium se queja de que no es clickeable
    protected void clickWithJs(WebElement element) {
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
    }

    // pone el elemento en el centro de la pantalla antes de hacerle cosas. sirve mas que nada en tablas grandes o forms donde el elemento 
    // esta en el dom pero no se ve en pantalla, asi evitamos que tire timeout por no scrollear antes
    protected void scrollIntoView(WebElement element) {
        ((JavascriptExecutor) driver).executeScript(
            "arguments[0].scrollIntoView({block:'center'});", element);
    }

    // pausa a lo bruto con sleep. la uso solo cuando no queda otra en partes muy puntuales donde las esperas de selenium se quedan cortas 
    // con las animaciones raras de orangehrm (por ej cuando se abre un dropdown para autocompletar)
    protected void pausa(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ignored) {}
    }
}