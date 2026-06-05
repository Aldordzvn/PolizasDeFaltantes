package com.rdzvn.polizasdefaltantes.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CrearPolizaRequest(

        @NotNull(message = "El ID del empleado es necesario")
        @Positive(message = "El ID no puede ser negativo ni 0")
        Long idEmpleado,

        @NotBlank(message = "El SKU es necesario")
        @Size(min = 3, max = 50, message = "El SKU debe tener entre 3 y 50 caracteres")
        String sku,

        @NotNull(message = "La cantidad es necesaria")
        @Positive(message = "La cantidad no puede ser 0 ni negativa")
        Integer cantidadFaltante
) {

}
