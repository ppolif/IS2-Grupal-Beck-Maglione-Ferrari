package com.example.zero.repositories;

import com.example.zero.entidades.compraProveedor.Proveedor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProveedorRepository extends JpaRepository<Proveedor, String> {

    default Optional<Proveedor> find(String id) {
        return id != null ? findById(id) : Optional.empty();
    }

    default Optional<Proveedor> find(UUID id) {
        return id != null ? find(id.toString()) : Optional.empty();
    }

    @Query("SELECT p FROM Proveedor p WHERE p.id = :id AND (p.eliminado = false OR p.eliminado IS NULL)")
    Optional<Proveedor> findActive(@Param("id") String id);

    default Optional<Proveedor> findActive(UUID id) {
        return id != null ? findActive(id.toString()) : Optional.empty();
    }

    @Query("SELECT p FROM Proveedor p WHERE p.cuit = :cuit AND (p.eliminado = false OR p.eliminado IS NULL)")
    Optional<Proveedor> findByCuitAndEliminadoFalse(@Param("cuit") String cuit);

    Optional<Proveedor> findByCuit(String cuit);

    @Query("SELECT p FROM Proveedor p WHERE p.razonSocial = :razonSocial AND (p.eliminado = false OR p.eliminado IS NULL)")
    Optional<Proveedor> findByRazonSocialAndEliminadoFalse(@Param("razonSocial") String razonSocial);

    @Query("SELECT p FROM Proveedor p WHERE (p.eliminado = false OR p.eliminado IS NULL) ORDER BY p.razonSocial ASC")
    List<Proveedor> findByEliminadoFalse();
}
