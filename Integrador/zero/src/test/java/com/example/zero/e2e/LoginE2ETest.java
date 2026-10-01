package com.example.zero.e2e;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("E2E - Pruebas de Autenticación (Login)")
public class LoginE2ETest extends BaseE2ETest {

    @Test
    @DisplayName("Debe iniciar sesión correctamente como Administrador y redirigir al panel")
    void testLoginAdminExitoso() {
        driver.get(getBaseUrl() + "/admin/login");
        pausaVisual();

        WebElement emailInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username")));
        WebElement passwordInput = driver.findElement(By.name("password"));
        WebElement submitBtn = driver.findElement(By.cssSelector("button[type='submit']"));

        emailInput.sendKeys("admin@zero.com");
        pausaVisual(500);

        passwordInput.sendKeys("admin123");
        pausaVisual(500);

        submitBtn.click();

        // Debe redirigir fuera del login hacia el dashboard o registro de venta
        wait.until(ExpectedConditions.or(
                ExpectedConditions.urlContains("/admin/registrar-venta"),
                ExpectedConditions.urlContains("/admin")
        ));
        pausaVisual();

        assertTrue(driver.getCurrentUrl().contains("/admin"), "La URL actual debe pertenecer al panel administrativo");
        assertFalse(driver.getCurrentUrl().contains("/admin/login"), "No debe permanecer en la página de login");

        // Validar que se visualiza el contenido del panel (sidebar o navbar administrativo)
        WebElement panelElement = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("aside, nav, h1")
        ));
        assertNotNull(panelElement, "El layout administrativo debe estar presente");
        pausaVisual(1500);
    }

    @Test
    @DisplayName("Debe mostrar error al intentar iniciar sesión con credenciales inválidas")
    void testLoginAdminCredencialesInvalidas() {
        driver.get(getBaseUrl() + "/admin/login");
        pausaVisual();

        WebElement emailInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username")));
        WebElement passwordInput = driver.findElement(By.name("password"));
        WebElement submitBtn = driver.findElement(By.cssSelector("button[type='submit']"));

        emailInput.sendKeys("admin@zero.com");
        pausaVisual(500);

        passwordInput.sendKeys("clave_erronea_123");
        pausaVisual(500);

        submitBtn.click();

        // Debe mantenerse en login y mostrar alerta de error
        WebElement alertaError = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector(".alert-danger")
        ));
        pausaVisual(1500);

        assertTrue(driver.getCurrentUrl().contains("/login"), "La URL debe seguir siendo la de login");
        assertTrue(alertaError.isDisplayed(), "El mensaje de error debe ser visible");
        assertFalse(alertaError.getText().isBlank(), "El mensaje de error debe contener texto explicativo");
    }
}
