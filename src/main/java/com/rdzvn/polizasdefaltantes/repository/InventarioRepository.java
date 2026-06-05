package com.rdzvn.polizasdefaltantes.repository;

import com.rdzvn.polizasdefaltantes.entity.Inventario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InventarioRepository extends JpaRepository<Inventario, String> {
    boolean existsBySku(String sku);
    Optional<Inventario> findBySku(String sku);
}
