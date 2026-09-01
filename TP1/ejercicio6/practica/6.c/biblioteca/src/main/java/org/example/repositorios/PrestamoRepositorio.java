package org.example.repositorios;

import org.example.entidades.Prestamo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PrestamoRepositorio extends JpaRepository<Prestamo, String> {

    List<Prestamo> findByAltaTrue();

    Optional<Prestamo> findByIdAndAltaTrue(String id);

    // Busca todos los préstamos activos (sin fecha de devolución) de un usuario específico
    List<Prestamo> findByUsuarioIdAndFechaDevolucionIsNullAndAltaTrue(String usuarioId);
}
