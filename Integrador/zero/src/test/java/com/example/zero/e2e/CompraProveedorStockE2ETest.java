package com.example.zero.e2e;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("E2E - Registro de Compra a Proveedor y Aumento de Stock al Entregar")
public class CompraProveedorStockE2ETest extends BaseE2ETest {

    @Test
    @DisplayName("Debe registrar orden de compra en estado pendiente y aumentar el stock al marcar como entregada")
    void testRegistrarCompraYEntregarAumentaStock() {
        // 1. Iniciar sesión como administrador
        loginComoAdmin();

        // 2. Elegir un producto del catálogo y consultar su stock inicial en /admin/products
        String codigoProducto = "PROD-002";
        int stockInicial = obtenerStockDeProducto(codigoProducto);
        pausaVisual();

        // 3. Navegar al formulario de registro de compra a proveedor
        driver.get(getBaseUrl() + "/admin/registrar-compra");
        pausaVisual();

        // 4. Seleccionar un proveedor mayorista
        WebElement provElem = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("proveedorId")));
        Select selectProveedor = new Select(provElem);

        boolean proveedorSeleccionado = false;
        for (WebElement opt : selectProveedor.getOptions()) {
            if (opt.isEnabled() && opt.getAttribute("value") != null && !opt.getAttribute("value").isBlank()) {
                selectProveedor.selectByValue(opt.getAttribute("value"));
                proveedorSeleccionado = true;
                break;
            }
        }
        assertTrue(proveedorSeleccionado, "Debe existir al menos un proveedor disponible para seleccionar");
        pausaVisual(500);

        // 5. Configurar estado como SIN_DEFINIR (pendiente de entrega)
        Select selectEstado = new Select(driver.findElement(By.id("estado")));
        selectEstado.selectByValue("SIN_DEFINIR");
        pausaVisual(500);

        // 6. Seleccionar producto a pedir
        Select selectProducto = new Select(driver.findElement(By.id("selectorProducto")));
        boolean productoEncontrado = false;
        for (WebElement opt : selectProducto.getOptions()) {
            if (opt.getText().contains(codigoProducto)) {
                selectProducto.selectByVisibleText(opt.getText());
                productoEncontrado = true;
                break;
            }
        }
        assertTrue(productoEncontrado, "El producto " + codigoProducto + " debe estar disponible en el catálogo de compra");
        pausaVisual(5000);

        // 7. Indicar cantidad a pedir
        int cantidadPedida = 5;
        WebElement cantInput = driver.findElement(By.id("selectorCantidad"));
        cantInput.clear();
        cantInput.sendKeys(String.valueOf(cantidadPedida));
        pausaVisual(5000);

        // 8. Hacer clic en "Pedir" para agregar al detalle de la orden
        WebElement btnPedir = driver.findElement(By.id("btnAgregarProducto"));
        scrollToElement(btnPedir);
        btnPedir.click();
        pausaVisual();

        // Verificar que el ítem figure en la tabla de detalle
        WebElement filaDetalle = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.xpath("//table[@id='tablaDetalles']//tbody//tr[not(@id='filaVacia')]")
        ));
        assertNotNull(filaDetalle, "La tabla de detalle debe contener el producto pedido");

        // 9. Confirmar y registrar la orden de compra
        WebElement btnConfirmar = driver.findElement(By.cssSelector("#formCompra button[type='submit']"));
        scrollToElement(btnConfirmar);
        btnConfirmar.click();

        // 10. Validar alerta de éxito tras registrar la orden
        wait.until(ExpectedConditions.urlContains("/admin/registrar-compra"));
        WebElement alertaExitoCompra = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector(".alert-success")
        ));
        assertTrue(alertaExitoCompra.isDisplayed(), "Debe mostrarse confirmación de orden registrada");
        pausaVisual();

        // 11. Verificar que el stock NO ha aumentado aún (factura en estado pendiente / SIN_DEFINIR)
        int stockSinEntregar = obtenerStockDeProducto(codigoProducto);
        assertEquals(stockInicial, stockSinEntregar,
                "El stock no debe incrementarse mientras la factura esté pendiente de entrega");
        pausaVisual();

        // 12. Navegar a /admin/orders para ubicar la orden generada y marcarla como entregada
        driver.get(getBaseUrl() + "/admin/orders");
        pausaVisual();

        // Buscar el botón para marcar como entregada (acción /admin/compras/{id}/entregar)
        WebElement btnEntregar = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//form[contains(@action, '/entregar')]//button")
        ));
        scrollToElement(btnEntregar);
        btnEntregar.click();

        // Aceptar confirmación en caso de alerta JS modal
        try {
            Alert alert = wait.until(ExpectedConditions.alertIsPresent());
            alert.accept();
        } catch (Exception ignored) {
        }

        // 13. Validar que la entrega fue procesada con mensaje de confirmación
        WebElement alertaEntrega = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector(".alert-success")
        ));
        assertTrue(alertaEntrega.isDisplayed(), "Debe mostrarse mensaje de recepción de mercadería");
        pausaVisual();

        // 14. Validar en /admin/products que el stock se incrementó en la cantidad pedida
        int stockFinal = obtenerStockDeProducto(codigoProducto);
        pausaVisual(15000);
        assertEquals(stockInicial + cantidadPedida, stockFinal,
                "El stock final debe ser exactamente el inicial más la cantidad entregada por el proveedor");
    }
}
