package com.example.carrito.servicios.compra.template;

import com.example.carrito.dtos.ComprobantePago;
import com.example.carrito.dtos.DatosPagoDTO;
import com.example.carrito.entidades.Carrito;
import com.example.carrito.entidades.Detalle;
import com.example.carrito.entidades.FacturaCompra;
import com.example.carrito.entidades.Usuario;
import com.example.carrito.excepciones.ErrorServicio;
import com.example.carrito.repositorios.CarritoRepositorio;
import com.example.carrito.repositorios.DetalleRepositorio;
import com.example.carrito.repositorios.FacturaCompraRepositorio;
import com.example.carrito.repositorios.UsuarioRepositorio;
import com.example.carrito.servicios.CarritoServicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * ============================================================================
 * PATRÓN DE DISEÑO GOF: TEMPLATE METHOD (MÉTODO PLANTILLA)
 * ============================================================================
 * Define el esqueleto del algoritmo de procesamiento de compras (HU5).
 * Los pasos invariables (validación, registro de compra y vaciado seguro del carrito)
 * están definidos aquí, mientras que las variaciones de cálculo (recargos/descuentos)
 * y ejecución del cobro son delegadas a las subclases.
 */
public abstract class ProcesadorCompraTemplate {

    @Autowired
    protected CarritoRepositorio carritoRepositorio;

    @Autowired
    protected UsuarioRepositorio usuarioRepositorio;

    @Autowired
    protected FacturaCompraRepositorio facturaCompraRepositorio;

    @Autowired
    protected DetalleRepositorio detalleRepositorio;

    @Autowired
    protected CarritoServicio carritoServicio;

    /**
     * MÉTODO PLANTILLA (TEMPLATE METHOD)
     * Es 'final' para impedir que las subclases alteren el flujo del algoritmo.
     * Es '@Transactional' para asegurar atomicidad (Criterio HU5: Si falla, el carrito NO se vacía).
     */
    @Transactional(rollbackFor = Exception.class)
    public final FacturaCompra procesarCompra(String carritoId, String usuarioId, DatosPagoDTO datosPago)
            throws ErrorServicio {

        // 1. Paso Concreto: Validar existencia de Carrito y Usuario
        Carrito carrito = validarYObtenerCarrito(carritoId);
        Usuario usuario = validarYObtenerUsuario(usuarioId);

        // 2. Paso Concreto: Validar que el carrito tenga al menos un producto activo y total válido (HU5)
        validarCarritoConProductosActivos(carrito);

        // 3. Paso Concreto: Verificar disponibilidad de artículos
        verificarDisponibilidad(carrito);

        // 4. Paso Abstracto / Hook: Calcular total final aplicando recargos o descuentos según método
        double totalFinal = calcularTotalFinal(carrito, datosPago);

        // 5. Paso Abstracto: Ejecutar la transacción de pago correspondiente
        ComprobantePago comprobante = ejecutarTransaccionPago(totalFinal, datosPago);

        // 6. Paso Concreto: Generar registro histórico de factura de compra asociado al Usuario (HU2 y HU5)
        FacturaCompra orden = generarFacturaCompra(carrito, usuario, totalFinal, comprobante);

        // 7. Paso Concreto: Vaciar/limpiar lógicamente el carrito actual y reiniciar total a 0 (HU5)
        limpiarCarrito(carrito);

        // 8. Paso Hook opcional: Notificación al cliente o logs de auditoría
        notificarConfirmacion(usuario, orden, comprobante);

        return orden;
    }

    // =========================================================================
    // PASOS CONCRETOS (Comunes a todas las modalidades de compra)
    // =========================================================================

    protected void limpiarCarrito(Carrito carrito) {
        if (carrito.getDetalles() != null) {
            for (Detalle d : carrito.getDetalles()) {
                if (!d.isEliminado()) {
                    d.setEliminado(true);
                    d.setSubtotal(0.0);
                    detalleRepositorio.save(d);
                }
            }
        }
        carrito.setTotal(0.0);
        carritoRepositorio.save(carrito);
    }

    protected Carrito validarYObtenerCarrito(String carritoId) throws ErrorServicio {
        if (carritoId == null || carritoId.trim().isEmpty()) {
            throw new ErrorServicio("El identificador del carrito no puede ser nulo");
        }
        return carritoRepositorio.findById(carritoId)
                .filter(c -> !c.isEliminado())
                .orElseThrow(() -> new ErrorServicio("Carrito no encontrado o inactivo"));
    }

    protected Usuario validarYObtenerUsuario(String usuarioId) throws ErrorServicio {
        if (usuarioId == null || usuarioId.trim().isEmpty()) {
            throw new ErrorServicio("El identificador del usuario no puede ser nulo");
        }
        return usuarioRepositorio.findById(usuarioId)
                .filter(u -> !u.isEliminado())
                .orElseThrow(() -> new ErrorServicio("Usuario no encontrado o dado de baja"));
    }

    protected void validarCarritoConProductosActivos(Carrito carrito) throws ErrorServicio {
        carritoServicio.recalcularTotal(carrito);
        List<Detalle> detallesActivos = carrito.getDetalles().stream()
                .filter(d -> !d.isEliminado() && d.getCantidad() > 0)
                .collect(Collectors.toList());

        if (detallesActivos.isEmpty()) {
            throw new ErrorServicio("El carrito debe contener al menos un producto activo para procesar la compra");
        }

        if (carrito.getTotal() <= 0) {
            throw new ErrorServicio("El total del carrito debe ser mayor a cero");
        }
    }

    protected void verificarDisponibilidad(Carrito carrito) throws ErrorServicio {
        for (Detalle d : carrito.getDetalles()) {
            if (!d.isEliminado() && (d.getArticulo() == null || d.getArticulo().isEliminado())) {
                throw new ErrorServicio("El artículo '" +
                        (d.getArticulo() != null ? d.getArticulo().getNombre() : "desconocido") +
                        "' ya no está disponible en la tienda");
            }
        }
    }

    protected FacturaCompra generarFacturaCompra(Carrito carrito, Usuario usuario, double totalFinal,
                                                 ComprobantePago comprobante) {
        String resumen = carrito.getDetalles().stream()
                .filter(d -> !d.isEliminado())
                .map(d -> d.getArticulo().getNombre() + " (x" + d.getCantidad() + ")")
                .collect(Collectors.joining(", "));

        FacturaCompra orden = FacturaCompra.builder()
                .usuario(usuario)
                .fecha(LocalDateTime.now())
                .total(totalFinal)
                .metodoPago(comprobante.getMetodoPago())
                .codigoComprobante(comprobante.getCodigoTransaccion())
                .resumenArticulos(resumen)
                .eliminado(false)
                .build();

        return facturaCompraRepositorio.save(orden);
    }

    // =========================================================================
    // PASOS ABSTRACTOS (A ser implementados obligatoriamente por las subclases)
    // =========================================================================

    /**
     * Permite aplicar recargos (ej. cuotas con tarjeta) o descuentos (ej. transferencia).
     */
    protected abstract double calcularTotalFinal(Carrito carrito, DatosPagoDTO datosPago) throws ErrorServicio;

    /**
     * Ejecuta el cobro específico contra la pasarela o genera los datos de transferencia.
     */
    protected abstract ComprobantePago ejecutarTransaccionPago(double monto, DatosPagoDTO datosPago) throws ErrorServicio;

    // =========================================================================
    // PASO HOOK (Comportamiento por defecto con opción de sobrescritura)
    // =========================================================================

    protected void notificarConfirmacion(Usuario usuario, FacturaCompra orden, ComprobantePago comprobante) {
        System.out.println("[AUDITORÍA COMPRA] Compra procesada con éxito para usuario: "
                + usuario.getEmail() + " | Orden ID: " + orden.getId()
                + " | Total: $" + orden.getTotal()
                + " | Método: " + comprobante.getMetodoPago());
    }
}
