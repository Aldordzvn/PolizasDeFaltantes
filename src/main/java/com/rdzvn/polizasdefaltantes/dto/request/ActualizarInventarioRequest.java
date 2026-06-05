package com.rdzvn.polizasdefaltantes.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record ActualizarInventarioRequest(

        @NotBlank(message = "El nombre es necesario")
        @Size(min = 3, max = 100, message = "El nombre debe contener entre 3 y 100 caracteres")
        String nombre,

        @NotNull(message = "La cantidad es necesaria")
        @Positive(message = "La cantidad no puede ser 0 ni negativa")
        int cantidad
) {

}
