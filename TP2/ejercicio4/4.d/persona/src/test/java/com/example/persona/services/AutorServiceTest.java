package com.example.persona.services;

import com.example.persona.dtos.AutorDto;
import com.example.persona.entities.Autor;
import com.example.persona.mappers.AutorMapper;
import com.example.persona.repositories.AutorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AutorServiceTest {

    private AutorRepository autorRepository;
    private AutorMapper autorMapper;
    private AutorServiceImpl autorService;

    @BeforeEach
    void setUp() {
        autorRepository = Mockito.mock(AutorRepository.class);
        autorMapper = Mockito.mock(AutorMapper.class);
        autorService = new AutorServiceImpl(autorRepository, autorMapper);
    }

    @Test
    @DisplayName("clonar() debe clonar el autor usando Prototype y persistirlo con sufijo ' - Copia'")
    void testClonarAutorEnServicio() throws Exception {
        Autor autorOriginal = new Autor("Julio", "Cortázar", "Biografía original");
        autorOriginal.setId(10L);

        when(autorRepository.findById(10L)).thenReturn(Optional.of(autorOriginal));
        when(autorRepository.save(any(Autor.class))).thenAnswer(invocation -> {
            Autor a = invocation.getArgument(0);
            a.setId(99L);
            return a;
        });

        AutorDto dto = new AutorDto();
        dto.setId(99L);
        dto.setNombre("Julio - Copia");
        dto.setApellido("Cortázar");
        when(autorMapper.toDto(any(Autor.class))).thenReturn(dto);

        AutorDto clonDto = autorService.clonar(10L);

        assertNotNull(clonDto);
        assertEquals(99L, clonDto.getId());
        org.mockito.ArgumentCaptor<Autor> captor = org.mockito.ArgumentCaptor.forClass(Autor.class);
        verify(autorRepository, times(1)).save(captor.capture());
        Autor saved = captor.getValue();
        assertEquals("Julio - Copia", saved.getNombre());
        assertEquals("Cortázar", saved.getApellido());
    }
}
