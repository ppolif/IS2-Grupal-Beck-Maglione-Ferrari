package com.example.persona.mappers;

import com.example.persona.dtos.*;
import com.example.persona.entities.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
class MapStructMappersTest {

    @Autowired
    private AutorMapper autorMapper;

    @Autowired
    private LocalidadMapper localidadMapper;

    @Autowired
    private DomicilioMapper domicilioMapper;

    @Autowired
    private LibroMapper libroMapper;

    @Autowired
    private LibroFisicoMapper libroFisicoMapper;

    @Autowired
    private LibroDigitalMapper libroDigitalMapper;

    @Autowired
    private PersonaMapper personaMapper;

    @Test
    @DisplayName("AutorMapper debe transformar Autor a AutorDto y viceversa")
    void testAutorMapper() {
        Autor autor = new Autor("Ernesto", "Sábato", "Físico y escritor");
        autor.setId(5L);

        AutorDto dto = autorMapper.toDto(autor);
        assertNotNull(dto);
        assertEquals(5L, dto.getId());
        assertEquals("Ernesto", dto.getNombre());
        assertEquals("Sábato", dto.getApellido());
        assertEquals("Físico y escritor", dto.getBiografia());

        Autor entity = autorMapper.toEntity(dto);
        assertNotNull(entity);
        assertEquals(dto.getId(), entity.getId());
        assertEquals(dto.getNombre(), entity.getNombre());
        assertEquals(dto.getApellido(), entity.getApellido());
        assertEquals(dto.getBiografia(), entity.getBiografia());
    }

    @Test
    @DisplayName("LocalidadMapper debe transformar Localidad a LocalidadDto y viceversa")
    void testLocalidadMapper() {
        Localidad loc = new Localidad("Mendoza Capital");
        loc.setId(10L);

        LocalidadDto dto = localidadMapper.toDto(loc);
        assertNotNull(dto);
        assertEquals(10L, dto.getId());
        assertEquals("Mendoza Capital", dto.getDenominacion());

        Localidad entity = localidadMapper.toEntity(dto);
        assertNotNull(entity);
        assertEquals(dto.getId(), entity.getId());
        assertEquals(dto.getDenominacion(), entity.getDenominacion());
    }

    @Test
    @DisplayName("DomicilioMapper debe transformar Domicilio a DomicilioDto con su Localidad anidada")
    void testDomicilioMapper() {
        Localidad loc = new Localidad("Godoy Cruz");
        loc.setId(20L);

        Domicilio dom = new Domicilio("San Martín", 1234, "-32.89", "-68.84", loc);
        dom.setId(15L);

        DomicilioDto dto = domicilioMapper.toDto(dom);
        assertNotNull(dto);
        assertEquals(15L, dto.getId());
        assertEquals("San Martín", dto.getCalle());
        assertEquals(1234, dto.getNumero());
        assertNotNull(dto.getLocalidad());
        assertEquals("Godoy Cruz", dto.getLocalidad().getDenominacion());

        Domicilio entity = domicilioMapper.toEntity(dto);
        assertNotNull(entity);
        assertEquals(dto.getCalle(), entity.getCalle());
        assertEquals(dto.getNumero(), entity.getNumero());
        assertNotNull(entity.getLocalidad());
        assertEquals("Godoy Cruz", entity.getLocalidad().getDenominacion());
    }

    @Test
    @DisplayName("LibroMapper debe transformar Libro con sus autores anidados")
    void testLibroMapper() {
        Autor autor = new Autor("Julio", "Cortázar", "Escritor");
        autor.setId(1L);

        Libro libro = new Libro();
        libro.setId(100L);
        libro.setTitulo("Final del juego");
        libro.setFecha(1956);
        libro.setGenero("Cuentos");
        libro.setPaginas(180);
        libro.setFechaVencimientoDevolucion(LocalDate.of(2026, 11, 20));
        libro.setAutores(List.of(autor));

        LibroDto dto = libroMapper.toDto(libro);
        assertNotNull(dto);
        assertEquals(100L, dto.getId());
        assertEquals("Final del juego", dto.getTitulo());
        assertEquals(1, dto.getAutores().size());
        assertEquals("Julio", dto.getAutores().get(0).getNombre());

        Libro entity = libroMapper.toEntity(dto);
        assertNotNull(entity);
        assertEquals(dto.getTitulo(), entity.getTitulo());
        assertEquals(1, entity.getAutores().size());
    }

    @Test
    @DisplayName("LibroFisicoMapper y LibroDigitalMapper deben mapear atributos especializados")
    void testLibrosEspecializadosMappers() {
        LibroFisico fisico = new LibroFisico();
        fisico.setId(201L);
        fisico.setTitulo("Libro Impreso");
        fisico.setNumeroEstante("B-04");
        fisico.setPesoGramos(600);

        LibroFisicoDto fisicoDto = libroFisicoMapper.toDto(fisico);
        assertNotNull(fisicoDto);
        assertEquals("B-04", fisicoDto.getNumeroEstante());
        assertEquals(600, fisicoDto.getPesoGramos());

        LibroDigital digital = new LibroDigital();
        digital.setId(202L);
        digital.setTitulo("Libro PDF");
        digital.setTamanoMB(12.4);
        digital.setFormato("PDF");
        digital.setUrlDescarga("https://link.com/libro.pdf");

        LibroDigitalDto digitalDto = libroDigitalMapper.toDto(digital);
        assertNotNull(digitalDto);
        assertEquals(12.4, digitalDto.getTamanoMB());
        assertEquals("PDF", digitalDto.getFormato());
        assertEquals("https://link.com/libro.pdf", digitalDto.getUrlDescarga());
    }

    @Test
    @DisplayName("PersonaMapper debe mapear entidad completa con domicilio y libros")
    void testPersonaMapper() {
        Localidad loc = new Localidad("Luján de Cuyo");
        Domicilio dom = new Domicilio("Italia", 450, "-33.00", "-68.87", loc);

        Libro libro = new Libro();
        libro.setTitulo("Libro Persona");
        libro.setFecha(2020);

        Persona persona = new Persona();
        persona.setId(1L);
        persona.setNombre("Carlos");
        persona.setApellido("Pérez");
        persona.setDni(12345678);
        persona.setEmail("carlos@example.com");
        persona.setFechaNacimiento(LocalDate.of(1995, 5, 10));
        persona.setDomicilio(dom);
        persona.setLibros(List.of(libro));

        PersonaDto dto = personaMapper.toDto(persona);
        assertNotNull(dto);
        assertEquals("Carlos", dto.getNombre());
        assertEquals("carlos@example.com", dto.getEmail());
        assertNotNull(dto.getDomicilio());
        assertEquals("Italia", dto.getDomicilio().getCalle());
        assertEquals("Luján de Cuyo", dto.getDomicilio().getLocalidad().getDenominacion());
        assertEquals(1, dto.getLibros().size());
        assertEquals("Libro Persona", dto.getLibros().get(0).getTitulo());

        Persona entity = personaMapper.toEntity(dto);
        assertNotNull(entity);
        assertEquals(dto.getNombre(), entity.getNombre());
        assertEquals(dto.getEmail(), entity.getEmail());
        assertEquals(1, entity.getLibros().size());
    }
}

