package com.rdzvn.polizasdefaltantes.repository;

import com.rdzvn.polizasdefaltantes.entity.Empleado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface EmpleadoRepository extends JpaRepository<Empleado, Long> {
    List<Empleado> findByActivoTrue();

    String DESACTIVAR_QUERY = "UPDATE Empleado e SET e.activo = false WHERE e.idEmpleado = :id";

    // Soft delete — marca como inactivo en vez de borrar
    @Modifying
    @Query(DESACTIVAR_QUERY)
    Long desactivar(@Param("id") Long id);

}
