package com.example.junit_prueba_2.services;

import com.example.junit_prueba_2.entities.Rectangulo;

public class RectanguloService {

    public double calcularArea(Rectangulo r) {
        return r.getAncho()*r.getLargo();
    }

    public double calcularPerimetro(Rectangulo r) {
        return 2*(r.getLargo()+r.getAncho());
    }
}
