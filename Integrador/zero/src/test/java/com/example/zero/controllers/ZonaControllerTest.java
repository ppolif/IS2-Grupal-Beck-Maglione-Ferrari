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
import org.springframework.ui.Model;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ZonaControllerTest {

    @Mock
    private ZonaService zonaService;

    @Mock
    private Model model;

    @InjectMocks
    private ZonaController controller;

    @Test
    void getPaises_retornaFragmentoYAgregaPaisesAlModelo() {
        List<PaisDTO> paises = List.of(new PaisDTO("p1", "Argentina"));
        when(zonaService.listarPaisesActivos()).thenReturn(paises);

        String view = controller.getPaises(model);

        assertEquals("shop/fragments/zonas :: opcionesPaises", view);
        verify(model).addAttribute("paises", paises);
    }

    @Test
    void getProvincias_conPaisValido_retornaFragmentoYAgregaProvinciasAlModelo() {
        List<ProvinciaDTO> provincias = List.of(new ProvinciaDTO("pr1", "Córdoba", "p1", "Argentina"));
        when(zonaService.listarProvinciasPorPais("p1")).thenReturn(provincias);

        String view = controller.getProvincias("p1", model);

        assertEquals("shop/fragments/zonas :: opcionesProvincias", view);
        verify(model).addAttribute("provincias", provincias);
    }

    @Test
    void getProvincias_conPaisNulo_retornaFragmentoYListaVacia() {
        String view = controller.getProvincias(null, model);

        assertEquals("shop/fragments/zonas :: opcionesProvincias", view);
        verify(model).addAttribute("provincias", List.of());
    }

    @Test
    void getDepartamentos_conProvinciaValida_retornaFragmentoYAgregaDepartamentosAlModelo() {
        List<DepartamentoDTO> departamentos = List.of(new DepartamentoDTO("d1", "Capital", "pr1", "Córdoba"));
        when(zonaService.listarDepartamentosPorProvincia("pr1")).thenReturn(departamentos);

        String view = controller.getDepartamentos("pr1", model);

        assertEquals("shop/fragments/zonas :: opcionesDepartamentos", view);
        verify(model).addAttribute("departamentos", departamentos);
    }

    @Test
    void getDepartamentos_conProvinciaVacia_retornaFragmentoYListaVacia() {
        String view = controller.getDepartamentos("  ", model);

        assertEquals("shop/fragments/zonas :: opcionesDepartamentos", view);
        verify(model).addAttribute("departamentos", List.of());
    }

    @Test
    void getLocalidades_conDepartamentoValido_retornaFragmentoYAgregaLocalidadesAlModelo() {
        List<LocalidadDTO> localidades = List.of(new LocalidadDTO("loc1", "Córdoba", "5000", "d1", "Capital"));
        when(zonaService.listarLocalidadesPorDepartamento("d1")).thenReturn(localidades);

        String view = controller.getLocalidades("d1", model);

        assertEquals("shop/fragments/zonas :: opcionesLocalidades", view);
        verify(model).addAttribute("localidades", localidades);
    }

    @Test
    void getLocalidades_conDepartamentoNulo_retornaFragmentoYListaVacia() {
        String view = controller.getLocalidades(null, model);

        assertEquals("shop/fragments/zonas :: opcionesLocalidades", view);
        verify(model).addAttribute("localidades", List.of());
    }
}
