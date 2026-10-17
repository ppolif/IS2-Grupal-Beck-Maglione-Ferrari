package com.example.consultorio.repositories;

import com.example.consultorio.entities.EntidadPersona;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.List;
import java.util.Optional;

@NoRepositoryBean
public interface PersonaRepository<T extends EntidadPersona, ID> extends JpaRepository<T, ID> {
    Optional<T> findByDni(String dni);

    boolean existsByDni(String dni);

    Optional<T> findByDniAndEliminadoFalse(String dni);

    boolean existsByDniAndEliminadoFalse(String dni);

    List<T> findByEliminadoFalse();

    Optional<T> findByIdAndEliminadoFalse(String id);
}
