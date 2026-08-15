package org.example;

import jakarta.persistence.Entity;
import lombok.Data;
import lombok.experimental.SuperBuilder;

@Entity
@SuperBuilder
@Data
public class FacturaCompra extends Factura{
    //private Proveedor proveedor;
}
