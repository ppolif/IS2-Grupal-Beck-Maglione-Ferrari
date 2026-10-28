package com.example.pelis_api_externa.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

//le decimos a spring que cree un objeto tipo RestTemplate cada vez que se inicie la app
@Configuration
public class RestTemplateConfig {

    //le decimos a spring que lo gestione
    //asi se puede inyectar en cualquier parte del proyecto
    @Bean
    ///RestTemplate es una clase de Spring para hacer llamadas HTTP
    ///a servicios REST desde apps java. por eso permite consumir APIs REST
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }


}
