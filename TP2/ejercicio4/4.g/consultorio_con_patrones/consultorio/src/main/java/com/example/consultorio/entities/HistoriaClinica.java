package com.example.consultorio.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

// Utilizamos el patron Builder en HistoriaClinica porque es un objeto complejo compuesto por
// relaciones con otras entidades (Paciente, Usuario) y una coleccion de DetalleHistoriaClinica,
// ademas de valores iniciales por defecto (lista de detalles inicializada y eliminado = false).
// El Builder permite ensamblar la historia clinica paso a paso de manera clara y segura.
@Entity
@Data
@NoArgsConstructor
public class HistoriaClinica{
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne
    private Paciente paciente;

    @ManyToOne
    private Usuario usuario;

    @OneToMany(mappedBy = "historia", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DetalleHistoriaClinica> detalles = new ArrayList<>();

    private boolean eliminado;

    // Constructor privado usado exclusivamente por el Builder para instanciar la entidad
    // garantizando que la lista de detalles nunca quede nula y vinculando cada detalle con esta historia.
    private HistoriaClinica(Builder builder) {
        this.id = builder.id;
        this.paciente = builder.paciente;
        this.usuario = builder.usuario;
        this.detalles = builder.detalles != null ? builder.detalles : new ArrayList<>();
        this.eliminado = builder.eliminado;
        for (DetalleHistoriaClinica detalle : this.detalles) {
            detalle.setHistoria(this);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    // Se implementa como clase estatica interna dentro de la propia entidad siguiendo la convencion
    // estandar de Java: permite acceder al constructor privado de HistoriaClinica, encapsula
    // los valores por defecto (como la lista vacia de detalles y eliminado = false) y ofrece
    // metodos encadenables (interfaz fluida) sin necesidad de exponer constructores complejos.
    public static class Builder {
        private String id;
        private Paciente paciente;
        private Usuario usuario;
        private List<DetalleHistoriaClinica> detalles = new ArrayList<>();
        private boolean eliminado = false;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder paciente(Paciente paciente) {
            this.paciente = paciente;
            return this;
        }

        public Builder usuario(Usuario usuario) {
            this.usuario = usuario;
            return this;
        }

        public Builder detalles(List<DetalleHistoriaClinica> detalles) {
            this.detalles = detalles != null ? new ArrayList<>(detalles) : new ArrayList<>();
            return this;
        }

        public Builder agregarDetalle(DetalleHistoriaClinica detalle) {
            if (this.detalles == null) {
                this.detalles = new ArrayList<>();
            }
            if (detalle != null) {
                this.detalles.add(detalle);
            }
            return this;
        }

        public Builder eliminado(boolean eliminado) {
            this.eliminado = eliminado;
            return this;
        }

        public HistoriaClinica build() {
            return new HistoriaClinica(this);
        }
    }

}
