package com.rdzvn.polizasdefaltantes.dto.response;

import com.rdzvn.polizasdefaltantes.entity.Poliza;

public record PolizaResponse(
        Long idPoliza,
        Long idEmpleado,
        String sku,
        Integer cantidadFaltante
) {
    public static PolizaResponse desde(Poliza poliza){
        return new PolizaResponse(
                poliza.getIdPoliza(),
                poliza.getEmpleado().getIdEmpleado(),
                poliza.getInventario().getSku(),
                poliza.getCantidadFaltante()
        );
    }
}
