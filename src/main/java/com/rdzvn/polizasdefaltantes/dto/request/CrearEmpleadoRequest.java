package com.rdzvn.polizasdefaltantes.dto.request;

import com.rdzvn.polizasdefaltantes.entity.Roles;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CrearEmpleadoRequest(
        @NotBlank(message = "El nombre es necesario")
        @Size(min = 3, max = 100, message = "El nombre debe tener entre 3 y 100 caracteres")
        String nombre,

        @NotBlank(message = "El apellido es necesario")
        @Size(min = 3, max = 100, message = "El apellido debe de tener entre 3 y 100 caracteres")
        String apellido,

        @NotNull(message = "El puesto es necesario")
        Roles puesto
) {
}
