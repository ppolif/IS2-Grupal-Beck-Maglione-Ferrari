package com.DulceMayor.backend.services.producto;

import com.DulceMayor.backend.config.ConflictoException;
import com.DulceMayor.backend.config.RecursoNoEncontradoException;
import com.DulceMayor.backend.config.ValidacionException;
import com.DulceMayor.backend.dtos.BaseMapper;
import com.DulceMayor.backend.dtos.producto.PromocionMapper;
import com.DulceMayor.backend.dtos.producto.PromocionRequest;
import com.DulceMayor.backend.dtos.producto.PromocionResponse;
import com.DulceMayor.backend.entities.producto.Producto;
import com.DulceMayor.backend.entities.producto.Promocion;
import com.DulceMayor.backend.repositories.BaseRepository;
import com.DulceMayor.backend.repositories.producto.PromocionRepository;
import java.time.LocalDate;
import java.util.List;

import com.DulceMayor.backend.services.BaseService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio de negocio para la administración de promociones y códigos de descuento.
 *
 * <p><b>Patrón GoF aplicado: Template Method (Comportamiento)</b></p>
 * <ul>
 *   <li><b>Participante GoF:</b> ConcreteClass (Clase Concreta).</li>
 *   <li><b>Clase Base / AbstractClass:</b> {@link BaseService}.</li>
 *   <li><b>Operaciones primitivas y Hooks implementados:</b>
 *     <ul>
 *       <li>{@link #prepararRequest(PromocionRequest)}: normaliza el código a mayúsculas y limpia espacios.</li>
 *       <li>{@link #validarCrear(PromocionRequest)}: valida unicidad de código, rangos porcentuales (0..100) y coherencia de fechas.</li>
 *       <li>{@link #antesDePersistir(Promocion, PromocionRequest)}: asocia el producto destacado opcional.</li>
 *       <li>{@link #validarModificar(String, PromocionRequest, Promocion)}: valida unicidad de código excluyendo el propio ID.</li>
 *       <li>{@link #actualizarEntidad(PromocionRequest, Promocion)}: actualiza descripción, descuento, fechas y producto asociado.</li>
 *     </ul>
 *   </li>
 * </ul>
 */
@Service
public class PromocionService extends BaseService<Promocion, PromocionRequest, PromocionResponse> {

    private final PromocionRepository promocionRepository;
    private final ProductoService productoService;

    public PromocionService(
            PromocionRepository promocionRepository,
            PromocionMapper promocionMapper,
            ProductoService productoService) {
        super(promocionRepository, promocionMapper, "Promoción");
        this.promocionRepository = promocionRepository;
        this.productoService = productoService;
    }

    @Override
    protected PromocionRequest prepararRequest(PromocionRequest request) {
        if (request == null) {
            return null;
        }
        return new PromocionRequest(
                request.codigo() != null ? request.codigo().trim().toUpperCase() : null,
                request.descripcion() != null ? request.descripcion().trim() : null,
                request.porcentajeDescuento(),
                request.cantidadMinima(),
                request.productoId() != null && !request.productoId().isBlank() ? request.productoId().trim() : null,
                request.fechaDesde(),
                request.fechaHasta()
        );
    }

    @Override
    protected void validarCrear(PromocionRequest request) {
        validarRequest(request);
        if (promocionRepository.existsByCodigoIgnoreCaseAndEliminadoFalse(request.codigo())) {
            throw new ConflictoException(
                    "DUPLICADO",
                    "Ya existe una promoción con el código '" + request.codigo() + "'"
            );
        }
        if (request.productoId() != null) {
            productoService.obtenerEntidad(request.productoId());
        }
    }

    @Override
    protected void validarModificar(String id, PromocionRequest request, Promocion existente) {
        validarRequest(request);
        if (promocionRepository.existsByCodigoIgnoreCaseAndIdNotAndEliminadoFalse(request.codigo(), id)) {
            throw new ConflictoException(
                    "DUPLICADO",
                    "Ya existe otra promoción con el código '" + request.codigo() + "'"
            );
        }
        if (request.productoId() != null) {
            productoService.obtenerEntidad(request.productoId());
        }
    }

    @Override
    protected void antesDePersistir(Promocion entidad, PromocionRequest request) {
        if (request.productoId() != null) {
            Producto producto = productoService.obtenerEntidad(request.productoId());
            entidad.setProducto(producto);
        } else {
            entidad.setProducto(null);
        }
    }

    @Override
    protected List<Promocion> buscarEntidadesActivas() {
        return promocionRepository.findAllByEliminadoFalseOrderByCodigoAsc();
    }

    ///metodo para verificar si un codigo ingresado es valido
    @Transactional(readOnly = true)
    public PromocionResponse buscarVigentePorCodigo(String codigo) {
        if (codigo == null || codigo.isBlank()) {
            throw new ValidacionException("El código de promoción es obligatorio");
        }
        String codigoNormalizado = codigo.trim().toUpperCase();
        Promocion promocion = promocionRepository.findByCodigoIgnoreCaseAndEliminadoFalse(codigoNormalizado)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Promoción no encontrada o inexistente con código: " + codigoNormalizado
                ));

        LocalDate hoy = LocalDate.now();
        if (hoy.isBefore(promocion.getFechaDesde()) || hoy.isAfter(promocion.getFechaHasta())) {
            throw new RecursoNoEncontradoException(
                    "La promoción con código '" + codigoNormalizado + "' no se encuentra vigente a la fecha actual"
            );
        }

        return mapper.aResponse(promocion);
    }

    @Transactional(readOnly = true)
    public List<PromocionResponse> listar(Boolean soloVigentes) {
        //si hay filtro se aplica
        if (Boolean.TRUE.equals(soloVigentes)) {
            List<Promocion> vigentes = promocionRepository.findAllVigentes(LocalDate.now());
            return mapper.aResponseList(vigentes);
        }
        //sino se lista todo
        return listar();
    }

    private void validarRequest(PromocionRequest request) {
        if (request == null) {
            throw new ValidacionException("Los datos de la promoción son obligatorios");
        }
        if (request.codigo() == null || request.codigo().isBlank()) {
            throw new ValidacionException("El código de la promoción es obligatorio");
        }
        if (request.porcentajeDescuento() == null || request.porcentajeDescuento() < 0 || request.porcentajeDescuento() > 100) {
            throw new ValidacionException("El porcentaje de descuento debe estar comprendido entre 0 y 100");
        }
        if (request.cantidadMinima() != null && request.cantidadMinima() < 0) {
            throw new ValidacionException("La cantidad mínima no puede ser negativa");
        }
        if (request.fechaDesde() == null) {
            throw new ValidacionException("La fecha desde es obligatoria");
        }
        if (request.fechaHasta() == null) {
            throw new ValidacionException("La fecha hasta es obligatoria");
        }
        if (request.fechaHasta().isBefore(request.fechaDesde())) {
            throw new ValidacionException("La fecha hasta no puede ser anterior a la fecha desde");
        }
    }
}

