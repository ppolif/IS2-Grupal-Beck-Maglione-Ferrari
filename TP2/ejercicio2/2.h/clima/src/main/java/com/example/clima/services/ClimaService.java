package com.example.clima.services;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Service
public class ClimaService {

    public ResponseEntity<String> obtenerClimaApi(String ciudad) {
        try {
            String apiKey = "3e9af427babde906097feb88a58b2479";
            String url = "https://api.openweathermap.org/data/2.5/weather"
                    + "?q=" + ciudad
                    + "&appid=" + apiKey;
//                    + "&units=metric"
//                    + "&lang=es";

            RestTemplate restTemplate = new RestTemplate();
            String clima = restTemplate.getForObject(url, String.class);

            return ResponseEntity.ok(clima);
        } catch (HttpClientErrorException.Unauthorized e){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Error al autenticarse");

        }
    }




}
