package com.example.zero.repositories;

import com.example.zero.entidades.zona.Pais;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaisRepository extends JpaRepository<Pais, String> {
    Pais findByNombre(String nombre);
}