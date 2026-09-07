package com.example.tinder.repositorios;

import com.example.tinder.entidades.Voto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VotoRepositorio extends JpaRepository<Voto, String> {

    @Query("SELECT v FROM Voto v WHERE v.mascota1.id = :id ORDER BY v.fecha DESC")
    public List<Voto> buscarVotosPropios(@Param("id") String id);

    @Query("SELECT v FROM Voto v WHERE v.mascota2.id = :id ORDER BY v.fecha DESC")
    public List<Voto> buscarVotosRecibidos(@Param("id") String id);
}
