package com.example.zero.dto.producto;

import com.example.zero.entidades.producto.Producto;
import com.example.zero.entidades.producto.SubCategoria;
import com.example.zero.services.StockService;
import com.example.zero.services.VigenciaPrecioService;
import com.example.zero.services.producto.ProductoService;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductoDTO {

    private ProductoService productoService;

    private String id;
    private String codigo;
    private String nombre;
    private String descripcion;
    private String talle;
    private boolean enOferta;
    private SubCategoria subCategoria;
    private String imagenUrl;
    private double precioActual;
    private int stock;
    private String estadoStock;
    private int cantidadFaltanteStock;
    private String urlWhatsAppReposicion;

    // Métodos alias para compatibilidad con plantillas y nombres en inglés
    public String getName() {
        return nombre;
    }

    public String getDescription() {
        return descripcion;
    }

    public String getImageUrl() {
        return imagenUrl != null ? imagenUrl : "/shop/img/product/p1.jpg";
    }

    public double getPrice() {
        return precioActual;
    }

    public double getPrecio() {
        return precioActual;
    }

    public String getCategoryName() {
        if (subCategoria != null && subCategoria.getCategoria() != null) {
            return subCategoria.getCategoria().getNombre();
        }
        if (subCategoria != null) {
            return subCategoria.getNombre();
        }
        return "Indumentaria";
    }

    public static ProductoDTO de(Producto p, double precioActual, int stock, StockService stockService, ProductoService ps) {
        if (p == null) return null;

        String estadoStock = stockService != null ? stockService.getEstadoStock(stock) : (stock > 50 ? "BIEN" : (stock >= 20 ? "REGULAR" : "MALO"));
        int faltante = stockService != null ? stockService.getCantidadFaltanteStock(stock) : Math.max(0, 50 - stock);
        String urlWhatsApp = stockService != null
                ? stockService.getUrlWhatsAppReposicion(p.getNombre(), p.getCodigo(), stock)
                : "";

        return ProductoDTO.builder()
                .id(p.getId())
                .codigo(p.getCodigo())
                .nombre(p.getNombre())
                .descripcion(p.getDescripcion())
                .talle(p.getTalle())
                .enOferta(p.isEnOferta())
                .subCategoria(p.getSubCategoria())
                .imagenUrl(ps.obtenerImagenUrl(p))
                .precioActual(precioActual)
                .stock(stock)

                .estadoStock(estadoStock)
                .cantidadFaltanteStock(faltante)
                .urlWhatsAppReposicion(urlWhatsApp)
                .build();
    }

    public static ProductoDTO de(Producto p, VigenciaPrecioService vigenciaService, StockService stockService, ProductoService ps) {
        if (p == null) return null;

        double precio = 0.0;
        if (vigenciaService != null && p.getId() != null) {
            try {
                precio = vigenciaService.obtenerPrecioActual(p.getId());
            } catch (Exception ignored) {
            }
        }

        int stock = 0;
        if (stockService != null && p.getId() != null) {
            try {
                stock = stockService.calcularStockActual(p.getId());
            } catch (Exception ignored) {
            }
        }

        return de(p, precio, stock, stockService, ps);
    }
}
