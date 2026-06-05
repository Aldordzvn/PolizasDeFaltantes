package com.rdzvn.polizasdefaltantes.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CrearInventarioRequest(
        @NotBlank(message = "El SKU es necesario")
        @Size(min = 8, max = 50, message = "El SKU debe tener de 8 a 50 caracteres")
        String sku,

        @NotBlank(message = "El nombre es necesario")
        @Size(min = 3, max = 100, message = "El nombre debe tener entre 8 a 100 caracteres")
        String nombre,

        @NotNull(message = "La cantidad es necesaria")
        @Positive(message = "La cantidad no puede ser 0 ni negativa")
        Integer cantidad
) {

}
