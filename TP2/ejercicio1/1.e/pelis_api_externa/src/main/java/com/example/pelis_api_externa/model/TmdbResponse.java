package com.example.pelis_api_externa.model;

import lombok.Data;
import java.util.List;

@Data
public class TmdbResponse {
    // el nombre del atributo debe ser exactamente "results" para que
    // Jackson (el traductor interno de Spring) lo asocie con la lista del JSON de TMDb
    private List<Pelicula> results;
}
