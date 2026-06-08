package com.rdzvn.polizasdefaltantes.service;

import com.rdzvn.polizasdefaltantes.dto.request.ActualizarPolizaRequest;
import com.rdzvn.polizasdefaltantes.dto.request.CrearPolizaRequest;
import com.rdzvn.polizasdefaltantes.dto.response.PolizaResponse;
import com.rdzvn.polizasdefaltantes.entity.Empleado;
import com.rdzvn.polizasdefaltantes.entity.Inventario;
import com.rdzvn.polizasdefaltantes.entity.Poliza;
import com.rdzvn.polizasdefaltantes.excepciones.BusinessException;
import com.rdzvn.polizasdefaltantes.excepciones.ResourceNotFoundException;
import com.rdzvn.polizasdefaltantes.repository.PolizaRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class PolizaService {

    private final PolizaRepository polizaRepository;
    private final InventarioService inventarioService;
    private final EmpleadoService empleadoService;

    public PolizaService(PolizaRepository polizaRepository, InventarioService inventarioService, EmpleadoService empleadoService) {
        this.polizaRepository = polizaRepository;
        this.inventarioService = inventarioService;
        this.empleadoService = empleadoService;
    }

    @Transactional(readOnly = true)
    public List<PolizaResponse> listarPolizas(){
        return polizaRepository.findAll().stream()
                .map(PolizaResponse::desde)
                .collect(Collectors.toList());
    }

    public Poliza buscarPorIdPoliza(Long idPoliza){
        return polizaRepository.findById(idPoliza)
                .orElseThrow(()-> new ResourceNotFoundException("Poliza no encontrada"));
    }


    @Transactional
    public PolizaResponse crearPolizaDesdeRequest(CrearPolizaRequest request){
        log.info("Iniciando creación de póliza - empleado ID: {}, SKU: {}, cantidad: {}", request.idEmpleado(), request.sku(), request.cantidadFaltante());
        Inventario inventario = inventarioService.buscarPorSku(request.sku());
        Empleado empleado = empleadoService.buscarPorId(request.idEmpleado());

        try{
            inventarioService.descontarCantidad(request.sku(), request.cantidadFaltante());
        }catch (BusinessException e){
            log.warn("Inventario insuficiente - SKU: {}, solicitando: {}, disponible: {}",
                    request.sku(), request.cantidadFaltante(), inventario.getCantidad());
            throw e;
        }

        Poliza poliza = new Poliza(
                empleado,
                inventario,
                request.cantidadFaltante()
        );

        PolizaResponse response = PolizaResponse.desde(polizaRepository.save(poliza));

        log.info("Póliza creada exitosamente — ID: {}, SKU: {}, inventario restante: {}",
                response.idPoliza(), request.sku(),
                inventario.getCantidad() - request.cantidadFaltante());

        return response;

    }

    @Transactional
    public PolizaResponse actualizarPolizaDesdeRequest(Long id, ActualizarPolizaRequest request){
        log.info("Iniciando actualización de póliza — ID: {}, SKU: {}, nueva cantidad: {}",
                id, request.sku(), request.cantidadFaltante());

        Poliza poliza = buscarPorIdPoliza(id);
        Empleado empleado = empleadoService.buscarPorId(request.idEmpleado());
        boolean mismoSku = poliza.getInventario().getSku().equals(request.sku());

        if (mismoSku) {
            Integer cantidadAnterior = poliza.getCantidadFaltante();

            if (cantidadAnterior > request.cantidadFaltante()) {
                Integer diferencia = cantidadAnterior - request.cantidadFaltante();
                inventarioService.restaurarCantidad(request.sku(), diferencia);
                log.info("Inventario restaurado parcialmente — SKU: {}, diferencia devuelta: {}",
                        request.sku(), diferencia);

            } else if (cantidadAnterior < request.cantidadFaltante()) {
                Integer diferencia = request.cantidadFaltante() - cantidadAnterior;
                try {
                    inventarioService.descontarCantidad(request.sku(), diferencia);
                } catch (BusinessException e) {
                    log.warn("Inventario insuficiente al actualizar póliza — ID: {}, SKU: {}, diferencia: {}",
                            id, request.sku(), diferencia);
                    throw e;
                }
                log.info("Inventario descontado parcialmente — SKU: {}, diferencia descontada: {}",
                        request.sku(), diferencia);
            }

        } else {
            String skuAnterior = poliza.getInventario().getSku();
            inventarioService.restaurarCantidad(skuAnterior, poliza.getCantidadFaltante());
            log.info("Inventario anterior restaurado — SKU: {}, cantidad restaurada: {}",
                    skuAnterior, poliza.getCantidadFaltante());

            try {
                inventarioService.descontarCantidad(request.sku(), request.cantidadFaltante());
            } catch (BusinessException e) {
                log.warn("Inventario insuficiente en nuevo SKU — SKU: {}, solicitado: {}",
                        request.sku(), request.cantidadFaltante());
                throw e;
            }
            log.info("Inventario nuevo descontado — SKU: {}, cantidad descontada: {}",
                    request.sku(), request.cantidadFaltante());
        }

        Inventario inventarioActualizado = inventarioService.buscarPorSku(request.sku());
        poliza.setEmpleado(empleado);
        poliza.setInventario(inventarioActualizado);
        poliza.setCantidadFaltante(request.cantidadFaltante());

        PolizaResponse response = PolizaResponse.desde(polizaRepository.save(poliza));
        log.info("Póliza actualizada exitosamente — ID: {}", id);

        return response;
    }

    @Transactional
    public void eliminarPoliza(Long id){
        log.info("Iniciando eliminación de póliza — ID: {}", id);

        Poliza poliza = buscarPorIdPoliza(id);
        String sku = poliza.getInventario().getSku();
        Integer cantidadARestaurar = poliza.getCantidadFaltante();

        inventarioService.restaurarCantidad(sku, cantidadARestaurar);
        log.info("Inventario restaurado — SKU: {}, cantidad restaurada: {}", sku, cantidadARestaurar);

        polizaRepository.delete(poliza);
        log.info("Póliza eliminada exitosamente — ID: {}", id);
    }
}
