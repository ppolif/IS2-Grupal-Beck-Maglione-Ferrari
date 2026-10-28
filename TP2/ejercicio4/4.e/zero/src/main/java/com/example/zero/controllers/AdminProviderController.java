package com.example.zero.controllers;

import com.example.zero.entidades.compraProveedor.Proveedor;
import com.example.zero.services.ProveedorService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

//para el abm de proveedor
@Controller
@RequestMapping("/admin/providers")
public class AdminProviderController {

    private final ProveedorService proveedorService;

    public AdminProviderController(ProveedorService proveedorService) {
        this.proveedorService = proveedorService;
    }


    @GetMapping
    public String listProviders(Model model,
                                @RequestParam(name = "success", required = false) String success,
                                @RequestParam(name = "error", required = false) String error) {
        List<Proveedor> proveedores = proveedorService.listarActivos();
        model.addAttribute("providers", proveedores);

        if ("created".equals(success)) {
            model.addAttribute("successMessage", "Proveedor registrado exitosamente.");
        } else if ("updated".equals(success)) {
            model.addAttribute("successMessage", "Proveedor modificado exitosamente.");
        } else if ("deleted".equals(success)) {
            model.addAttribute("successMessage", "Proveedor dado de baja exitosamente.");
        }

        if (error != null) {
            model.addAttribute("errorMessage", error);
        }

        return "admin/providers";
    }

    ///form de registro
    @GetMapping("/nuevo")
    public String showCreateForm(Model model) {
        model.addAttribute("proveedor", new Proveedor());
        model.addAttribute("isEdit", false);
        return "admin/provider-form";
    }

    ///procesar el alta
    @PostMapping("/guardar")
    public String createProvider(@RequestParam("cuit") String cuit,
                                 @RequestParam("razonSocial") String razonSocial,
                                 Model model) {
        try {
            proveedorService.crearProveedor(razonSocial, cuit);
            return "redirect:/admin/providers?success=created";
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("isEdit", false);
            model.addAttribute("cuit", cuit);
            model.addAttribute("razonSocial", razonSocial);
            return "admin/provider-form";
        }
    }

    //ormulario para editar un proveedor
    @GetMapping("/editar/{id}")
    public String showEditForm(@PathVariable("id") String id, Model model) {
        try {
            Proveedor proveedor = proveedorService.buscarPorId(id);
            model.addAttribute("proveedor", proveedor);
            model.addAttribute("isEdit", true);
            return "admin/provider-form";
        } catch (IllegalArgumentException e) {
            return "redirect:/admin/providers?error=" + e.getMessage();
        }
    }

    ///procesar la modificación
    @PostMapping("/editar/{id}")
    public String updateProvider(@PathVariable("id") String id,
                                 @RequestParam("cuit") String cuit,
                                 @RequestParam("razonSocial") String razonSocial,
                                 Model model) {
        try {
            proveedorService.modificarProveedor(id, razonSocial, cuit);
            return "redirect:/admin/providers?success=updated";
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("isEdit", true);
            Proveedor p = Proveedor.builder().id(id).cuit(cuit).razonSocial(razonSocial).build();
            model.addAttribute("proveedor", p);
            return "admin/provider-form";
        }
    }


    @PostMapping("/eliminar/{id}")
    public String deleteProviderPost(@PathVariable("id") String id) {
        proveedorService.eliminarProveedor(id);
        return "redirect:/admin/providers?success=deleted";
    }


    @GetMapping("/eliminar/{id}")
    public String deleteProviderGet(@PathVariable("id") String id) {
        proveedorService.eliminarProveedor(id);
        return "redirect:/admin/providers?success=deleted";
    }
}

