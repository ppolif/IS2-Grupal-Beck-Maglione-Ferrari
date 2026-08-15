package org.example.repositories;

import org.example.entities.Factura;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface FacturaRepository extends JpaRepository<Factura, String> {

    // Persistencia automática (JPA busca ambas, compras y ventas)
    List<Factura> findByEliminadoFalse();

    // Usamos 'default' para poder escribir código con llaves {} en una interfaz
    default boolean aplicarBajaLogica(String id) {
        Optional<Factura> facturaOpt = this.findById(id);
        if (facturaOpt.isPresent()) {
            Factura factura = facturaOpt.get();
            factura.setEliminado(true); // Baja lógica
            this.save(factura);
            return true;
        }
        return false;
    }
}