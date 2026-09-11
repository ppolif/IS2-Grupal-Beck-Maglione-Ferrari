package com.example.tinder.e2e;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertTrue;

class LoginE2ETest {

    private WebDriver driver;

    @BeforeEach
    void setup() {
        ChromeOptions options = new ChromeOptions();
        options.setBinary("/usr/bin/brave-browser");
        driver = new ChromeDriver(options);

        // Le damos al robot hasta 5 segundos para encontrar los elementos por si la página tarda en cargar
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(5));
    }

    @Test
    void login_UsuarioValido_IniciaSesionYRedirige() {
        // 1. Navegamos a TU aplicación
        driver.get("http://localhost:9000/login");

        // 2. Buscamos los inputs y escribimos.
        WebElement inputEmail = driver.findElement(By.name("email"));
        inputEmail.sendKeys("augustobeck472@gmail.com");

        WebElement inputClave = driver.findElement(By.name("clave"));
        inputClave.sendKeys("1234567");

        // 3. Buscamos el botón por su tipo y hacemos clic
        WebElement botonEntrar = driver.findElement(By.cssSelector("button[type='submit']"));
        botonEntrar.click();

        // 4. Verificamos el éxito leyendo la URL donde terminó el robot
        String urlActual = driver.getCurrentUrl();

        // Esperamos que Spring Security lo haya mandado al inicio
        assertTrue(urlActual.endsWith("/inicio") || urlActual.equals("http://localhost:8080/"));
    }

    @AfterEach
    void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
}