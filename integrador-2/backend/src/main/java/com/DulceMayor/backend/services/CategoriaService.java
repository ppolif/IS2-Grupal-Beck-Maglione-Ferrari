package com.DulceMayor.backend.services;

import com.DulceMayor.backend.config.ConflictoException;
import com.DulceMayor.backend.config.ValidacionException;
import com.DulceMayor.backend.dtos.BaseMapper;
import com.DulceMayor.backend.dtos.CategoriaMapper;
import com.DulceMayor.backend.dtos.CategoriaRequest;
import com.DulceMayor.backend.dtos.CategoriaResponse;
import com.DulceMayor.backend.entities.Categoria;
import com.DulceMayor.backend.repositories.BaseRepository;
import com.DulceMayor.backend.repositories.CategoriaRepository;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * Servicio de negocio para la gestión de categorías del catálogo.
 *
 * <p><b>Patrón GoF aplicado: Template Method (Comportamiento)</b></p>
 * <ul>
 *   <li><b>Participante GoF:</b> ConcreteClass (Clase Concreta).</li>
 *   <li><b>Clase Base / AbstractClass:</b> {@link BaseService}.</li>
 *   <li><b>Operaciones primitivas y Hooks implementados:</b>
 *     <ul>
 *       <li>{@link #prepararRequest(CategoriaRequest)}: normaliza espacios y mayúsculas/minúsculas.</li>
 *       <li>{@link #validarCrear(CategoriaRequest)}: valida unicidad de nombre de categoría.</li>
 *       <li>{@link #validarModificar(String, CategoriaRequest, Categoria)}: valida unicidad excluyendo el propio ID.</li>
 *       <li>{@link #actualizarEntidad(CategoriaRequest, Categoria)}: aplica los cambios en el modelo de persistencia.</li>
 *     </ul>
 *   </li>
 * </ul>
 *
 * <p>Nota de arquitectura (sección 10.1 regla 4 de GENERAL.md): al eliminar una categoría NO se
 * consulta {@code SubCategoriaRepository} desde aquí porque la dependencia va de {@code SubCategoria}
 * hacia {@code Categoria}.</p>
 */
@Service
public class CategoriaService extends BaseService<Categoria, CategoriaRequest, CategoriaResponse> {

    private static final int LONGITUD_MAXIMA_NOMBRE = 100;

    private final CategoriaRepository categoriaRepository;
    private final CategoriaMapper categoriaMapper;

    public CategoriaService(
            CategoriaRepository categoriaRepository,
            CategoriaMapper categoriaMapper) {
        this.categoriaRepository = categoriaRepository;
        this.categoriaMapper = categoriaMapper;
    }

    @Override
    protected BaseRepository<Categoria> getRepository() {
        return categoriaRepository;
    }

    @Override
    protected BaseMapper<Categoria, CategoriaRequest, CategoriaResponse> getMapper() {
        return categoriaMapper;
    }

    @Override
    protected String getNombreRecurso() {
        return "Categoría";
    }

    @Override
    protected CategoriaRequest prepararRequest(CategoriaRequest request) {
        if (request == null || request.nombre() == null) {
            return request;
        }
        return new CategoriaRequest(request.nombre().trim());
    }

    @Override
    protected void validarCrear(CategoriaRequest request) {
        validarFormatoNombre(request);
        if (categoriaRepository.existsByNombreIgnoreCaseAndEliminadoFalse(request.nombre())) {
            throw new ConflictoException(
                    "DUPLICADO",
                    "Ya existe una categoría con el nombre '" + request.nombre() + "'"
            );
        }
    }

    @Override
    protected void validarModificar(String id, CategoriaRequest request, Categoria existente) {
        validarFormatoNombre(request);
        if (categoriaRepository.existsByNombreIgnoreCaseAndIdNotAndEliminadoFalse(request.nombre(), id)) {
            throw new ConflictoException(
                    "DUPLICADO",
                    "Ya existe otra categoría con el nombre '" + request.nombre() + "'"
            );
        }
    }

    @Override
    protected List<Categoria> buscarEntidadesActivas() {
        return categoriaRepository.findAllByEliminadoFalseOrderByNombreAsc();
    }

    private void validarFormatoNombre(CategoriaRequest request) {
        if (request == null || request.nombre() == null || request.nombre().isBlank()) {
            throw new ValidacionException("El nombre de la categoría es obligatorio");
        }
        if (request.nombre().length() > LONGITUD_MAXIMA_NOMBRE) {
            throw new ValidacionException(
                    "El nombre de la categoría no puede superar los " + LONGITUD_MAXIMA_NOMBRE + " caracteres"
            );
        }
    }
}

