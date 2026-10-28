package com.example.consultorio.repositories;

import com.example.consultorio.entities.Paciente;
import org.springframework.stereotype.Repository;

@Repository
public interface PacienteRepository extends PersonaRepository<Paciente, String> {


}
