package com.rdzvn.polizasdefaltantes.service;

import com.rdzvn.polizasdefaltantes.dto.request.ActualizarPolizaRequest;
import com.rdzvn.polizasdefaltantes.dto.request.CrearPolizaRequest;
import com.rdzvn.polizasdefaltantes.dto.response.PolizaResponse;
import com.rdzvn.polizasdefaltantes.entity.Empleado;
import com.rdzvn.polizasdefaltantes.entity.Inventario;
import com.rdzvn.polizasdefaltantes.entity.Poliza;
import com.rdzvn.polizasdefaltantes.entity.Roles;
import com.rdzvn.polizasdefaltantes.excepciones.BusinessException;
import com.rdzvn.polizasdefaltantes.excepciones.ResourceNotFoundException;
import com.rdzvn.polizasdefaltantes.repository.PolizaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PolizaServiceTest {
    // Ahora mockeamos los Services, no los Repositories
    @Mock
    private PolizaRepository polizaRepository;

    @Mock
    private InventarioService inventarioService;

    @Mock
    private EmpleadoService empleadoService;

    @InjectMocks
    private PolizaService polizaService;

    private Empleado empleado;
    private Inventario inventario;
    private Poliza poliza;

    @BeforeEach
    void setUp() {
        empleado = new Empleado("Juan", "Pérez", Roles.ADMIN);
        inventario = new Inventario("SKU-001", "Artículo Test", 20);
        poliza = new Poliza(empleado, inventario, 5);
    }

    // ─────────────────────────────────────────────
    // CREAR PÓLIZA
    // ─────────────────────────────────────────────

    @Test
    void debeCrearPoliza_cuandoInventarioEsSuficiente() {
        // ARRANGE
        CrearPolizaRequest request = new CrearPolizaRequest(1L, "SKU-001", 5);

        when(inventarioService.buscarPorSku("SKU-001")).thenReturn(inventario);
        when(empleadoService.buscarPorId(1L)).thenReturn(empleado);
        // descontarCantidad no devuelve nada (void) — no necesita when()
        // pero sí verificamos que se llamó
        when(polizaRepository.save(any(Poliza.class))).thenReturn(poliza);

        // ACT
        PolizaResponse resultado = polizaService.crearPolizaDesdeRequest(request);

        // ASSERT
        assertThat(resultado).isNotNull();
        // Verificar que se descontó el inventario exactamente una vez
        verify(inventarioService, times(1)).descontarCantidad("SKU-001", 5);
        verify(polizaRepository, times(1)).save(any(Poliza.class));
    }

    @Test
    void debeLanzarExcepcion_cuandoInventarioEsInsuficiente() {
        // ARRANGE
        CrearPolizaRequest request = new CrearPolizaRequest(1L, "SKU-001", 10);

        when(inventarioService.buscarPorSku("SKU-001")).thenReturn(inventario);
        when(empleadoService.buscarPorId(1L)).thenReturn(empleado);

        // Simulamos que descontarCantidad lanza BusinessException
        doThrow(new BusinessException("Inventario insuficiente para SKU: SKU-001"))
                .when(inventarioService).descontarCantidad("SKU-001", 10);

        // ACT + ASSERT
        assertThatThrownBy(() -> polizaService.crearPolizaDesdeRequest(request))
                .isInstanceOf(BusinessException.class);

        verify(polizaRepository, never()).save(any());
    }

    @Test
    void debeLanzarExcepcion_cuandoEmpleadoNoExiste() {
        // ARRANGE
        CrearPolizaRequest request = new CrearPolizaRequest(99L, "SKU-001", 5);

        when(inventarioService.buscarPorSku("SKU-001")).thenReturn(inventario);
        // Simulamos que el empleado no existe
        when(empleadoService.buscarPorId(99L))
                .thenThrow(new ResourceNotFoundException("Empleado con ID 99 no encontrado"));

        // ACT + ASSERT
        assertThatThrownBy(() -> polizaService.crearPolizaDesdeRequest(request))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(polizaRepository, never()).save(any());
        verify(inventarioService, never()).descontarCantidad(any(), any());
    }

    @Test
    void debeLanzarExcepcion_cuandoSkuNoExiste() {
        // ARRANGE
        CrearPolizaRequest request = new CrearPolizaRequest(1L, "SKU-INEXISTENTE", 5);

        when(inventarioService.buscarPorSku("SKU-INEXISTENTE"))
                .thenThrow(new ResourceNotFoundException("Inventario con SKU SKU-INEXISTENTE no encontrado"));

        // ACT + ASSERT
        assertThatThrownBy(() -> polizaService.crearPolizaDesdeRequest(request))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(polizaRepository, never()).save(any());
        verify(empleadoService, never()).buscarPorId(any());
    }

    // ─────────────────────────────────────────────
    // ELIMINAR PÓLIZA
    // ─────────────────────────────────────────────

    @Test
    void debeEliminarPoliza_yRestaurarInventario() {
        // ARRANGE
        when(polizaRepository.findById(1L)).thenReturn(Optional.of(poliza));

        // ACT
        polizaService.eliminarPoliza(1L);

        // ASSERT — restaurarCantidad llamado con el SKU y cantidad correctos
        verify(inventarioService, times(1)).restaurarCantidad("SKU-001", 5);
        verify(polizaRepository, times(1)).delete(poliza);
    }

    @Test
    void debeLanzarExcepcion_cuandoPolizaNoExisteAlEliminar() {
        // ARRANGE
        when(polizaRepository.findById(99L)).thenReturn(Optional.empty());

        // ACT + ASSERT
        assertThatThrownBy(() -> polizaService.eliminarPoliza(99L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(inventarioService, never()).restaurarCantidad(any(), any());
        verify(polizaRepository, never()).delete(any());
    }

    // ─────────────────────────────────────────────
    // ACTUALIZAR PÓLIZA
    // ─────────────────────────────────────────────

    @Test
    void debeActualizarPoliza_cuandoMismoSkuYCantidadMenor() {
        // ARRANGE — cantidad baja de 5 a 3, debe devolver 2 al inventario
        ActualizarPolizaRequest request = new ActualizarPolizaRequest(1L, "SKU-001", 3);

        when(polizaRepository.findById(1L)).thenReturn(Optional.of(poliza));
        when(empleadoService.buscarPorId(1L)).thenReturn(empleado);
        when(inventarioService.buscarPorSku("SKU-001")).thenReturn(inventario);
        when(polizaRepository.save(any())).thenReturn(poliza);

        // ACT
        polizaService.actualizarPolizaDesdeRequest(1L, request);

        // ASSERT — diferencia = 5 - 3 = 2, debe restaurar 2
        verify(inventarioService, times(1)).restaurarCantidad("SKU-001", 2);
        verify(inventarioService, never()).descontarCantidad(any(), any());
    }

    @Test
    void debeActualizarPoliza_cuandoMismoSkuYCantidadMayor() {
        // ARRANGE — cantidad sube de 5 a 8, debe descontar 3 del inventario
        ActualizarPolizaRequest request = new ActualizarPolizaRequest(1L, "SKU-001", 8);

        when(polizaRepository.findById(1L)).thenReturn(Optional.of(poliza));
        when(empleadoService.buscarPorId(1L)).thenReturn(empleado);
        when(inventarioService.buscarPorSku("SKU-001")).thenReturn(inventario);
        when(polizaRepository.save(any())).thenReturn(poliza);

        // ACT
        polizaService.actualizarPolizaDesdeRequest(1L, request);

        // ASSERT — diferencia = 8 - 5 = 3, debe descontar 3
        verify(inventarioService, times(1)).descontarCantidad("SKU-001", 3);
        verify(inventarioService, never()).restaurarCantidad(any(), any());
    }

    @Test
    void debeActualizarPoliza_cuandoSkuDiferente() {
        // ARRANGE — cambia de SKU-001 a SKU-002
        Inventario inventarioNuevo = new Inventario("SKU-002", "Otro Artículo", 15);
        ActualizarPolizaRequest request = new ActualizarPolizaRequest(1L, "SKU-002", 4);

        when(polizaRepository.findById(1L)).thenReturn(Optional.of(poliza));
        when(empleadoService.buscarPorId(1L)).thenReturn(empleado);
        when(inventarioService.buscarPorSku("SKU-002")).thenReturn(inventarioNuevo);
        when(polizaRepository.save(any())).thenReturn(poliza);

        // ACT
        polizaService.actualizarPolizaDesdeRequest(1L, request);

        // ASSERT — restaura SKU anterior, descuenta SKU nuevo
        verify(inventarioService, times(1)).restaurarCantidad("SKU-001", 5);
        verify(inventarioService, times(1)).descontarCantidad("SKU-002", 4);
    }

    @Test
    void debeLanzarExcepcion_cuandoPolizaNoExisteAlActualizar() {
        // ARRANGE
        ActualizarPolizaRequest request = new ActualizarPolizaRequest(1L, "SKU-001", 5);

        when(polizaRepository.findById(99L)).thenReturn(Optional.empty());

        // ACT + ASSERT
        assertThatThrownBy(() -> polizaService.actualizarPolizaDesdeRequest(99L, request))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(polizaRepository, never()).save(any());
        verify(inventarioService, never()).descontarCantidad(any(), any());
        verify(inventarioService, never()).restaurarCantidad(any(), any());
    }
}
