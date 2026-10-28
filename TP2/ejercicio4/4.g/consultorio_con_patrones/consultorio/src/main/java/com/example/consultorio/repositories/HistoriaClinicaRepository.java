package com.example.consultorio.repositories;

import com.example.consultorio.entities.HistoriaClinica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HistoriaClinicaRepository extends JpaRepository<HistoriaClinica, String> {
    Optional<HistoriaClinica> findByPacienteDniAndEliminadoFalse(String pacienteId);
    boolean existsByPacienteDniAndEliminadoFalse(String pacienteId);
    List<HistoriaClinica> findByEliminadoFalse();
    Optional<HistoriaClinica> findByIdAndEliminadoFalse(String id);
}
