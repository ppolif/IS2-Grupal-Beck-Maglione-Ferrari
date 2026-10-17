package com.example.consultorio.service;

import com.example.consultorio.entities.Medico;
import com.example.consultorio.template.TemplateCrud;
import org.springframework.stereotype.Service;

//en este servicio no hace falta logica porque el patron plantilla se encarga de todo!! en
//TemplateCrud
@Service
public class MedicoService extends TemplateCrud<Medico> {

    @Override
    public Medico instanciarNuevaEntidad() {
        return new Medico();
    }
}
