package com.example.persona.services;

import com.example.persona.dtos.MigracionResultadoDto;
import com.example.persona.dtos.RegistroMigradoDto;
import com.example.persona.entities.Domicilio;
import com.example.persona.entities.Localidad;
import com.example.persona.entities.Persona;
import com.example.persona.repositories.LocalidadRepository;
import com.example.persona.repositories.PersonaRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.StringTokenizer;

@Service
public class MigracionServiceImpl implements MigracionService {

    private static final Logger log = LoggerFactory.getLogger(MigracionServiceImpl.class);

    ///traemos la ruta del app properties
    @Value("${app.migracion.archivo:../migracion.txt}")
    private String rutaArchivoConfigurada;

    @Autowired
    private PersonaRepository personaRepository;

    @Autowired
    private LocalidadRepository localidadRepository;

    @Override
    @Transactional
    public MigracionResultadoDto migrarDesdeArchivo(String rutaEspecifica) throws Exception {
        File file = resolverArchivo(rutaEspecifica);
        if (!file.exists()) {
            throw new FileNotFoundException("No se encontró el archivo de migración en la ruta: " + file.getAbsolutePath());
        }

        log.info("Iniciando migración desde archivo: {}", file.getAbsolutePath());
        Scanner scanner = new Scanner(file, StandardCharsets.UTF_8);
        return procesarScanner(scanner, file.getName());
    }

    @Override
    @Transactional
    public MigracionResultadoDto migrarDesdeArchivoSubido(MultipartFile archivo) throws Exception {
        if (archivo == null || archivo.isEmpty()) {
            throw new IllegalArgumentException("El archivo subido está vacío o no se ha proporcionado.");
        }

        log.info("Iniciando migración desde archivo subido: {}", archivo.getOriginalFilename());
        Scanner scanner = new Scanner(archivo.getInputStream(), StandardCharsets.UTF_8);
        return procesarScanner(scanner, archivo.getOriginalFilename());
    }

    //
    private File resolverArchivo(String rutaEspecifica) {
        if (rutaEspecifica != null && !rutaEspecifica.trim().isEmpty()) {
            File f = new File(rutaEspecifica);
            if (f.exists()) {
                return f;
            }
        }

        //si el archivo no estaba en la ruta preconfigurada, prueba ptras posibles ubicaciones
        String[] posiblesRutas = {
                rutaArchivoConfigurada,
                "../migracion.txt",
                "../migración.txt",
                "migracion.txt",
                "migración.txt",
                "./migracion.txt",
                "./migración.txt",
                "src/main/resources/migracion.txt",
                "src/main/resources/migración.txt"
        };

        for (String ruta : posiblesRutas) {
            File f = new File(ruta);
            if (f.exists()) {
                return f;
            }
        }

        // si no se encuentra en las rutas relativas, devolver la ruta configurada por defecto
        return new File(rutaArchivoConfigurada != null ? rutaArchivoConfigurada : "../migracion.txt");
    }

    //separamiento del archivo en lineas con scanner
    //separamiento de lineas en atributos usando StrinTokenizer
    private MigracionResultadoDto procesarScanner(Scanner scanner, String origen) {
        MigracionResultadoDto resultado = new MigracionResultadoDto();
        resultado.setNombreArchivo(origen);

        Localidad localidadDefault = obtenerLocalidadPorDefecto();
        List<RegistroMigradoDto> registros = new ArrayList<>();
        int nroFila = 0;
        int exitosos = 0;
        int fallidos = 0;

        try {
            while (scanner.hasNextLine()) {
                nroFila++;
                String linea = scanner.nextLine().trim();

                // ignorar líneas vacías
                if (linea.isEmpty()) {
                    continue;
                }

                // ignorar cabecera si existe
                if (linea.toUpperCase().startsWith("NOMBRE;")) {
                    continue;
                }

                RegistroMigradoDto reg = new RegistroMigradoDto();
                reg.setNumeroFila(nroFila);
                reg.setLineaOriginal(linea);

                try {
                    // tokenizar por punto y coma (;)
                    StringTokenizer atributo = new StringTokenizer(linea, ";");

                    if (atributo.countTokens() < 5) {
                        throw new IllegalArgumentException("Campos insuficientes (se esperaban 5: NOMBRE;APELLIDO;DNI;CALLE;NÚMERO;)");
                    }

                    String nombre = atributo.nextToken();
                    String apellido = atributo.nextToken();
                    String dniStr = atributo.nextToken();
                    String calle = atributo.nextToken();
                    String numeroStr = atributo.nextToken();

                    reg.setNombre(nombre);
                    reg.setApellido(apellido);

                    int dni;
                    try {
                        dni = Integer.parseInt(dniStr);
                        reg.setDni(dni);
                    } catch (NumberFormatException e) {
                        throw new IllegalArgumentException("DNI inválido: '" + dniStr + "'");
                    }

                    reg.setCalle(calle);

                    int numero;
                    try {
                        numero = Integer.parseInt(numeroStr);
                        reg.setNumero(numero);
                    } catch (NumberFormatException e) {
                        throw new IllegalArgumentException("Número de calle inválido: '" + numeroStr + "'");
                    }

                    // buscar proveedor existente por DNI o crear nuevo
                    Optional<Persona> personaOpt = personaRepository.findByDni(dni);
                    Persona persona;

                    if (personaOpt.isPresent()) {
                        persona = personaOpt.get();
                        persona.setNombre(nombre);
                        persona.setApellido(apellido);
                        if (persona.getActivo() == null) {
                            persona.setActivo(true);
                        }
                        reg.setEstado("ACTUALIZADO");
                        reg.setMensaje("Registro actualizado (DNI ya existente en el sistema).");
                    } else {
                        persona = new Persona();
                        persona.setNombre(nombre);
                        persona.setApellido(apellido);
                        persona.setDni(dni);
                        persona.setActivo(true);
                        persona.setTienePrestamo(false);
                        reg.setEstado("INGRESADO");
                        reg.setMensaje("Registro ingresado correctamente.");
                    }

                    // domicilio
                    Domicilio domicilio = persona.getDomicilio();
                    if (domicilio == null) {
                        domicilio = new Domicilio();
                        domicilio.setActivo(true);
                    }
                    domicilio.setCalle(calle);
                    domicilio.setNumero(numero);
                    if (domicilio.getLocalidad() == null && localidadDefault != null) {
                        domicilio.setLocalidad(localidadDefault);
                    }
                    persona.setDomicilio(domicilio);

                    personaRepository.save(persona);
                    exitosos++;

                } catch (Exception e) {
                    fallidos++;
                    reg.setEstado("ERROR");
                    reg.setMensaje(e.getMessage());
                    log.warn("Fila {}: Error al procesar '{}': {}", nroFila, linea, e.getMessage());
                }

                registros.add(reg);
            }
        } finally {
            scanner.close();
        }

        resultado.setTotalLeidos(registros.size());
        resultado.setTotalExitosos(exitosos);
        resultado.setTotalFallidos(fallidos);
        resultado.setRegistros(registros);
        resultado.setMensaje("Migración completada. Exitosos: " + exitosos + ", Fallidos: " + fallidos);
        return resultado;
    }

    private Localidad obtenerLocalidadPorDefecto() {
        try {
            return localidadRepository.findAll().stream()
                    .filter(l -> l.getActivo() == null || l.getActivo())
                    .findFirst()
                    .orElseGet(() -> {
                        Localidad def = new Localidad();
                        def.setDenominacion("Mendoza");
                        def.setActivo(true);
                        return localidadRepository.save(def);
                    });
        } catch (Exception e) {
            log.warn("No se pudo obtener o crear la localidad por defecto: {}", e.getMessage());
            return null;
        }
    }
}
