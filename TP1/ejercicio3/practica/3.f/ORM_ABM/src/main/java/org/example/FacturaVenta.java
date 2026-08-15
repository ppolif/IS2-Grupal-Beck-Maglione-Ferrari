package org.example;


import jakarta.persistence.Entity;
import lombok.Data;
import lombok.experimental.SuperBuilder;

@Entity
@SuperBuilder
@Data
public class FacturaVenta extends Factura{
    //private Cliente cliente;
}
