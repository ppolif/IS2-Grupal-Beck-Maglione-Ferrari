package com.example.zero.repositories;

import com.example.zero.entidades.compra.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StockRepository extends JpaRepository<Stock, String> {

    @Query("SELECT s FROM Stock s WHERE s.id = :id AND s.eliminado = false")
    Optional<Stock> findActive(@Param("id") String id);

    List<Stock> findByEliminadoFalse();

    @Query("SELECT s FROM Stock s WHERE s.detalle.producto.id = :prodId AND s.eliminado = false ORDER BY s.detalle.factura.fechaFactura DESC")
    List<Stock> findStockPorProductoDesc(@Param("prodId") String prodId);

    default Optional<Stock> findUltimoStockPorProducto(String prodId) {
        List<Stock> list = findStockPorProductoDesc(prodId);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    @Query("SELECT s FROM Stock s WHERE s.detalle.id = :detalleId")
    Optional<Stock> findByDetalleId(@Param("detalleId") String detalleId);

    Optional<Stock> findByDetalle(com.example.zero.entidades.compra.Detalle detalle);
}


