package com.example.zero;

import com.example.zero.entidades.compraProveedor.Proveedor;
import com.example.zero.entidades.producto.Producto;
import com.example.zero.repositories.ProductoRepository;
import com.example.zero.repositories.ProveedorRepository;
import com.example.zero.services.CompraProveedorService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ZeroApplicationTests {

    @Autowired
    private CompraProveedorService compraProveedorService;

    @Autowired
    private ProveedorRepository proveedorRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Test
    void testObtenerUltimoCostoUnitarioIntegration() {
        List<Proveedor> proveedores = proveedorRepository.findByEliminadoFalse();
        List<Producto> productos = productoRepository.findByEliminadoFalse();

        assertFalse(proveedores.isEmpty(), "Debe haber proveedores");
        assertFalse(productos.isEmpty(), "Debe haber productos");

        Proveedor textil = proveedores.stream()
                .filter(p -> p.getRazonSocial() != null && p.getRazonSocial().contains("Textil"))
                .findFirst()
                .orElse(proveedores.get(0));

        Producto prod2 = productos.stream()
                .filter(p -> p.getCodigo() != null && p.getCodigo().equals("PROD-002"))
                .findFirst()
                .orElse(productos.size() > 1 ? productos.get(1) : productos.get(0));

        Double costo = compraProveedorService.obtenerUltimoCostoUnitario(textil.getId(), prod2.getId());
        System.out.println(">> [TEST] Costo para " + textil.getRazonSocial() + " y " + prod2.getNombre() + ": " + costo);
        assertNotNull(costo);
        assertTrue(costo > 0, "El costo debe ser mayor a 0, pero fue: " + costo);
    }
}

