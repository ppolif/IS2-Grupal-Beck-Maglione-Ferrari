package com.example.zero.controllers;

import com.example.zero.entidades.compraProveedor.Proveedor;
import com.example.zero.services.ProveedorService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador administrativo para la gestión de Proveedores (ABM).
 */
@Controller
@RequestMapping("/admin/providers")
public class AdminProviderController {

    private final ProveedorService proveedorService;

    public AdminProviderController(ProveedorService proveedorService) {
        this.proveedorService = proveedorService;
    }

    /**
     * Listado de proveedores activos.
     */
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

    /**
     * Formulario para registrar un nuevo proveedor.
     */
    @GetMapping("/nuevo")
    public String showCreateForm(Model model) {
        model.addAttribute("proveedor", new Proveedor());
        model.addAttribute("isEdit", false);
        return "admin/provider-form";
    }

    /**
     * Procesar el alta de un nuevo proveedor.
     */
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

    /**
     * Formulario para editar un proveedor existente.
     */
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

    /**
     * Procesar la modificación de un proveedor existente.
     */
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

    /**
     * Baja lógica de un proveedor (POST).
     */
    @PostMapping("/eliminar/{id}")
    public String deleteProviderPost(@PathVariable("id") String id) {
        proveedorService.eliminarProveedor(id);
        return "redirect:/admin/providers?success=deleted";
    }

    /**
     * Baja lógica de un proveedor (GET para enlaces rápidos).
     */
    @GetMapping("/eliminar/{id}")
    public String deleteProviderGet(@PathVariable("id") String id) {
        proveedorService.eliminarProveedor(id);
        return "redirect:/admin/providers?success=deleted";
    }
}

