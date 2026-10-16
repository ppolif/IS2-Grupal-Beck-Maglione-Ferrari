package com.example.consultorio.service;

import com.example.consultorio.entities.Paciente;
import com.example.consultorio.template.TemplateCrud;
import org.springframework.stereotype.Service;

//en este servicio no hace falta logica porque el patron plantilla se encarga de todo!! en
//TemplateCrud
@Service
public class PacienteService extends TemplateCrud<Paciente> {

    @Override
    public Paciente instanciarNuevaEntidad() {
        return new Paciente();
    }
}
