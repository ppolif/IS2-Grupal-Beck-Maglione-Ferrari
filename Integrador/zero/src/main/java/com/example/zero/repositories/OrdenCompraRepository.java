package com.example.zero.repositories;

import com.example.zero.entidades.compraCliente.OrdenCompra;
import com.example.zero.entidades.persona.Cliente;
import com.example.zero.enums.EstadoOrdenCompra;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrdenCompraRepository extends JpaRepository<OrdenCompra, String> {

    default Optional<OrdenCompra> find(String id) {
        return id != null ? findById(id) : Optional.empty();
    }

    default Optional<OrdenCompra> find(UUID id) {
        return id != null ? find(id.toString()) : Optional.empty();
    }

    @Query("SELECT o FROM OrdenCompra o WHERE o.id = :id AND o.eliminado = false")
    Optional<OrdenCompra> findActive(@Param("id") String id);

    default Optional<OrdenCompra> findActive(UUID id) {
        return id != null ? findActive(id.toString()) : Optional.empty();
    }

    Optional<OrdenCompra> findByClienteAndEstadoOrdenCompraAndEliminadoFalse(Cliente cliente, EstadoOrdenCompra estado);

    @Query("SELECT o FROM OrdenCompra o WHERE o.cliente.numeroDocumento = :doc AND o.estadoOrdenCompra = :estado AND o.eliminado = false")
    Optional<OrdenCompra> findCarritoActivoPorClienteDoc(@Param("doc") String documentoCliente, @Param("estado") EstadoOrdenCompra estado);

    List<OrdenCompra> findByClienteAndEliminadoFalse(Cliente cliente);

    List<OrdenCompra> findByEliminadoFalse();
}

