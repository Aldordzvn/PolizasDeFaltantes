package com.rdzvn.polizasdefaltantes.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record ActualizarEmpleadoRequest(
        @NotBlank(message = "El nombre es necesario")
        @Size(min = 3, max = 100, message = "El nombre debe tener entre 3 y 100 caracteres")
        String nombre,

        @NotBlank(message = "El apellido es necesario")
        @Size(min = 3, max = 100, message = "El apellido debe tener entre 3 y 100 caracteres")
        String apellido
) {
}
