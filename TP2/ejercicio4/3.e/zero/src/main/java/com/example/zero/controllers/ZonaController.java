package com.example.zero.controllers;

import com.example.zero.dto.zona.DepartamentoDTO;
import com.example.zero.dto.zona.LocalidadDTO;
import com.example.zero.dto.zona.PaisDTO;
import com.example.zero.dto.zona.ProvinciaDTO;
import com.example.zero.services.zona.ZonaService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/zonas")
public class ZonaController {

    private final ZonaService zonaService;

    public ZonaController(ZonaService zonaService) {
        this.zonaService = zonaService;
    }

    @GetMapping("/paises")
    public String getPaises(Model model) {
        List<PaisDTO> paises = (zonaService != null) ? zonaService.listarPaisesActivos() : List.of();
        model.addAttribute("paises", paises);
        return "shop/fragments/zonas :: opcionesPaises";
    }

    @GetMapping("/provincias")
    public String getProvincias(@RequestParam(value = "paisId", required = false) String paisId, Model model) {
        List<ProvinciaDTO> provincias = (zonaService != null && paisId != null && !paisId.isBlank())
                ? zonaService.listarProvinciasPorPais(paisId.trim())
                : List.of();
        model.addAttribute("provincias", provincias);
        return "shop/fragments/zonas :: opcionesProvincias";
    }

    @GetMapping("/departamentos")
    public String getDepartamentos(@RequestParam(value = "provinciaId", required = false) String provinciaId, Model model) {
        List<DepartamentoDTO> departamentos = (zonaService != null && provinciaId != null && !provinciaId.isBlank())
                ? zonaService.listarDepartamentosPorProvincia(provinciaId.trim())
                : List.of();
        model.addAttribute("departamentos", departamentos);
        return "shop/fragments/zonas :: opcionesDepartamentos";
    }

    @GetMapping("/localidades")
    public String getLocalidades(@RequestParam(value = "departamentoId", required = false) String departamentoId, Model model) {
        List<LocalidadDTO> localidades = (zonaService != null && departamentoId != null && !departamentoId.isBlank())
                ? zonaService.listarLocalidadesPorDepartamento(departamentoId.trim())
                : List.of();
        model.addAttribute("localidades", localidades);
        return "shop/fragments/zonas :: opcionesLocalidades";
    }
}
