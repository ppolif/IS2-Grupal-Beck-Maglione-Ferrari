package com.DulceMayor.backend.services.producto;

import com.DulceMayor.backend.config.ConflictoException;
import com.DulceMayor.backend.config.ValidacionException;
import com.DulceMayor.backend.dtos.BaseMapper;
import com.DulceMayor.backend.dtos.producto.SubCategoriaMapper;
import com.DulceMayor.backend.dtos.producto.SubCategoriaRequest;
import com.DulceMayor.backend.dtos.producto.SubCategoriaResponse;
import com.DulceMayor.backend.entities.producto.Categoria;
import com.DulceMayor.backend.entities.producto.SubCategoria;
import com.DulceMayor.backend.repositories.BaseRepository;
import com.DulceMayor.backend.repositories.producto.SubCategoriaRepository;
import java.util.List;

import com.DulceMayor.backend.services.BaseService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio de negocio para la gestión de subcategorías del catálogo.
 *
 * <p><b>Patrón GoF aplicado: Template Method (Comportamiento)</b></p>
 * <ul>
 *   <li><b>Participante GoF:</b> ConcreteClass (Clase Concreta).</li>
 *   <li><b>Clase Base / AbstractClass:</b> {@link BaseService}.</li>
 *   <li><b>Operaciones primitivas y Hooks implementados:</b>
 *     <ul>
 *       <li>{@link #prepararRequest(SubCategoriaRequest)}: normaliza espacios y formato del nombre.</li>
 *       <li>{@link #validarCrear(SubCategoriaRequest)}: valida categoría padre existente y unicidad del nombre en la categoría.</li>
 *       <li>{@link #antesDePersistir(SubCategoria, SubCategoriaRequest)}: asocia la entidad foránea {@link Categoria}.</li>
 *       <li>{@link #validarModificar(String, SubCategoriaRequest, SubCategoria)}: valida unicidad excluyendo el propio ID.</li>
 *       <li>{@link #actualizarEntidad(SubCategoriaRequest, SubCategoria)}: actualiza el nombre y reasigna categoría si cambió.</li>
 *     </ul>
 *   </li>
 * </ul>
 */
@Service
public class SubCategoriaService extends BaseService<SubCategoria, SubCategoriaRequest, SubCategoriaResponse> {

    private static final int LONGITUD_MAXIMA_NOMBRE = 100;

    private final SubCategoriaRepository subCategoriaRepository;
    private final SubCategoriaMapper subCategoriaMapper;
    private final CategoriaService categoriaService;

    public SubCategoriaService(
            SubCategoriaRepository subCategoriaRepository,
            SubCategoriaMapper subCategoriaMapper,
            CategoriaService categoriaService) {
        this.subCategoriaRepository = subCategoriaRepository;
        this.subCategoriaMapper = subCategoriaMapper;
        this.categoriaService = categoriaService;
    }

    @Override
    protected BaseRepository<SubCategoria> getRepository() {
        return subCategoriaRepository;
    }

    @Override
    protected BaseMapper<SubCategoria, SubCategoriaRequest, SubCategoriaResponse> getMapper() {
        return subCategoriaMapper;
    }

    @Override
    protected String getNombreRecurso() {
        return "Subcategoría";
    }

    @Override
    protected SubCategoriaRequest prepararRequest(SubCategoriaRequest request) {
        if (request == null) {
            return null;
        }
        String nombreLimpio = request.nombre() != null ? request.nombre().trim() : null;
        return new SubCategoriaRequest(nombreLimpio, request.categoriaId());
    }

    @Override
    protected void validarCrear(SubCategoriaRequest request) {
        validarFormato(request);
        categoriaService.obtenerEntidad(request.categoriaId());
        if (subCategoriaRepository.existsByNombreIgnoreCaseAndCategoriaIdAndEliminadoFalse(request.nombre(), request.categoriaId())) {
            throw new ConflictoException(
                    "DUPLICADO",
                    "Ya existe una subcategoría con el nombre '" + request.nombre() + "' en la categoría indicada"
            );
        }
    }

    @Override
    protected void validarModificar(String id, SubCategoriaRequest request, SubCategoria existente) {
        validarFormato(request);
        categoriaService.obtenerEntidad(request.categoriaId());
        if (subCategoriaRepository.existsByNombreIgnoreCaseAndCategoriaIdAndIdNotAndEliminadoFalse(
                request.nombre(), request.categoriaId(), id)) {
            throw new ConflictoException(
                    "DUPLICADO",
                    "Ya existe otra subcategoría con el nombre '" + request.nombre() + "' en la categoría indicada"
            );
        }
    }

    @Override
    protected void antesDePersistir(SubCategoria entidad, SubCategoriaRequest request) {
        Categoria categoria = categoriaService.obtenerEntidad(request.categoriaId());
        entidad.setCategoria(categoria);
    }

    @Override
    protected List<SubCategoria> buscarEntidadesActivas() {
        return subCategoriaRepository.findAllByEliminadoFalseOrderByNombreAsc();
    }

    //se lista sin o con filtro de categoria
    @Transactional(readOnly = true)
    public List<SubCategoriaResponse> listar(String categoriaId) {
        if (categoriaId == null || categoriaId.isBlank()) {
            return listar();
        }
        List<SubCategoria> entidades = subCategoriaRepository.findAllByCategoriaIdAndEliminadoFalseOrderByNombreAsc(categoriaId);
        return subCategoriaMapper.aResponseList(entidades);
    }

    private void validarFormato(SubCategoriaRequest request) {
        if (request == null) {
            throw new ValidacionException("Los datos de la subcategoría son obligatorios");
        }
        if (request.nombre() == null || request.nombre().isBlank()) {
            throw new ValidacionException("El nombre de la subcategoría es obligatorio");
        }
        if (request.nombre().length() > LONGITUD_MAXIMA_NOMBRE) {
            throw new ValidacionException(
                    "El nombre de la subcategoría no puede superar los " + LONGITUD_MAXIMA_NOMBRE + " caracteres"
            );
        }
        if (request.categoriaId() == null || request.categoriaId().isBlank()) {
            throw new ValidacionException("El id de la categoría es obligatorio");
        }
    }
}

