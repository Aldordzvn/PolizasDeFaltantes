package com.rdzvn.polizasdefaltantes.service;

import com.rdzvn.polizasdefaltantes.dto.request.ActualizarEmpleadoRequest;
import com.rdzvn.polizasdefaltantes.dto.request.CrearEmpleadoRequest;
import com.rdzvn.polizasdefaltantes.dto.response.EmpleadoResponse;
import com.rdzvn.polizasdefaltantes.entity.Empleado;
import com.rdzvn.polizasdefaltantes.excepciones.ResourceNotFoundException;
import com.rdzvn.polizasdefaltantes.repository.EmpleadoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class EmpleadoService {
    private final EmpleadoRepository empleadoRepository;

    public EmpleadoService(EmpleadoRepository empleadoRepository) {
        this.empleadoRepository = empleadoRepository;
    }

    public Empleado buscarPorId(Long id){
        return empleadoRepository.findById(id)
                .filter(Empleado::isActivo)
                .orElseThrow(()-> new ResourceNotFoundException("El Empleado con el ID " + id + " no fue encontrado"));
    }

    public List<EmpleadoResponse> listarComoResponse(){
        return empleadoRepository.findByActivoTrue().stream()
                .map(EmpleadoResponse::desde)
                .collect(Collectors.toList());
    }

    @Transactional
    public EmpleadoResponse crearDesdeRequest(CrearEmpleadoRequest request){
        Empleado nuevo = new Empleado(
                request.nombre(),
                request.apellido(),
                request.puesto()
        );

        return EmpleadoResponse.desde(empleadoRepository.save(nuevo));
    }

    @Transactional
    public EmpleadoResponse actualizarEmpleadoDesdeRequest(Long id, ActualizarEmpleadoRequest request){
        Empleado empleado = buscarPorId(id);
        empleado.setNombre(request.nombre());
        empleado.setApellido(request.apellido());
        return EmpleadoResponse.desde(empleadoRepository.save(empleado));
    }

    @Transactional
    public void eliminarEmpleado(Long id){
        if(!empleadoRepository.existsById(id)){
            throw new ResourceNotFoundException("El Empleado con el ID " + id + " ID no existe");
        }

        empleadoRepository.desactivar(id);
    }

}
