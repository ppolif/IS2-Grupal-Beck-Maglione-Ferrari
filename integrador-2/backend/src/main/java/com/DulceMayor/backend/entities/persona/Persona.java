package com.DulceMayor.backend.entities.persona;

import com.DulceMayor.backend.entities.EntidadBase;
import com.DulceMayor.backend.entities.Usuario;
import com.DulceMayor.backend.entities.contacto.Contacto;
import com.DulceMayor.backend.entities.direccion.Direccion;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * Entidad base abstracta que representa a una persona física en el sistema,
 * ya sea un cliente {@link Minorista} o un {@link Empleado}.
 * Aplica estrategia de herencia JPA {@link InheritanceType#JOINED}.
 */
@Entity
@Table(name = "personas")
@Inheritance(strategy = InheritanceType.JOINED)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@ToString(callSuper = true, exclude = {"usuarios", "direcciones", "contactos"})
public abstract class Persona extends EntidadBase {

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, length = 100)
    private String apellido;

    @Column(name = "fecha_nacimiento")
    private LocalDate fechaNacimiento;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_documento", nullable = false, length = 20)
    private TipoDocumento tipoDocumento;

    @Column(name = "numero_documento", nullable = false, length = 30)
    private String numeroDocumento;

    @Builder.Default
    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "persona_id")
    private List<Usuario> usuarios = new ArrayList<>();

    @Builder.Default
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "persona_id")
    private List<Direccion> direcciones = new ArrayList<>();

    @Builder.Default
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "persona_id")
    private List<Contacto> contactos = new ArrayList<>();

    /**
     * Retorna el usuario activo actualmente asociado a la persona (no eliminado lógicamente).
     * Mantiene compatibilidad con el resto del dominio que asume un único usuario vigente.
     * sino habia que cambiar todo jaja
     */
    public Usuario getUsuario() {
        if (usuarios == null) {
            return null;
        }
        return usuarios.stream()
                .filter(u -> !u.isEliminado())
                .findFirst()
                .orElse(null);
    }

    /**
     * Asocia un nuevo usuario activo a la persona.
     * Si la lista de usuarios no está inicializada, la crea y agrega la instancia.
     */
    public void setUsuario(Usuario usuario) {
        if (this.usuarios == null) {
            this.usuarios = new ArrayList<>();
        }
        if (usuario != null && !this.usuarios.contains(usuario)) {
            this.usuarios.add(usuario);
        }
    }

    /// aca tebemos mas patron builder, esto lo ponemos así para no cambiar todos los lugares desde los que se buildea una persona
    /// antes estaba con un solo usuario (lo cual esta bien en el sentido de que una persona tiene un solo user activo)
    /// pero ahora es una lista, asi que para no cambiar todas las llamadas usamos una regla de lombok:
    /// "Si tú escribes con tus propias manos una parte de la clase Builder dentro de tu entidad, yo no la sobreescribo, la respeto
    /// y le agrego el resto de cosas automáticamente"
    /// C representa la clase final que se está construyendo (ej: Empleado o Minorista).
    /// B representa el Builder de esa clase
    public abstract static class PersonaBuilder<C extends Persona, B extends PersonaBuilder<C, B>>
            extends EntidadBaseBuilder<C, B> {
        public B usuario(Usuario usuario) {
            if (usuario != null) {
                return usuarios(new ArrayList<>(List.of(usuario)));
            }
            return self();
        }
    }
}

