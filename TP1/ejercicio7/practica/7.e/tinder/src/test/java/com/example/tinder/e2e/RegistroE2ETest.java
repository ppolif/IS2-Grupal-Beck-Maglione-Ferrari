package com.example.tinder.e2e;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.Select;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertTrue;

class RegistroE2ETest {

    private WebDriver driver;

    @BeforeEach
    void setup() {
        ChromeOptions options = new ChromeOptions();
        options.setBinary("/usr/bin/brave-browser");
        driver = new ChromeDriver(options);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(5));
    }

    @Test
    void registro_LlenarFormularioValido_RegistraYRedirige() {
        // 1. Navegamos a la ruta del registro
        driver.get("http://localhost:9000/registro");

        // 2. Llenamos los campos de texto
        driver.findElement(By.name("nombre")).sendKeys("Robot");
        driver.findElement(By.name("apellido")).sendKeys("Selenium");

        // Usamos un timestamp para generar un email distinto en cada prueba
        // y evitar el error de "email ya registrado" en la base de datos
        String emailUnico = "robot" + System.currentTimeMillis() + "@test.com";
        driver.findElement(By.name("email")).sendKeys(emailUnico);

        // 3. Seleccionamos la Zona del <select>
        WebElement comboZona = driver.findElement(By.name("idZona"));
        Select selectZona = new Select(comboZona);
        // Seleccionamos la primera zona disponible en la lista (índice 0 es la primera opción)
        selectZona.selectByIndex(0);

        // 4. Llenamos las contraseñas
        driver.findElement(By.name("clave")).sendKeys("1234567");
        driver.findElement(By.name("repetirClave")).sendKeys("1234567");

        // 5. Presionamos el botón "Registrarme"
        driver.findElement(By.cssSelector("button[type='submit']")).click();

        String urlActual = driver.getCurrentUrl();
        System.out.println("=== LA URL FINAL FUE: " + urlActual + " ===");

        java.util.List<WebElement> errores = driver.findElements(By.cssSelector("p[style='color:red;']"));

        if (!errores.isEmpty()) {
            // Si la lista no está vacía, significa que apareció un error en la pantalla
            System.out.println("=== EL REGISTRO FALLÓ. EL SERVIDOR DICE: ===");
            System.out.println(errores.get(0).getText());
        }

        // 7. La aserción final: El test pasa a verde solo si NO hay errores en pantalla
        assertTrue(errores.isEmpty(), "El registro falló porque apareció un error en la pantalla.");
    }

    @AfterEach
    void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
}