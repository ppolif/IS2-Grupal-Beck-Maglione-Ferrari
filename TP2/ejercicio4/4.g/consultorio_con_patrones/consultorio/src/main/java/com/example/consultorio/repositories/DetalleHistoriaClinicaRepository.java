package com.example.consultorio.repositories;

import com.example.consultorio.entities.DetalleHistoriaClinica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DetalleHistoriaClinicaRepository extends JpaRepository<DetalleHistoriaClinica, String> {
    List<DetalleHistoriaClinica> findByHistoriaIdAndEliminadoFalseOrderByFechaHistoriaDesc(String historiaId);
}
