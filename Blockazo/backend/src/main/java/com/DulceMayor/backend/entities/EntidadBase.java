package com.DulceMayor.backend.entities;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Superclase mapeada base para todas las entidades persistentes del dominio.
 * Centraliza la identidad (UUID) y la bandera de borrado lógico (eliminado = false por defecto)
 * según la convención definida en 01-dominio.md.
 */
@MappedSuperclass
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public abstract class EntidadBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(length = 36, nullable = false, updatable = false)
    private String id;

    @Builder.Default
    @Column(nullable = false)
    private boolean eliminado = false;
}

