package com.example.zero.repositories;

import com.example.zero.entidades.empresa.Empresa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmpresaRepository extends JpaRepository<Empresa, String> {
    List<Empresa> findByEliminadoFalse();
    Optional<Empresa> findFirstByEliminadoFalse();
}
