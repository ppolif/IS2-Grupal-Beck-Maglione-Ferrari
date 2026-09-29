package com.example.zero.services;

import com.example.zero.entidades.empresa.Empresa;
import com.example.zero.enums.TipoEmpresa;
import com.example.zero.repositories.EmpresaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EmpresaService {

    private final EmpresaRepository empresaRepository;

    public EmpresaService(EmpresaRepository empresaRepository) {
        this.empresaRepository = empresaRepository;
    }

    @Transactional
    public Empresa obtenerSucursalActiva() {
        return empresaRepository.findFirstByEliminadoFalse()
                .orElseGet(() -> {
                    Empresa e = new Empresa();
                    e.setId("SUC-001");
                    e.setRazonSocial("ZERO Argentina S.A. - Sucursal Central");
                    e.setCuit("30-71829384-9");
                    e.setTipoSucursal(TipoEmpresa.SEDE_CENTRAL);
                    e.setEliminado(false);
                    return empresaRepository.save(e);
                });
    }

    public List<Empresa> listarSucursales() {
        return empresaRepository.findByEliminadoFalse();
    }
}
