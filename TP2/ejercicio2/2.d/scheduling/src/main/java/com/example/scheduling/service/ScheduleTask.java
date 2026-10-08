package com.example.scheduling.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.ZoneId;

/*
 - fixedRate: Ejecuta a intervalos fijos desde el inicio de cada ejecución.
 - fixedDelay: Ejecuta a intervalos fijos desde el final de cada ejecución.
 - initialDelay: Retrasa la primera ejecución.
 - cron: Ejecuta según una expresión CRON.
 - zone: Especifica la zona horaria para una expresión CRON.
 */

@Component
public class ScheduleTask {

    //lunes, miercoles, viernes a las 15
    //@Scheduled(fixedRate = 5000)
    @Scheduled(cron = "0 0 15 * * 1,3,5", zone = "America/Bogota")
    public void scheduleMessage() {
        System.out.println("hola");
    }

//    public static void main(String[] args) {
//        ZoneId.getAvailableZoneIds().forEach(System.out::println);
//    }
}
