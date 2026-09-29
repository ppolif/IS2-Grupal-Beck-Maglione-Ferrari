package com.example.zero.controllers;

import com.example.zero.dto.zona.DepartamentoDTO;
import com.example.zero.dto.zona.LocalidadDTO;
import com.example.zero.dto.zona.PaisDTO;
import com.example.zero.dto.zona.ProvinciaDTO;
import com.example.zero.services.zona.ZonaService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@RequestMapping("/zonas")
public class ZonaController {

    private final ZonaService zonaService;

    public ZonaController(ZonaService zonaService) {
        this.zonaService = zonaService;
    }

    @GetMapping("/paises")
    @ResponseBody
    public List<PaisDTO> getPaises() {
        if (zonaService == null) {
            return List.of();
        }
        return zonaService.listarPaisesActivos();
    }

    @GetMapping("/provincias")
    @ResponseBody
    public List<ProvinciaDTO> getProvincias(@RequestParam(value = "paisId", required = false) String paisId) {
        if (zonaService == null || paisId == null || paisId.isBlank()) {
            return List.of();
        }
        return zonaService.listarProvinciasPorPais(paisId.trim());
    }

    @GetMapping("/departamentos")
    @ResponseBody
    public List<DepartamentoDTO> getDepartamentos(@RequestParam(value = "provinciaId", required = false) String provinciaId) {
        if (zonaService == null || provinciaId == null || provinciaId.isBlank()) {
            return List.of();
        }
        return zonaService.listarDepartamentosPorProvincia(provinciaId.trim());
    }

    @GetMapping("/localidades")
    @ResponseBody
    public List<LocalidadDTO> getLocalidades(@RequestParam(value = "departamentoId", required = false) String departamentoId) {
        if (zonaService == null || departamentoId == null || departamentoId.isBlank()) {
            return List.of();
        }
        return zonaService.listarLocalidadesPorDepartamento(departamentoId.trim());
    }
}
