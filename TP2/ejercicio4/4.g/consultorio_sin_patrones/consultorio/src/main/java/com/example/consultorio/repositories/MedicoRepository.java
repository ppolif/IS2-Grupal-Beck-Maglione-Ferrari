package com.example.consultorio.repositories;

import com.example.consultorio.entities.Medico;
import org.springframework.stereotype.Repository;

@Repository
public interface MedicoRepository extends PersonaRepository<Medico, String> {


}
