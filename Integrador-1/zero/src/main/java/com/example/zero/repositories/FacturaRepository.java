package com.example.zero.repositories;

import com.example.zero.entidades.compra.Factura;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.zero.entidades.compraCliente.FacturaCliente;
import com.example.zero.entidades.persona.Cliente;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FacturaRepository extends JpaRepository<Factura, String> {

    default Optional<Factura> find(String id) {
        return id != null ? findById(id) : Optional.empty();
    }

    default Optional<Factura> find(UUID id) {
        return id != null ? find(id.toString()) : Optional.empty();
    }

    @Query("SELECT f FROM Factura f WHERE f.id = :id AND f.eliminado = false")
    Optional<Factura> findActive(@Param("id") String id);

    default Optional<Factura> findActive(UUID id) {
        return id != null ? findActive(id.toString()) : Optional.empty();
    }

    Optional<Factura> findByNumeroFacturaAndEliminadoFalse(Long numeroFactura);

    List<Factura> findByEliminadoFalseOrderByFechaFacturaDesc();

    // Se trae la ultima factura (Top, ya que estan ordenadas de forma descendente)
    Optional<Factura> findTopByOrderByNumeroFacturaDesc();

    @Query("SELECT f FROM Factura f " +
           "WHERE f.fechaFactura BETWEEN :inicio AND :fin " +
           "AND (f.eliminado = false OR f.eliminado IS NULL) " +
           "AND TYPE(f) != FacturaProveedor " +
           "ORDER BY f.fechaFactura DESC")
    List<Factura> findVentasEntreFechas(@Param("inicio") LocalDateTime inicio, @Param("fin") LocalDateTime fin);

    @Query("SELECT fc FROM FacturaCliente fc " +
           "WHERE fc.ordenCompra.cliente = :cliente " +
           "AND (fc.eliminado = false OR fc.eliminado IS NULL) " +
           "ORDER BY fc.fechaFactura DESC")
    List<Factura> findByClienteOrderByFechaFacturaDesc(@Param("cliente") Cliente cliente);
}

