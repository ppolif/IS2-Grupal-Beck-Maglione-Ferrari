package com.example.zero.controllers;

import com.example.zero.dto.zona.DepartamentoDTO;
import com.example.zero.dto.zona.LocalidadDTO;
import com.example.zero.dto.zona.PaisDTO;
import com.example.zero.dto.zona.ProvinciaDTO;
import com.example.zero.services.zona.ZonaService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ZonaControllerTest {

    @Mock
    private ZonaService zonaService;

    @InjectMocks
    private ZonaController controller;

    @Test
    void getPaises_retornaListaDePaises() {
        when(zonaService.listarPaisesActivos()).thenReturn(List.of(new PaisDTO("p1", "Argentina")));

        List<PaisDTO> response = controller.getPaises();

        assertNotNull(response);
        assertEquals(1, response.size());
        assertEquals("Argentina", response.get(0).getNombre());
    }

    @Test
    void getProvincias_retornaListaDeProvincias() {
        when(zonaService.listarProvinciasPorPais("p1")).thenReturn(List.of(new ProvinciaDTO("pr1", "Córdoba", "p1", "Argentina")));

        List<ProvinciaDTO> response = controller.getProvincias("p1");

        assertNotNull(response);
        assertEquals(1, response.size());
        assertEquals("Córdoba", response.get(0).getNombre());
    }

    @Test
    void getDepartamentos_retornaListaDeDepartamentos() {
        when(zonaService.listarDepartamentosPorProvincia("pr1")).thenReturn(List.of(new DepartamentoDTO("d1", "Capital", "pr1", "Córdoba")));

        List<DepartamentoDTO> response = controller.getDepartamentos("pr1");

        assertNotNull(response);
        assertEquals(1, response.size());
        assertEquals("Capital", response.get(0).getNombre());
    }

    @Test
    void getLocalidades_retornaListaDeLocalidades() {
        when(zonaService.listarLocalidadesPorDepartamento("d1")).thenReturn(List.of(new LocalidadDTO("loc1", "Córdoba", "5000", "d1", "Capital")));

        List<LocalidadDTO> response = controller.getLocalidades("d1");

        assertNotNull(response);
        assertEquals(1, response.size());
        assertEquals("Córdoba", response.get(0).getNombre());
    }
}
