package com.rdzvn.polizasdefaltantes.controller;

import com.rdzvn.polizasdefaltantes.dto.request.ActualizarInventarioRequest;
import com.rdzvn.polizasdefaltantes.dto.request.CrearInventarioRequest;
import com.rdzvn.polizasdefaltantes.dto.response.ApiResponse;
import com.rdzvn.polizasdefaltantes.dto.response.InventarioResponse;
import com.rdzvn.polizasdefaltantes.service.InventarioService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("api/v1/inventario")
public class InventarioController {

    private final InventarioService inventarioService;

    public InventarioController(InventarioService inventarioService) {
        this.inventarioService = inventarioService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<InventarioResponse>>> listar(){
        List<InventarioResponse> inventarios = inventarioService.listarDesdeResponse();
        return ResponseEntity.ok(
                ApiResponse.ok(inventarios, "Se encontraron " + inventarios.size() + " Inventarios")
        );
    }

    @PostMapping
    public ResponseEntity<ApiResponse<InventarioResponse>> crearInventario(@RequestBody @Valid CrearInventarioRequest request){
        InventarioResponse creado = inventarioService.crearInventarioDesdeRequest(request);
        URI location = URI.create("/api/v1/inventarios/" + creado.sku());
        return ResponseEntity
                .created(location)
                .body(ApiResponse.ok(creado, "Inventario creado exitosamente"));
    }

    @PutMapping("/{sku}")
    public ResponseEntity<ApiResponse<InventarioResponse>> actualizarEmpleado(@PathVariable String sku, @RequestBody @Valid ActualizarInventarioRequest request){
        InventarioResponse actualizado = inventarioService.actualizarInventarioDeseRequest(sku, request);
        return ResponseEntity.ok(
                ApiResponse.ok(actualizado, "Inventario actualizado correctamente")
        );
    }

    @DeleteMapping("/{sku}")
    public ResponseEntity<ApiResponse<Void>> eliminar(@PathVariable String sku){
        inventarioService.eliminarInventario(sku);
        return ResponseEntity.ok(
                ApiResponse.ok(null, "Inventario eliminado correctamente")
        );
    }
}
