package com.example.tinder.entidades;

import jakarta.persistence.*;

import java.util.Date;

@Entity
public class Voto {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Temporal(TemporalType.TIMESTAMP)
    private Date fecha;

    @Temporal(TemporalType.TIMESTAMP)
    private Date respuesta;

    @ManyToOne
    private Mascota mascota1;

    @ManyToOne
    private Mascota mascota2;

    public Mascota getMascota1() {
        return mascota1;
    }

    public void setMascota1(Mascota mascota1) {
        this.mascota1 = mascota1;
    }

    public Mascota getMascota2() {
        return mascota2;
    }

    public void setMascota2(Mascota mascota2) {
        this.mascota2 = mascota2;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    public Date getRespuesta() {
        return respuesta;
    }

    public void setRespuesta(Date respuesta) {
        this.respuesta = respuesta;
    }
}
