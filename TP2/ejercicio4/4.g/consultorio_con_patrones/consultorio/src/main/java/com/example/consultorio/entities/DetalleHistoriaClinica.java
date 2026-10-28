package com.example.consultorio.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

// Utilizamos el patron Builder en DetalleHistoriaClinica porque la creacion de un detalle
// involucra multiples atributos (texto del informe, medico, historia clinica asociada) y
// valores por defecto que siempre deben inicializarse de forma consistente (como la fecha actual
// y el estado eliminado en false). El Builder evita tener constructores gigantes o depender
// de multiples llamadas a setters dispersas por el codigo.
@Entity
@Data
@NoArgsConstructor
public class DetalleHistoriaClinica{
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private Date fechaHistoria;
    private String detalleHistoria;

    @ManyToOne
    private HistoriaClinica historia;

    @ManyToOne
    private Medico medico;

    private boolean eliminado;

    // Constructor privado que recibe el Builder: asegura que la construccion paso a paso
    // sea controlada por el Builder, mientras que @NoArgsConstructor deja el constructor vacio
    // unicamente para el funcionamiento interno de JPA/Hibernate y Spring.
    private DetalleHistoriaClinica(Builder builder) {
        this.id = builder.id;
        this.fechaHistoria = builder.fechaHistoria;
        this.detalleHistoria = builder.detalleHistoria;
        this.historia = builder.historia;
        this.medico = builder.medico;
        this.eliminado = builder.eliminado;
    }

    public static Builder builder() {
        return new Builder();
    }

    // Se implementa el Builder como una clase estatica interna (estandar en Java / Effective Java)
    // porque mantiene alta cohesion (la logica de construccion vive junto a la entidad que construye),
    // puede acceder al constructor privado de DetalleHistoriaClinica y evita crear clases sueltas
    // adicionales cuando no existen multiples constructores polimorficos para el mismo objeto.
    public static class Builder {
        private String id;
        private Date fechaHistoria = new Date();
        private String detalleHistoria;
        private HistoriaClinica historia;
        private Medico medico;
        private boolean eliminado = false;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder fechaHistoria(Date fechaHistoria) {
            this.fechaHistoria = fechaHistoria;
            return this;
        }

        public Builder detalleHistoria(String detalleHistoria) {
            this.detalleHistoria = detalleHistoria;
            return this;
        }

        public Builder historia(HistoriaClinica historia) {
            this.historia = historia;
            return this;
        }

        public Builder medico(Medico medico) {
            this.medico = medico;
            return this;
        }

        public Builder eliminado(boolean eliminado) {
            this.eliminado = eliminado;
            return this;
        }

        public DetalleHistoriaClinica build() {
            return new DetalleHistoriaClinica(this);
        }
    }

}
