package org.example.entities;

import jakarta.persistence.Embeddable;
import lombok.*;
import java.util.Date;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Vigencia {
    private double precio;
    private Date desde;
    private Date hasta;
}