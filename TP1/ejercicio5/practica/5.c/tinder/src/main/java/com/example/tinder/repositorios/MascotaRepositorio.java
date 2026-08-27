package com.example.tinder.repositorios;

import com.example.tinder.entidades.Mascota;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MascotaRepositorio extends JpaRepository<Mascota, String> {

    @Query("SELECT m FROM Mascota m WHERE m.usuario.id = :id")
    public List<Mascota> buscarMascotaPorUsuario(@Param("id") String id);
}
