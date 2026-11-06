package com.DulceMayor.backend.services.producto;

import com.DulceMayor.backend.config.ConflictoException;
import com.DulceMayor.backend.config.RecursoNoEncontradoException;
import com.DulceMayor.backend.config.ValidacionException;
import com.DulceMayor.backend.dtos.BaseMapper;
import com.DulceMayor.backend.dtos.producto.ProductoCreadoEvent;
import com.DulceMayor.backend.dtos.producto.VigenciaPrecioMapper;
import com.DulceMayor.backend.dtos.producto.VigenciaPrecioRequest;
import com.DulceMayor.backend.dtos.producto.VigenciaPrecioResponse;
import com.DulceMayor.backend.entities.producto.Producto;
import com.DulceMayor.backend.entities.producto.VigenciaPrecio;
import com.DulceMayor.backend.repositories.BaseRepository;
import com.DulceMayor.backend.repositories.producto.VigenciaPrecioRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import com.DulceMayor.backend.services.BaseService;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio de negocio para la administración de las vigencias de precios de productos.
 * Implementa las reglas de vigencia actual (fechaHasta == null), cierre de vigencias previas
 * y validación contra solapamientos de fechas según 02-reglas-negocio.md.
 *
 * <p><b>Patrón GoF aplicado: Template Method (Comportamiento)</b></p>
 * <ul>
 *   <li><b>Participante GoF:</b> ConcreteClass (Clase Concreta).</li>
 *   <li><b>Clase Base / AbstractClass:</b> {@link BaseService}.</li>
 *   <li><b>Operaciones primitivas y Hooks implementados:</b>
 *     <ul>
 *       <li>{@link #validarCrear(VigenciaPrecioRequest)}: valida producto existente, precio > 0, fechas y ausencia de solapamientos.</li>
 *       <li>{@link #antesDePersistir(VigenciaPrecio, VigenciaPrecioRequest)}: asocia la relación con {@link Producto} y cierra la vigencia previa.</li>
 *       <li>{@link #validarModificar(String, VigenciaPrecioRequest, VigenciaPrecio)}: valida no solapamiento excluyendo el propio ID.</li>
 *       <li>{@link #actualizarEntidad(VigenciaPrecioRequest, VigenciaPrecio)}: actualiza precios y rangos de fechas.</li>
 *     </ul>
 *   </li>
 * </ul>
 *
 * <p><b>Patrón GoF aplicado: Observer (Comportamiento)</b></p>
 * <ul>
 *   <li><b>Participante GoF:</b> ConcreteObserver (Observador Concreto).</li>
 *   <li><b>Sujeto / Publisher:</b> {@link ProductoService} (emite {@link ProductoCreadoEvent}).</li>
 *   <li><b>Implementación:</b> Vía {@link EventListener} en {@link #alCrearProducto(ProductoCreadoEvent)}.
 *       Reacciona de manera autónoma al alta de un producto creando su primera vigencia de precio activa,
 *       sin que el sujeto emisor conozca este servicio.</li>
 * </ul>
 */
@Service
public class VigenciaPrecioService extends BaseService<VigenciaPrecio, VigenciaPrecioRequest, VigenciaPrecioResponse> {

    private final VigenciaPrecioRepository vigenciaPrecioRepository;
    private final ProductoService productoService;

    public VigenciaPrecioService(
            VigenciaPrecioRepository vigenciaPrecioRepository,
            VigenciaPrecioMapper vigenciaPrecioMapper,
            ProductoService productoService) {
        super(vigenciaPrecioRepository, vigenciaPrecioMapper, "Vigencia de precio");
        this.vigenciaPrecioRepository = vigenciaPrecioRepository;
        this.productoService = productoService;
    }

    /// patron observer, cuando un producto nuevo se crea, se le notifica a vigencia para que cree el registro correspondiente
    @EventListener
    @Transactional
    public void alCrearProducto(ProductoCreadoEvent event) {
        if (event != null && event.precioInicial() != null) {
            Producto producto = productoService.obtenerEntidad(event.productoId());
            VigenciaPrecio inicial = VigenciaPrecio.builder()
                    .producto(producto)
                    .fechaDesde(LocalDate.now())
                    .fechaHasta(null)
                    .precio(event.precioInicial())
                    .eliminado(false)
                    .build();
            vigenciaPrecioRepository.save(inicial);
        }
    }

    @Transactional
    public VigenciaPrecioResponse registrarPrecio(String productoId, VigenciaPrecioRequest request) {
        validarRequest(request);
        Producto producto = productoService.obtenerEntidad(productoId);
        LocalDate hoy = LocalDate.now();

        // Si existe una vigencia actual abierta (fechaHasta == null), se cierra al día de ayer o hoy
        Optional<VigenciaPrecio> actualOpt = vigenciaPrecioRepository.findByProductoIdAndFechaHastaIsNullAndEliminadoFalse(productoId);
        if (actualOpt.isPresent()) {
            VigenciaPrecio actual = actualOpt.get();
            if (hoy.isAfter(actual.getFechaDesde())) {
                actual.setFechaHasta(hoy.minusDays(1));
            } else {
                actual.setFechaHasta(actual.getFechaDesde());
            }
            vigenciaPrecioRepository.save(actual);
        }

        VigenciaPrecio nueva = VigenciaPrecio.builder()
                .producto(producto)
                .fechaDesde(hoy)
                .fechaHasta(null)
                .precio(request.precio())
                .eliminado(false)
                .build();

        VigenciaPrecio guardada = vigenciaPrecioRepository.save(nueva);
        return mapper.aResponse(guardada);
    }

    @Transactional(readOnly = true)
    public List<VigenciaPrecioResponse> listarPorProducto(String productoId) {
        productoService.obtenerEntidad(productoId);
        List<VigenciaPrecio> lista = vigenciaPrecioRepository.findAllByProductoIdAndEliminadoFalseOrderByFechaDesdeDesc(productoId);
        return mapper.aResponseList(lista);
    }

    @Transactional(readOnly = true)
    public Double obtenerPrecioVigente(String productoId) {
        return vigenciaPrecioRepository.findByProductoIdAndFechaHastaIsNullAndEliminadoFalse(productoId)
                .map(VigenciaPrecio::getPrecio)
                .orElse(null);
    }

    @Transactional(readOnly = true)
    public VigenciaPrecioResponse buscarVigente(String productoId) {
        productoService.obtenerEntidad(productoId);
        VigenciaPrecio vigente = vigenciaPrecioRepository.findByProductoIdAndFechaHastaIsNullAndEliminadoFalse(productoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró precio vigente para el producto"));
        return mapper.aResponse(vigente);
    }

    @Override
    protected void validarCrear(VigenciaPrecioRequest request) {
        validarRequest(request);
    }

    @Override
    protected void validarModificar(String id, VigenciaPrecioRequest request, VigenciaPrecio existente) {
        validarRequest(request);
    }

    private void validarRequest(VigenciaPrecioRequest request) {
        if (request == null) {
            throw new ValidacionException("Los datos de la vigencia de precio son obligatorios");
        }
        if (request.precio() == null || request.precio() <= 0) {
            throw new ValidacionException("El precio debe ser un número positivo mayor a 0");
        }
    }
}

