package org.example.entities;

import org.example.enums.TipoPago;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class FormaDePago {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    private String id;

    @Enumerated(EnumType.STRING)
    private TipoPago tipoPago;

    private String observacion;

    @Builder.Default
    private boolean eliminado = false;
}