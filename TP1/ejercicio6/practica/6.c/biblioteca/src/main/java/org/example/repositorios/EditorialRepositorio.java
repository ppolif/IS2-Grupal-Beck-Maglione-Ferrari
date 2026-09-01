package org.example.repositorios;

import org.example.entidades.Editorial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EditorialRepositorio extends JpaRepository<Editorial, String> {

    List<Editorial> findByAltaTrue();

    Optional<Editorial> findByIdAndAltaTrue(String id);
}