package org.example.services;

import org.example.entities.FacturaCompra;
import org.example.repositories.FacturaCompraRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class FacturaCompraService {

    @Autowired
    private FacturaCompraRepository repository;

    public FacturaCompra crear(FacturaCompra factura) {

        return repository.save(factura);
    }

    public List<FacturaCompra> listarActivas() {
        return repository.findByEliminadoFalse();
    }

    public boolean eliminar(String id) {
        Optional<FacturaCompra> facturaOpt = repository.findById(id);
        if (facturaOpt.isPresent()) {
            FacturaCompra factura = facturaOpt.get();
            factura.setEliminado(true); // Baja lógica
            repository.save(factura);
            return true;
        }
        return false;
    }
}