package com.rdzvn.polizasdefaltantes.dto.response;

import com.rdzvn.polizasdefaltantes.entity.Poliza;

import java.time.LocalDateTime;

public record PolizaResponse(
        Long idPoliza,
        Long idEmpleado,
        String nombreEmpleado,
        String sku,
        String nombreArticulo,
        Integer cantidadFaltante,
        LocalDateTime fechaCreacion
) {
    public static PolizaResponse desde(Poliza poliza){
        return new PolizaResponse(
                poliza.getIdPoliza(),
                poliza.getEmpleado().getIdEmpleado(),
                poliza.getEmpleado().getNombre()+ " " + poliza.getEmpleado().getApellido(),
                poliza.getInventario().getSku(),
                poliza.getInventario().getNombre(),
                poliza.getCantidadFaltante(),
                poliza.getFechaCreacion()
        );
    }
}
