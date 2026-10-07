package com.example.pelis_api_externa.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Pelicula {
    @JsonProperty("title")
    private String titulo;

    @JsonProperty("overview")
    private String sinopsis;

    @JsonProperty("release_date")
    private String fechaEstreno;

    // TMDb envía solo el final de la ruta del póster (ej: /xYj...jpg)
    @JsonProperty("poster_path")
    private String posterPath;


    //este getter es leído automáticamente por thymeleaf cuando
    // en el HTML hace ${pelicula.posterUrl}. onstruye la ruta para que la imagen se vea.
    public String getPosterUrl() {
        if (posterPath != null) {
            return "https://image.tmdb.org/t/p/w500" + posterPath;
        }
        // si la película no tiene póster, mostramos una imagen genérica
        return "https://via.placeholder.com/500x750/1a1a1a/ffffff?text=Sin+P%C3%B3ster";
    }
}
