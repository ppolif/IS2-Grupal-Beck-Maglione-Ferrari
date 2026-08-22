package com.example.demo;

import com.example.demo.business.logic.error.ErrorServiceException;
import com.example.demo.business.logic.service.NacionalidadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

//@SpringBootApplication se coloca en el main.
//engloba tres anotaciones fundamentales de Spring:
// @Configuration: Marca la clase como fuente de definiciones
// de beans para el contexto de la aplicación.
// @ComponentScan: Indica a Spring que busque controladores, servicios y
// otros componentes en el paquete actual y en sus subpaquetes.
//@EnableAutoConfiguration: Indica a Spring Boot que configure los beans automáticamente
@SpringBootApplication
public class DemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);


    }

}
