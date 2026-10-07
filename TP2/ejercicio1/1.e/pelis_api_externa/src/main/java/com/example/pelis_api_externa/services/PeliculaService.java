package com.example.pelis_api_externa.services;

import com.example.pelis_api_externa.model.Pelicula;
import com.example.pelis_api_externa.model.TmdbResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

@Service
public class PeliculaService {
    private final String API_KEY = "825256ded88bc37c301c1e40df7a51ec";

    //el url base es el de la api externa, que escucha las peticiones que se hagan desde esta app
    //el endpoint es /discover/movie indica al servidor qué servicio o recurso en particular se quiere consultar
    //el controlador del servidor está configurado para escuchar peticiones en ese path
    //endpoint (o path) es la ruta específica dentro del servidor que está escuchando y esperando a que llegue una petición
    private final String BASE_URL = "https://api.themoviedb.org/3/discover/movie";

    private final RestTemplate restTemplate;

    @Autowired
    public PeliculaService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public List<Pelicula> buscarPeliculas(Integer anio, String genero) {

        //url base con el filtro obligatorio de tiempo
        //le damos la clave para que pueda acceder a la api
        //filtro de año
        String urlBusqueda = "https://api.themoviedb.org/3/discover/movie?api_key=" + API_KEY
                + "&language=es-ES"
                + "&primary_release_year=" + anio;

        // filtro de género opcional
        if (genero != null && !genero.isEmpty()) {
            urlBusqueda += "&with_genres=" + genero;
        }

        // la API de TMDb no devuelve una lista pura de películas, sino un objeto estructurado o "envoltorio" de paginación
        // adentro de ese objeto, la API manda primero metadatos de control (como "page": 1) y dentro de la propiedad "results"
        // contiene la lista real de las películas
        TmdbResponse respuesta = restTemplate.getForObject(urlBusqueda.toString(), TmdbResponse.class);

        // verificar que no sea nulo y extraer la lista interna de películas
        if (respuesta != null && respuesta.getResults() != null) {
            return respuesta.getResults();
        }

        // sino devolvemos una lista vacía
        return java.util.List.of();
    }
}
