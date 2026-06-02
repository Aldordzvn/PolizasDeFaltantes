package com.rdzvn.polizasdefaltantes.dto.response;

import com.rdzvn.polizasdefaltantes.entity.Empleado;
import com.rdzvn.polizasdefaltantes.entity.Roles;

import java.time.LocalDateTime;

public record EmpleadoResponse(
        Long idEmpleado,
        String nombre,
        String apellido,
        Roles puesto,
        LocalDateTime creadoEn
) {

    public static EmpleadoResponse desde(Empleado e){
        return new EmpleadoResponse(
                e.getIdEmpleado(),
                e.getNombre(),
                e.getApellido(),
                e.getPuesto(),
                e.getCreadoEn()
        );
    }
}
