package org.example.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.enums.MovimientoStock;

import java.util.Date;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Stock {
    private Detalle detalle;

    @Enumerated(EnumType.STRING)
    private MovimientoStock tipo;

    private Date fechaMovimiento;

    private Integer cantActual;

    private Integer umbralReposicion;

    private Integer cantMax;
}
