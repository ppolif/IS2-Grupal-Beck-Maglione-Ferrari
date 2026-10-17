package com.example.consultorio.service;

import com.example.consultorio.entities.FotoPaciente;
import com.example.consultorio.entities.Paciente;
import com.example.consultorio.repositories.FotoPacienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

@Service
public class FotoService {

    @Autowired
    private FotoPacienteRepository fotoPacienteRepository;

    @Transactional
    public FotoPaciente crearImagen(MultipartFile archivo) throws Exception {
        if (archivo != null && !archivo.isEmpty()) {
            FotoPaciente img = new FotoPaciente();
            img.setNombre(archivo.getOriginalFilename());
            img.setMime(archivo.getContentType());
            img.setContenido(archivo.getBytes());
            return fotoPacienteRepository.save(img);
        }
        return null;
    }

    @Transactional(readOnly = true)
    public List<FotoPaciente> listarTodas() {
        return fotoPacienteRepository.findAll();
    }

    @Transactional(readOnly = true)
    public FotoPaciente buscarPorId(String id) throws Exception {
        return fotoPacienteRepository.findById(id).orElseThrow(() -> new Exception("Imagen no encontrada"));
    }

    @Transactional
    public FotoPaciente actualizarImagen(String id, MultipartFile archivo) throws Exception {
        if (archivo != null && !archivo.isEmpty()) {
            FotoPaciente img = buscarPorId(id);
            img.setNombre(archivo.getOriginalFilename());
            img.setMime(archivo.getContentType());
            img.setContenido(archivo.getBytes());
            return fotoPacienteRepository.save(img);
        }
        return null;
    }

    @Transactional
    public FotoPaciente asignarNuevaFoto(Paciente paciente, MultipartFile archivo) throws Exception {
        if (archivo != null && !archivo.isEmpty()) {
            Optional<FotoPaciente> fotoActivaPrevia = obtenerFotoActivaDePaciente(paciente.getId());

            if (fotoActivaPrevia.isPresent()) {
                eliminarImagen(fotoActivaPrevia.get().getId());
            }

            FotoPaciente nuevaFoto = crearImagen(archivo);

            if (nuevaFoto != null) {
                nuevaFoto.setPaciente(paciente);
                return fotoPacienteRepository.save(nuevaFoto);
            }
        }
        return null;
    }

    // borrado lógico de una foto
    @Transactional
    public void eliminarImagen(String id) throws Exception {
        FotoPaciente img = buscarPorId(id);
        img.setEliminado(true);
        fotoPacienteRepository.save(img);
    }


    @Transactional(readOnly = true)
    public Optional<FotoPaciente> obtenerFotoActivaDePaciente(String pacienteId) {
        return fotoPacienteRepository.findByPacienteIdAndEliminadoFalse(pacienteId);
    }
}
