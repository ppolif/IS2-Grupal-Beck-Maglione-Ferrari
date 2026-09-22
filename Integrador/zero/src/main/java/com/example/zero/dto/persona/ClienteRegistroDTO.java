package com.example.zero.dto.persona;

import com.example.zero.enums.TipoDocumento;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClienteRegistroDTO {

    // Credenciales de Usuario
    private String email;
    private String password;
    private String confirmPassword;

    // Datos de Persona
    private String nombre;
    private String apellido;
    private TipoDocumento tipoDocumento;
    private String numeroDocumento;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fechaNacimiento;

    // Datos de Cliente
    private String nacionalidadId;

    // Datos de Contacto
    private String tipoContacto; // "EMAIL" o "CELULAR"
    private String contactoEmail;
    private String contactoTelefono;
    private String contactoObservacion;

    // Datos de Domicilio / Dirección
    private String calle;
    private String numeracion;
    private String barrio;
    private String manzanaPiso;
    private String casaDepartamento;
    private String referencia;

    // Jerarquía Geográfica
    private String paisId;
    private String provinciaId;
    private String departamentoId;
    private String localidadId;
}

