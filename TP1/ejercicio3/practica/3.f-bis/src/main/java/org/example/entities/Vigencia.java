package org.example.entities;

import jakarta.persistence.*;
import lombok.*;
import java.util.Date;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Vigencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private double precio;
    private Date desde;
    private Date hasta;

    @ManyToOne
    @JoinColumn(name = "id")
    private Producto producto;
}