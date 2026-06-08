package com.rdzvn.polizasdefaltantes.service;

import com.rdzvn.polizasdefaltantes.dto.request.ActualizarPolizaRequest;
import com.rdzvn.polizasdefaltantes.dto.request.CrearPolizaRequest;
import com.rdzvn.polizasdefaltantes.dto.response.PolizaResponse;
import com.rdzvn.polizasdefaltantes.entity.Empleado;
import com.rdzvn.polizasdefaltantes.entity.Inventario;
import com.rdzvn.polizasdefaltantes.entity.Poliza;
import com.rdzvn.polizasdefaltantes.excepciones.BusinessException;
import com.rdzvn.polizasdefaltantes.excepciones.ResourceNotFoundException;
import com.rdzvn.polizasdefaltantes.repository.EmpleadoRepository;
import com.rdzvn.polizasdefaltantes.repository.InventarioRepository;
import com.rdzvn.polizasdefaltantes.repository.PolizaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PolizaService {

    private final PolizaRepository polizaRepository;
    private final InventarioRepository inventarioRepository;
    private final EmpleadoRepository empleadoRepository;

    public PolizaService(PolizaRepository polizaRepository, InventarioRepository inventarioRepository, EmpleadoRepository empleadoRepository) {
        this.polizaRepository = polizaRepository;
        this.inventarioRepository = inventarioRepository;
        this.empleadoRepository = empleadoRepository;
    }

    public List<PolizaResponse> listarPolizas(){
        return polizaRepository.findAll().stream()
                .map(PolizaResponse::desde)
                .collect(Collectors.toList());
    }

    public Poliza buscarPorIdPoliza(Long idPoliza){
        return polizaRepository.findById(idPoliza)
                .orElseThrow(()-> new ResourceNotFoundException("Poliza no encontrada"));
    }

    public Inventario buscarPorSku(String sku){
        return inventarioRepository.findBySku(sku)
                .orElseThrow(()-> new ResourceNotFoundException("El inventario con ese SKU no fue encontrado"));
    }

    public Empleado buscarPorIdEmpleado(Long id){
        return empleadoRepository.findById(id)
                .filter(Empleado::isActivo)
                .orElseThrow(()-> new ResourceNotFoundException("El empleado con ese ID no fue encontrado"));

    }


    @Transactional
    public PolizaResponse crearPolizaDesdeRequest(CrearPolizaRequest request){
        Inventario inventario = buscarPorSku(request.sku());
        Empleado empleado = buscarPorIdEmpleado(request.idEmpleado());

        if(inventario.getCantidad() < request.cantidadFaltante()){
            throw new BusinessException("La cantidad es mayor a la cantidad existente del inventario");
        }

        inventario.setCantidad(inventario.getCantidad() - request.cantidadFaltante());
        inventarioRepository.save(inventario);

        Poliza poliza = new Poliza(
                empleado,
                inventario,
                request.cantidadFaltante()
        );

        return PolizaResponse.desde(polizaRepository.save(poliza));

    }

    @Transactional
    public PolizaResponse actualizarPolizaDesdeRequest(Long id, ActualizarPolizaRequest request){
        Poliza poliza = buscarPorIdPoliza(id);
        Inventario inventario = buscarPorSku(request.sku());
        Empleado empleado = buscarPorIdEmpleado(request.idEmpleado());
        boolean mismoSku = poliza.getInventario().getSku().equals(request.sku());

        if(mismoSku){
            if(poliza.getCantidadFaltante() > request.cantidadFaltante()){
                Integer diferencia = poliza.getCantidadFaltante() - request.cantidadFaltante();
                inventario.setCantidad(inventario.getCantidad() + diferencia);
            } else if (poliza.getCantidadFaltante() < request.cantidadFaltante()) {
                Integer diferencia = request.cantidadFaltante() - poliza.getCantidadFaltante();
                Integer cantidadResultado = inventario.getCantidad() - diferencia;

                if (cantidadResultado < 0){
                    throw new BusinessException(
                            "Inventario insuficiente para actualizar la poliza"
                    );
                }
                inventario.setCantidad(cantidadResultado);
            }
            inventarioRepository.save(inventario);
        }else {
            Inventario inventarioAnterior = buscarPorSku(poliza.getInventario().getSku());
            inventarioAnterior.setCantidad(inventarioAnterior.getCantidad() + poliza.getCantidadFaltante());
            inventarioRepository.save(inventarioAnterior);

            if (inventario.getCantidad() < request.cantidadFaltante()){
                throw new BusinessException("Inventario insuficiente para el SKU: " + request.sku());
            }

            inventario.setCantidad(inventario.getCantidad() - request.cantidadFaltante());
            inventarioRepository.save(inventario);
        }

        poliza.setEmpleado(empleado);
        poliza.setInventario(inventario);
        poliza.setCantidadFaltante(request.cantidadFaltante());
        return PolizaResponse.desde(polizaRepository.save(poliza));
    }

    @Transactional
    public void eliminarPoliza(Long id){
        Poliza poliza = buscarPorIdPoliza(id);
        Inventario inventario = buscarPorSku(poliza.getInventario().getSku());
        Integer cantidadRestaurada = inventario.getCantidad() + poliza.getCantidadFaltante();
        inventario.setCantidad(cantidadRestaurada);
        inventarioRepository.save(inventario);
        polizaRepository.delete(poliza);
    }
}
