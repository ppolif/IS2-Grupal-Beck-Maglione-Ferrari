package com.example.persona.scheduling;

import com.example.persona.services.NotificacionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

//tareas programadas de Spring Scheduling para el envío automático de correos:
 // 1) avisos 1 día antes del vencimiento
 // 2) Saludos de cumpleaños con HTML y enlace a la facultad
//en esta clase hacemos el scheduling, o sea que solo llamamos a los metodos que corresponden
//anotandolos con el schedule

//es necesario anotarlo como component
@Component
public class NotificacionScheduler {

    private final NotificacionService notificacionService;

    public NotificacionScheduler(NotificacionService notificacionService) {
        this.notificacionService = notificacionService;
    }


    @Scheduled(cron = "${app.scheduling.cron-vencimiento:0 0 8 * * *}")
    public void ejecutarAvisosVencimientoLibros() {
        notificacionService.enviarAvisosVencimientoLibros();
    }


    @Scheduled(cron = "${app.scheduling.cron-cumpleanios:0 0 8 * * *}")
    public void ejecutarSaludosCumpleanios() {
        notificacionService.enviarSaludosCumpleanios();
    }
}
