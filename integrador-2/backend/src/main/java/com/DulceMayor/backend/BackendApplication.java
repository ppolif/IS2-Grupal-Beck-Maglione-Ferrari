package com.DulceMayor.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Clase principal de inicio de Spring Boot para DulceMayor Backend.
 *
 * <p>Anotaciones habilitadas:</p>
 * <ul>
 *   <li>{@link EnableAsync}: Permite la ejecución en segundo plano de oyentes de eventos
 *       (por ejemplo, envío de correos sin bloquear la transacción HTTP).</li>
 *   <li>{@link EnableScheduling}: Habilita la ejecución programada de tareas como el Newsletter mensual.</li>
 * </ul>
 */
@EnableAsync
@EnableScheduling
@SpringBootApplication
public class BackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(BackendApplication.class, args);
	}

}
