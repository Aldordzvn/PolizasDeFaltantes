package com.rdzvn.polizasdefaltantes.controller;

import com.rdzvn.polizasdefaltantes.dto.request.ActualizarEmpleadoRequest;
import com.rdzvn.polizasdefaltantes.dto.request.CrearEmpleadoRequest;
import com.rdzvn.polizasdefaltantes.dto.response.ApiResponse;
import com.rdzvn.polizasdefaltantes.dto.response.EmpleadoResponse;
import com.rdzvn.polizasdefaltantes.service.EmpleadoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/empleados")
public class EmpleadoController {

    private final EmpleadoService empleadoService;

    public EmpleadoController(EmpleadoService empleadoService) {
        this.empleadoService = empleadoService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<EmpleadoResponse>>> listar(){
        List<EmpleadoResponse> empleados = empleadoService.listarComoResponse();
        return ResponseEntity.ok(
                ApiResponse.ok(empleados, "Se encontraron " + empleados.size() + " empleados")
        );
    }

    @PostMapping
    public ResponseEntity<ApiResponse<EmpleadoResponse>> crearEmpleado(@RequestBody @Valid CrearEmpleadoRequest request){
        EmpleadoResponse creado = empleadoService.crearDesdeRequest(request);
        URI location = URI.create("/api/v1/empleados/" + creado.idEmpleado());
        return ResponseEntity
                .created(location)
                .body(ApiResponse.ok(creado, "Empleado creado exitosamente"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<EmpleadoResponse>> actualizarEmpleado(@PathVariable Long id, @RequestBody @Valid ActualizarEmpleadoRequest request){
        EmpleadoResponse actualizado = empleadoService.actualizarEmpleadoDesdeRequest(id, request);
        return ResponseEntity.ok(
                ApiResponse.ok(actualizado, "Empleado Actualizado correctamente")
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> eliminar(@PathVariable Long id){
        empleadoService.eliminarEmpleado(id);
        return ResponseEntity.ok(
                ApiResponse.ok(null, "Producto eliminado correctamente")
        );
    }



}
