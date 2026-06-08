package com.rdzvn.polizasdefaltantes.controller;

import com.rdzvn.polizasdefaltantes.dto.request.ActualizarPolizaRequest;
import com.rdzvn.polizasdefaltantes.dto.request.CrearPolizaRequest;
import com.rdzvn.polizasdefaltantes.dto.response.ApiResponse;
import com.rdzvn.polizasdefaltantes.dto.response.PolizaResponse;
import com.rdzvn.polizasdefaltantes.service.PolizaService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/polizas")
public class PolizaController {

    private final PolizaService polizaService;

    public PolizaController(PolizaService polizaService) {
        this.polizaService = polizaService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<PolizaResponse>>> listarPolizas(){
        List<PolizaResponse> polizas = polizaService.listarPolizas();
        return ResponseEntity.ok(
                ApiResponse.ok(polizas, "Se encontraron " + polizas.size() + " polizas")
        );
    }

    @PostMapping
    public ResponseEntity<ApiResponse<PolizaResponse>> crearPoliza(@RequestBody @Valid CrearPolizaRequest request){
        PolizaResponse creado = polizaService.crearPolizaDesdeRequest(request);
        URI location = URI.create("/api/v1/polizas/"+creado.idPoliza());
        return ResponseEntity
                .created(location)
                .body(ApiResponse.ok(creado, "Poliza creada exitosamente"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PolizaResponse>> actualizarPoliza(@PathVariable Long id, @RequestBody @Valid ActualizarPolizaRequest request){
        PolizaResponse actualizado = polizaService.actualizarPolizaDesdeRequest(id, request);
        return ResponseEntity.ok(
                ApiResponse.ok(actualizado, "Poliza actualizada correctamente")
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> eliminar(@PathVariable Long id){
        polizaService.eliminarPoliza(id);
        return ResponseEntity.ok(
                ApiResponse.ok(null, "Poliza eliminada correctamente")
        );
    }
}
