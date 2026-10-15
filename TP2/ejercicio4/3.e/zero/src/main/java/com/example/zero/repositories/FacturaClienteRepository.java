package com.example.zero.repositories;

import com.example.zero.entidades.compraCliente.FacturaCliente;
import com.example.zero.entidades.persona.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FacturaClienteRepository extends JpaRepository<FacturaCliente, String> {

    @Query("SELECT fc FROM FacturaCliente fc " +
           "WHERE fc.ordenCompra.cliente = :cliente " +
           "AND (fc.eliminado = false OR fc.eliminado IS NULL) " +
           "ORDER BY fc.fechaFactura DESC")
    List<FacturaCliente> findByClienteOrderByFechaFacturaDesc(@Param("cliente") Cliente cliente);

    @Query("SELECT fc FROM FacturaCliente fc WHERE fc.id = :id AND (fc.eliminado = false OR fc.eliminado IS NULL)")
    Optional<FacturaCliente> findActive(@Param("id") String id);
}
