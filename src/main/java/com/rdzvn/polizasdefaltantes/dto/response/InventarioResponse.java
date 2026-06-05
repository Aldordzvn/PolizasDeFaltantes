package com.rdzvn.polizasdefaltantes.dto.response;

import com.rdzvn.polizasdefaltantes.entity.Inventario;

import java.time.LocalDateTime;

public record InventarioResponse(
        String sku,
        String nombre,
        int cantidad,
        LocalDateTime creadoEn
) {
    public static InventarioResponse desde(Inventario inventario){
        return new InventarioResponse(
                inventario.getSku(),
                inventario.getNombre(),
                inventario.getCantidad(),
                inventario.getCreadoEn()
        );
    }
}
