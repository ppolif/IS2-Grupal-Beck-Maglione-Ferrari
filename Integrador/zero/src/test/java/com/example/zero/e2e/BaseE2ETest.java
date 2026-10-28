package com.example.zero.e2e;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import java.io.File;
import java.time.Duration;

/**
 * Clase base para todas las pruebas End-to-End con Selenium WebDriver.
 * Configura el arranque de Spring Boot en puerto aleatorio con el perfil de test
 * y administra el ciclo de vida del navegador.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public abstract class BaseE2ETest {

    @LocalServerPort
    protected int port;

    @org.springframework.beans.factory.annotation.Value("${selenium.headless:true}")
    protected boolean headlessConfig;

    @org.springframework.beans.factory.annotation.Value("${selenium.delay:1000}")
    protected int delayConfig;

    protected WebDriver driver;
    protected WebDriverWait wait;
    protected boolean headless;
    protected int delayMs;

    @BeforeEach
    public void setUp() {
        String sysHeadless = System.getProperty("selenium.headless");
        headless = (sysHeadless != null) ? Boolean.parseBoolean(sysHeadless) : headlessConfig;

        String sysDelay = System.getProperty("selenium.delay");
        delayMs = (sysDelay != null) ? Integer.parseInt(sysDelay) : (headless ? 0 : delayConfig);
        String browser = System.getProperty("selenium.browser", "auto").toLowerCase();

        if ("chrome".equals(browser)) {
            driver = initChromeDriver(headless);
        } else if ("firefox".equals(browser)) {
            driver = initFirefoxDriver(headless);
        } else {
            // Auto detección: probar Firefox primero, luego Chrome
            try {
                driver = initFirefoxDriver(headless);
            } catch (Exception e) {
                System.out.println(">> [E2E] No se pudo iniciar Firefox: " + e.getMessage() + ". Intentando con Chrome...");
                driver = initChromeDriver(headless);
            }
        }

        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(5));
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(20));
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    private WebDriver initFirefoxDriver(boolean headless) {
        String userHome = System.getProperty("user.home");
        File cachedGecko = new File(userHome + "/.cache/selenium/geckodriver/linux64/0.37.1/geckodriver");
        if (cachedGecko.exists() && System.getProperty("webdriver.gecko.driver") == null) {
            System.setProperty("webdriver.gecko.driver", cachedGecko.getAbsolutePath());
        }

        FirefoxOptions options = new FirefoxOptions();
        if (headless) {
            options.addArguments("-headless");
        }
        options.addArguments("--width=1920");
        options.addArguments("--height=1080");
        return new FirefoxDriver(options);
    }

    private WebDriver initChromeDriver(boolean headless) {
        String userHome = System.getProperty("user.home");
        File cachedChrome = new File(userHome + "/.cache/selenium/chromedriver/linux64/152.0.7977.82/chromedriver");
        if (cachedChrome.exists() && System.getProperty("webdriver.chrome.driver") == null) {
            System.setProperty("webdriver.chrome.driver", cachedChrome.getAbsolutePath());
        }

        ChromeOptions options = new ChromeOptions();
        if (headless) {
            options.addArguments("--headless=new");
        }
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--window-size=1920,1080");
        return new ChromeDriver(options);
    }

    @AfterEach
    public void tearDown() {
        if (driver != null) {
            try {
                driver.quit();
            } catch (Exception ignored) {
            }
        }
    }

    protected String getBaseUrl() {
        return "http://localhost:" + port;
    }

    /**
     * Inicia sesión como administrador en el sistema (/admin/login).
     */
    protected void loginComoAdmin() {
        driver.get(getBaseUrl() + "/admin/login");

        WebElement usernameInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username")));
        WebElement passwordInput = driver.findElement(By.name("password"));
        WebElement submitButton = driver.findElement(By.cssSelector("button[type='submit']"));

        usernameInput.clear();
        usernameInput.sendKeys("admin@zero.com");

        passwordInput.clear();
        passwordInput.sendKeys("admin123");

        submitButton.click();

        // Espera a que se complete la redirección al panel administrativo
        wait.until(ExpectedConditions.not(ExpectedConditions.urlContains("/admin/login")));
    }

    /**
     * Obtiene el stock actual de un producto desde la tabla de administración (/admin/products).
     *
     * @param codigoProducto Código del producto (ej: "PROD-001")
     * @return Cantidad entera de stock disponible
     */
    protected int obtenerStockDeProducto(String codigoProducto) {
        driver.get(getBaseUrl() + "/admin/products");

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("table tbody")));

        // Buscar la fila que contiene el código del producto
        WebElement fila = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.xpath("//table//tbody//tr[td[contains(text(), '" + codigoProducto + "')]]")
        ));

        // La 7ma columna corresponde al stock
        WebElement stockCell = fila.findElement(By.xpath("./td[7]"));
        String stockTexto = stockCell.getText().trim();
        return Integer.parseInt(stockTexto);
    }

    /**
     * Desplaza la vista suavemente hasta el elemento indicado si está fuera de pantalla.
     */
    protected void scrollToElement(WebElement element) {
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", element);
    }

    /**
     * Pausa la ejecución únicamente cuando NO está en modo headless (modo visual)
     * para permitir que una persona pueda ver paso a paso lo que ocurre en pantalla.
     */
    protected void pausaVisual(long millis) {
        if (!headless && millis > 0) {
            try {
                Thread.sleep(millis);
            } catch (InterruptedException ignored) {
            }
        }
    }

    protected void pausaVisual() {
        pausaVisual(delayMs);
    }
}
