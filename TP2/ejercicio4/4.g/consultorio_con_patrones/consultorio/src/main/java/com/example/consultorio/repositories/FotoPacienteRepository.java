package com.example.consultorio.repositories;

import com.example.consultorio.entities.FotoPaciente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FotoPacienteRepository extends JpaRepository<FotoPaciente, String> {
    // Obtiene la foto actualmente activa del paciente
    Optional<FotoPaciente> findByPacienteIdAndEliminadoFalse(String pacienteId);

    // Obtiene todo el historial de fotos (activas y eliminadas) del paciente
    List<FotoPaciente> findByPacienteId(String pacienteId);
}