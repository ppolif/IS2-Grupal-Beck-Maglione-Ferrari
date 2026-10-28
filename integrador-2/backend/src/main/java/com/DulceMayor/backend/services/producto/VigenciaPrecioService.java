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
    private final VigenciaPrecioMapper vigenciaPrecioMapper;
    private final ProductoService productoService;

    public VigenciaPrecioService(
            VigenciaPrecioRepository vigenciaPrecioRepository,
            VigenciaPrecioMapper vigenciaPrecioMapper,
            ProductoService productoService) {
        this.vigenciaPrecioRepository = vigenciaPrecioRepository;
        this.vigenciaPrecioMapper = vigenciaPrecioMapper;
        this.productoService = productoService;
    }

    @Override
    protected BaseRepository<VigenciaPrecio> getRepository() {
        return vigenciaPrecioRepository;
    }

    @Override
    protected BaseMapper<VigenciaPrecio, VigenciaPrecioRequest, VigenciaPrecioResponse> getMapper() {
        return vigenciaPrecioMapper;
    }

    @Override
    protected String getNombreRecurso() {
        return "Vigencia de precio";
    }

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

        // Si existe una vigencia actual abierta (fechaHasta == null), se cierra al día previo a la nueva
        Optional<VigenciaPrecio> actualOpt = vigenciaPrecioRepository.findByProductoIdAndFechaHastaIsNullAndEliminadoFalse(productoId);
        if (actualOpt.isPresent()) {
            VigenciaPrecio actual = actualOpt.get();
            if (request.fechaDesde().isBefore(actual.getFechaDesde())) {
                throw new ConflictoException(
                        "CONFLICTO",
                        "La fecha desde (" + request.fechaDesde() + ") no puede ser anterior al inicio de la vigencia actual (" + actual.getFechaDesde() + ")"
                );
            }
            actual.setFechaHasta(request.fechaDesde().minusDays(1));
            vigenciaPrecioRepository.save(actual);
        }

        // Verifica que no haya solapamiento con otras vigencias históricas
        List<VigenciaPrecio> solapadas = vigenciaPrecioRepository.buscarSolapamientos(
                productoId,
                request.fechaDesde(),
                request.fechaHasta(),
                null
        );
        if (!solapadas.isEmpty()) {
            throw new ConflictoException(
                    "CONFLICTO",
                    "No se permiten vigencias de precio solapadas para el mismo producto"
            );
        }

        VigenciaPrecio nueva = VigenciaPrecio.builder()
                .producto(producto)
                .fechaDesde(request.fechaDesde())
                .fechaHasta(request.fechaHasta())
                .precio(request.precio())
                .eliminado(false)
                .build();

        VigenciaPrecio guardada = vigenciaPrecioRepository.save(nueva);
        return vigenciaPrecioMapper.aResponse(guardada);
    }

    @Transactional(readOnly = true)
    public List<VigenciaPrecioResponse> listarPorProducto(String productoId) {
        productoService.obtenerEntidad(productoId);
        List<VigenciaPrecio> lista = vigenciaPrecioRepository.findAllByProductoIdAndEliminadoFalseOrderByFechaDesdeDesc(productoId);
        return vigenciaPrecioMapper.aResponseList(lista);
    }

    @Transactional(readOnly = true)
    public VigenciaPrecioResponse buscarVigente(String productoId) {
        productoService.obtenerEntidad(productoId);
        VigenciaPrecio vigente = vigenciaPrecioRepository.findByProductoIdAndFechaHastaIsNullAndEliminadoFalse(productoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró precio vigente para el producto"));
        return vigenciaPrecioMapper.aResponse(vigente);
    }

    @Override
    @Transactional
    public VigenciaPrecioResponse modificar(String id, VigenciaPrecioRequest request) {
        VigenciaPrecio existente = obtenerEntidad(id);
        validarRequest(request);

        List<VigenciaPrecio> solapadas = vigenciaPrecioRepository.buscarSolapamientos(
                existente.getProducto().getId(),
                request.fechaDesde(),
                request.fechaHasta(),
                id
        );
        if (!solapadas.isEmpty()) {
            throw new ConflictoException(
                    "CONFLICTO",
                    "La modificación genera solapamiento de fechas con otra vigencia de precio del producto"
            );
        }

        existente.setFechaDesde(request.fechaDesde());
        existente.setFechaHasta(request.fechaHasta());
        existente.setPrecio(request.precio());

        VigenciaPrecio guardada = vigenciaPrecioRepository.save(existente);
        return vigenciaPrecioMapper.aResponse(guardada);
    }

    private void validarRequest(VigenciaPrecioRequest request) {
        if (request == null) {
            throw new ValidacionException("Los datos de la vigencia de precio son obligatorios");
        }
        if (request.fechaDesde() == null) {
            throw new ValidacionException("La fecha desde es obligatoria");
        }
        if (request.precio() == null || request.precio() <= 0) {
            throw new ValidacionException("El precio debe ser un número positivo mayor a 0");
        }
        if (request.fechaHasta() != null && request.fechaHasta().isBefore(request.fechaDesde())) {
            throw new ValidacionException("La fecha hasta no puede ser anterior a la fecha desde");
        }
    }
}

