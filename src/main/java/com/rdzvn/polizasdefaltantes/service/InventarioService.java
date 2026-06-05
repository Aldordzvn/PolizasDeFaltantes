package com.rdzvn.polizasdefaltantes.service;

import com.rdzvn.polizasdefaltantes.dto.request.ActualizarInventarioRequest;
import com.rdzvn.polizasdefaltantes.dto.request.CrearInventarioRequest;
import com.rdzvn.polizasdefaltantes.dto.response.InventarioResponse;
import com.rdzvn.polizasdefaltantes.entity.Inventario;
import com.rdzvn.polizasdefaltantes.excepciones.BusinessException;
import com.rdzvn.polizasdefaltantes.excepciones.ResourceNotFoundException;
import com.rdzvn.polizasdefaltantes.repository.InventarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class InventarioService {
    private final InventarioRepository inventarioRepository;

    public InventarioService(InventarioRepository inventarioRepository) {
        this.inventarioRepository = inventarioRepository;
    }

    public List<InventarioResponse> listarDesdeResponse(){
        return inventarioRepository.findAll().stream()
                .map(InventarioResponse::desde)
                .collect(Collectors.toList());
    }

    public Inventario buscarPorSku(String sku){
        return inventarioRepository.findBySku(sku)
                .orElseThrow(()-> new ResourceNotFoundException(
                        "El inventario con SKU " + sku + " no fue encontrado"
                ));
    }

    @Transactional
    public InventarioResponse crearInventarioDesdeRequest(CrearInventarioRequest request){
        if(inventarioRepository.existsBySku(request.sku())){
            throw new BusinessException("ya existe un inventario con este SKU");
        }
        Inventario nuevo = new Inventario(
                request.sku(),
                request.nombre(),
                request.cantidad()
        );

        return InventarioResponse.desde(inventarioRepository.save(nuevo));
    }


    @Transactional
    public InventarioResponse actualizarInventarioDeseRequest(String sku, ActualizarInventarioRequest request){
        Inventario inventarioActualizado = buscarPorSku(sku);
        inventarioActualizado.setNombre(request.nombre());
        inventarioActualizado.setCantidad(request.cantidad());
        return InventarioResponse.desde(inventarioRepository.save(inventarioActualizado));
    }

    @Transactional
    public void eliminarInventario(String sku){
        Inventario inventario = buscarPorSku(sku);
        inventarioRepository.delete(inventario);
    }


}
