package com.example.persona.services;

import com.example.persona.dtos.MigracionResultadoDto;
import org.springframework.web.multipart.MultipartFile;

public interface MigracionService {
    //opcion para migrar un archivo que esta en el file system
    MigracionResultadoDto migrarDesdeArchivo(String rutaArchivo) throws Exception;

    //opcion para migrar un archivo subido directamente
    MigracionResultadoDto migrarDesdeArchivoSubido(MultipartFile archivo) throws Exception;
}
