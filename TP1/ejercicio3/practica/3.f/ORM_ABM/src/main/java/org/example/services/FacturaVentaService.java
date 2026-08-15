package org.example.services;

import org.example.entities.FacturaVenta;
import org.example.repositories.FacturaVentaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class FacturaVentaService {

    @Autowired
    private FacturaVentaRepository repository;

    public FacturaVenta crear(FacturaVenta factura) {
        // Acá podrías agregar lógica, como verificar si el cliente existe
        return repository.save(factura);
    }

    public List<FacturaVenta> listarActivas() {
        return repository.findByEliminadoFalse();
    }

    public boolean eliminar(String id) {
        Optional<FacturaVenta> facturaOpt = repository.findById(id);
        if (facturaOpt.isPresent()) {
            FacturaVenta factura = facturaOpt.get();
            factura.setEliminado(true); // Baja lógica
            repository.save(factura);
            return true;
        }
        return false;
    }
}