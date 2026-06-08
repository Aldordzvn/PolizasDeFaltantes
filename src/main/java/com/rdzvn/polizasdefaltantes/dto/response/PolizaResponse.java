package com.rdzvn.polizasdefaltantes.dto.response;

import com.rdzvn.polizasdefaltantes.entity.Poliza;

import java.time.LocalDateTime;

public record PolizaResponse(
        Long idPoliza,
        Long idEmpleado,
        String sku,
        Integer cantidadFaltante,
        LocalDateTime fechaCreacion
) {
    public static PolizaResponse desde(Poliza poliza){
        return new PolizaResponse(
                poliza.getIdPoliza(),
                poliza.getEmpleado().getIdEmpleado(),
                poliza.getInventario().getSku(),
                poliza.getCantidadFaltante(),
                poliza.getFechaCreacion()
        );
    }
}
