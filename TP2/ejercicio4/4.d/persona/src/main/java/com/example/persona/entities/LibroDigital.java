package com.example.persona.entities;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.envers.Audited;

import java.time.LocalDate;
import java.util.List;

@Entity
@DiscriminatorValue("DIGITAL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Audited
public class LibroDigital extends Libro {

    @Column(name = "tamano_mb")
    private Double tamanoMB;

    @Column(name = "formato")
    private String formato;

    @Column(name = "url_descarga")
    private String urlDescarga;

    public LibroDigital(String titulo, int fecha, String genero, int paginas,
                        LocalDate fechaVencimientoDevolucion, List<Autor> autores,
                        Double tamanoMB, String formato, String urlDescarga) {
        super(titulo, fecha, genero, paginas, fechaVencimientoDevolucion, autores);
        this.tamanoMB = tamanoMB;
        this.formato = formato;
        this.urlDescarga = urlDescarga;
    }
}

