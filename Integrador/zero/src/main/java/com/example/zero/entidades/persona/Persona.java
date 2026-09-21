package com.example.zero.entidades.persona;

import com.example.zero.enums.TipoDocumento;

import com.example.zero.entidades.empresa.Contacto;
import com.example.zero.entidades.zona.Direccion;
import com.example.zero.enums.TipoDocumento;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDate;
import java.util.Set;

/**
 * Clase base de Persona. Es abstracta porque en el
 * diagrama solo se instancian sus especializaciones Cliente y Empleado.
 *
 * Estrategia de herencia JOINED: cada subclase tiene su propia tabla, unida por el id,
 * lo cual es razonable dado que Cliente y Empleado agregan campos y relaciones propias.
 */
@Entity
@Table(name = "persona")
@Inheritance(strategy = InheritanceType.JOINED)
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@SuperBuilder
@ToString(exclude = "usuario")
public abstract class Persona {

//    @Id
//    @UuidGenerator
//    @Column(name = "id", updatable = false, nullable = false, length = 36)
//    private String id;

    @Id
    @Column(name = "numero_documento", nullable = false, length = 20)
    private String numeroDocumento;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, length = 100)
    private String apellido;

    @Column(name = "fecha_nacimiento", nullable = false)
    private LocalDate fechaNacimiento;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_documento", nullable = false, length = 20)
    private TipoDocumento tipoDocumento;


    @Column(nullable = false)
    @lombok.Builder.Default
    private boolean eliminado = false;

    // Relación 1 a 1 con Usuario
    // Usuario es el lado dueño de la FK (usuario.persona_id), ver clase Usuario.
    @OneToOne(mappedBy = "persona", fetch = FetchType.LAZY)
    private Usuario usuario;

    // --- Relaciones con clases que NO estan hechas todavia) ---

    // Persona *..1 Imagen (cada persona puede tener una imagen de perfil).
    // @ManyToOne(fetch = FetchType.LAZY)
    // @JoinColumn(name = "imagen_id")
    // private Imagen imagen;

    // Persona *..1 Direccion (muchas personas pueden compartir una misma dirección).
     @ManyToOne(fetch = FetchType.LAZY)
     @JoinColumn(name = "direccion_id")
     private Direccion direccion;

    // Persona 1..* Contacto (una persona puede tener varios medios de contacto).
     @OneToMany(fetch = FetchType.LAZY)
     private Set<Contacto> contactos = new java.util.HashSet<>();
}