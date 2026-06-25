package pageObjects;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

/**
 * Clase base con utilidades compartidas por todos los Page Objects.
 */
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

    protected void clickWithJs(WebElement element) {
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
    }

    protected void scrollIntoView(WebElement element) {
        ((JavascriptExecutor) driver).executeScript(
            "arguments[0].scrollIntoView({block:'center'});", element);
    }

    protected void pausa(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ignored) {}
    }
}
