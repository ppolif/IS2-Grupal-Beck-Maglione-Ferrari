package org.example.repositories;

import org.example.entities.Detalle;
import org.example.entities.FacturaVenta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DetalleRepository extends JpaRepository<Detalle, String> {
    List<Detalle> findByEliminadoFalse();
}