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
}
