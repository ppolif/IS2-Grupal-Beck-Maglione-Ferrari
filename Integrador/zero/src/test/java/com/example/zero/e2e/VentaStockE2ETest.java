package com.example.zero.e2e;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("E2E - Registro de Venta y Disminución de Stock")
public class VentaStockE2ETest extends BaseE2ETest {

    @Test
    @DisplayName("Debe registrar una venta como admin y disminuir el stock del producto vendido")
    void testRegistrarVentaDisminuyeStock() {
        // 1. Iniciar sesión como administrador
        loginComoAdmin();

        // 2. Elegir un producto de prueba y consultar su stock inicial en /admin/products
        String codigoProducto = "PROD-001";
        int stockInicial = obtenerStockDeProducto(codigoProducto);
        assertTrue(stockInicial > 0, "El producto debe tener stock disponible para vender");
        pausaVisual();

        // 3. Navegar a la página de registro de venta
        driver.get(getBaseUrl() + "/admin/registrar-venta");
        pausaVisual();

        // 4. Completar datos del comprador
        WebElement dniInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("clienteDni")));
        WebElement nombreInput = driver.findElement(By.id("clienteNombre"));
        WebElement apellidoInput = driver.findElement(By.id("clienteApellido"));
        WebElement emailInput = driver.findElement(By.id("clienteEmail"));

        dniInput.clear();
        dniInput.sendKeys("99887766");

        nombreInput.clear();
        nombreInput.sendKeys("Carlos");

        apellidoInput.clear();
        apellidoInput.sendKeys("Gomez");

        emailInput.clear();
        emailInput.sendKeys("carlos.gomez@test.com");
        pausaVisual(500);

        // 5. Seleccionar producto en el formulario
        WebElement selectProdElem = driver.findElement(By.id("selectorProducto"));
        Select selectProducto = new Select(selectProdElem);

        boolean encontrado = false;
        for (WebElement opt : selectProducto.getOptions()) {
            if (opt.getText().contains(codigoProducto)) {
                selectProducto.selectByVisibleText(opt.getText());
                encontrado = true;
                break;
            }
        }
        assertTrue(encontrado, "El producto " + codigoProducto + " debe estar disponible en el selector");
        pausaVisual(500);

        // 6. Indicar cantidad a vender
        int cantidadAVender = 2;
        WebElement cantidadInput = driver.findElement(By.id("selectorCantidad"));
        cantidadInput.clear();
        cantidadInput.sendKeys(String.valueOf(cantidadAVender));
        pausaVisual(500);

        // 7. Presionar el botón "Agregar" para incorporar el producto al carrito
        WebElement btnAgregar = driver.findElement(By.id("btnAgregarProducto"));
        scrollToElement(btnAgregar);
        btnAgregar.click();
        pausaVisual();

        // Verificar que el producto se agregó a la tabla del carrito
        WebElement filaCarrito = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.xpath("//table[@id='tablaCarrito']//tbody//tr[not(@id='filaVacia')]")
        ));
        assertNotNull(filaCarrito, "El carrito debe contener el producto agregado");

        // 8. Confirmar y registrar la venta
        WebElement btnConfirmar = driver.findElement(By.cssSelector("#formVenta button[type='submit']"));
        scrollToElement(btnConfirmar);
        btnConfirmar.click();

        // 9. Esperar redirección al listado de órdenes con mensaje de éxito
        wait.until(ExpectedConditions.or(
                ExpectedConditions.urlContains("/admin/orders"),
                ExpectedConditions.urlContains("/admin/tables-basic")
        ));

        WebElement alertaExito = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector(".alert-success")
        ));
        assertTrue(alertaExito.isDisplayed(), "Debe mostrarse mensaje de éxito tras registrar la venta");
        pausaVisual();

        // 10. Validar en /admin/products que el stock del producto disminuyó exactamente en la cantidad vendida
        int stockFinal = obtenerStockDeProducto(codigoProducto);
        pausaVisual(1500);
        assertEquals(stockInicial - cantidadAVender, stockFinal,
                "El stock final debe ser exactamente el inicial menos la cantidad vendida");
    }
}
