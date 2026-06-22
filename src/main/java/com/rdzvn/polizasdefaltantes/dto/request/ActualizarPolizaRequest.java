package com.rdzvn.polizasdefaltantes.dto.request;

import jakarta.validation.constraints.*;

public record ActualizarPolizaRequest(

        @NotNull(message = "El ID del empleado es necesario")
        @Positive(message = "El ID no puede ser 0 ni negativo")
        Long idEmpleado,

        @NotBlank(message = "El SKU es necesario")
        @Size(min = 3, max = 50, message = "El SKU debe contener entre 3 y 50 caracteres")
        String sku,

        @NotNull(message = "La cantidad faltante es necesaria")
        @Positive(message = "La cantidad faltante no puede ser 0 ni negativa")
        Integer cantidadFaltante
) {
}
