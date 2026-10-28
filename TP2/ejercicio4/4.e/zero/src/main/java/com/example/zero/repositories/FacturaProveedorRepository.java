package com.example.zero.repositories;

import com.example.zero.entidades.compraProveedor.FacturaProveedor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FacturaProveedorRepository extends JpaRepository<FacturaProveedor, String> {

    @Query("SELECT fp FROM FacturaProveedor fp LEFT JOIN FETCH fp.proveedor LEFT JOIN FETCH fp.formaDePago WHERE fp.id = :id AND (fp.eliminado = false OR fp.eliminado IS NULL)")
    Optional<FacturaProveedor> findActive(@Param("id") String id);

    @Query("SELECT fp FROM FacturaProveedor fp LEFT JOIN FETCH fp.proveedor LEFT JOIN FETCH fp.formaDePago WHERE (fp.eliminado = false OR fp.eliminado IS NULL) ORDER BY fp.fechaFactura DESC")
    List<FacturaProveedor> findByEliminadoFalseOrderByFechaFacturaDesc();

    @Query("SELECT fp FROM FacturaProveedor fp LEFT JOIN FETCH fp.proveedor LEFT JOIN FETCH fp.formaDePago WHERE fp.proveedor.id = :proveedorId AND (fp.eliminado = false OR fp.eliminado IS NULL) ORDER BY fp.fechaFactura DESC")
    List<FacturaProveedor> findByProveedorIdAndEliminadoFalse(@Param("proveedorId") String proveedorId);
}
