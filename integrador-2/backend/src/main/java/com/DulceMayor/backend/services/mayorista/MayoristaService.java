package com.DulceMayor.backend.services.mayorista;

import com.DulceMayor.backend.config.ConflictoException;
import com.DulceMayor.backend.config.ValidacionException;
import com.DulceMayor.backend.dtos.ContactoRequest;
import com.DulceMayor.backend.dtos.direccion.DireccionRequest;
import com.DulceMayor.backend.dtos.mayorista.MayoristaMapper;
import com.DulceMayor.backend.dtos.mayorista.MayoristaRequest;
import com.DulceMayor.backend.dtos.mayorista.MayoristaResponse;
import com.DulceMayor.backend.entities.Imagen;
import com.DulceMayor.backend.entities.TipoImagen;
import com.DulceMayor.backend.entities.mayorista.Mayorista;
import com.DulceMayor.backend.entities.mayorista.TipoEmpresa;
import com.DulceMayor.backend.repositories.mayorista.MayoristaRepository;
import com.DulceMayor.backend.services.BaseService;
import com.DulceMayor.backend.services.ContactoService;
import com.DulceMayor.backend.services.ImagenService;
import com.DulceMayor.backend.services.direccion.DireccionService;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio de negocio para la administración de la empresa mayorista (sede central y sucursales).
 */
@Service
public class MayoristaService extends BaseService<Mayorista, MayoristaRequest, MayoristaResponse> {

    private final MayoristaRepository mayoristaRepository;
    private final MayoristaMapper mayoristaMapper;
    private final DireccionService direccionService;
    private final ContactoService contactoService;
    private final ImagenService imagenService;

    public MayoristaService(
            MayoristaRepository mayoristaRepository,
            MayoristaMapper mayoristaMapper,
            DireccionService direccionService,
            ContactoService contactoService,
            ImagenService imagenService) {
        super(mayoristaRepository, mayoristaMapper, "Mayorista");
        this.mayoristaRepository = mayoristaRepository;
        this.mayoristaMapper = mayoristaMapper;
        this.direccionService = direccionService;
        this.contactoService = contactoService;
        this.imagenService = imagenService;
    }

    @Override
    protected MayoristaRequest prepararRequest(MayoristaRequest request) {
        if (request == null) {
            return null;
        }
        return new MayoristaRequest(
                request.razonSocial() != null ? request.razonSocial().trim() : null,
                request.cuit() != null ? request.cuit().trim() : null,
                request.tipoEmpresa(),
                request.direcciones(),
                request.contactos(),
                request.imagenIds()
        );
    }

    @Override
    protected void validarCrear(MayoristaRequest request) {
        validarFormato(request);
        if (mayoristaRepository.existsByCuitAndEliminadoFalse(request.cuit())) {
            throw new ConflictoException("DUPLICADO", "Ya existe un mayorista con el CUIT " + request.cuit());
        }
        validarImagenes(request.imagenIds());
    }

    @Override
    protected void validarModificar(String id, MayoristaRequest request, Mayorista existente) {
        validarFormato(request);
        if (mayoristaRepository.existsByCuitAndIdNotAndEliminadoFalse(request.cuit(), id)) {
            throw new ConflictoException("DUPLICADO", "Ya existe otro mayorista con el CUIT " + request.cuit());
        }
        validarImagenes(request.imagenIds());
    }

    @Override
    protected void antesDePersistir(Mayorista entidad, MayoristaRequest request) {
        if (request.direcciones() != null) {
            entidad.getDirecciones().clear();
            for (DireccionRequest dReq : request.direcciones()) {
                entidad.getDirecciones().add(direccionService.construirEntidad(dReq));
            }
        }
        if (request.contactos() != null) {
            entidad.getContactos().clear();
            for (ContactoRequest cReq : request.contactos()) {
                entidad.getContactos().add(contactoService.construirEntidad(cReq));
            }
        }
        if (request.imagenIds() != null) {
            entidad.getImagenes().clear();
            for (String imgId : request.imagenIds()) {
                entidad.getImagenes().add(imagenService.obtenerEntidad(imgId));
            }
        }
    }

    @Override
    protected List<Mayorista> buscarEntidadesActivas() {
        return mayoristaRepository.findAllByEliminadoFalseOrderByTipoEmpresaAscRazonSocialAsc();
    }

    @Transactional(readOnly = true)
    public List<MayoristaResponse> listarPublicoSucursales() {
        List<Mayorista> sucursales = mayoristaRepository.findAllByTipoEmpresaAndEliminadoFalse(TipoEmpresa.SUCURSAL);
        return mapper.aResponseList(sucursales);
    }

    private void validarFormato(MayoristaRequest request) {
        if (request == null) {
            throw new ValidacionException("Los datos del mayorista son obligatorios");
        }
        if (request.razonSocial() == null || request.razonSocial().isBlank()) {
            throw new ValidacionException("La razón social es obligatoria");
        }
        if (request.cuit() == null || request.cuit().isBlank()) {
            throw new ValidacionException("El CUIT es obligatorio");
        }
        if (request.tipoEmpresa() == null) {
            throw new ValidacionException("El tipo de empresa es obligatorio");
        }
    }

    private void validarImagenes(List<String> imagenIds) {
        if (imagenIds == null || imagenIds.isEmpty()) {
            return;
        }
        for (String id : imagenIds) {
            Imagen img = imagenService.obtenerEntidad(id);
            if (img.getTipoImagen() != TipoImagen.SUCURSAL && img.getTipoImagen() != TipoImagen.LOGO_EMPRESA) {
                throw new ValidacionException(
                        "Las imágenes de un mayorista deben ser de tipo SUCURSAL o LOGO_EMPRESA");
            }
        }
    }
}

