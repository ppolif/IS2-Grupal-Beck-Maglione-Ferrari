package org.example.repositories;

import org.example.entities.FacturaCompra;
import org.example.entities.FacturaVenta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface FacturaCompraRepository extends JpaRepository<FacturaCompra, String> {
    List<FacturaCompra> findByEliminadoFalse();
}