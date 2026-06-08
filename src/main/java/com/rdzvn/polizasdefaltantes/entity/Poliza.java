package com.rdzvn.polizasdefaltantes.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "polizas")
public class Poliza {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idPoliza;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_empleado", nullable = false)
    private Empleado empleado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sku", nullable = false)
    private Inventario inventario;

    @Column(name = "cantidad_faltante", nullable = false)
    private Integer cantidadFaltante;

    @Column(name = "fecha_creacion", updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "actualizado_en", nullable = false)
    private LocalDateTime actualizadoEn;

    public Poliza() {
    }

    public Poliza(Empleado empleado, Inventario inventario, Integer cantidadFaltante) {
        this.empleado = empleado;
        this.inventario = inventario;
        this.cantidadFaltante = cantidadFaltante;
    }

    @PrePersist
    public void antesDeInsertar(){
        fechaCreacion = LocalDateTime.now();
        actualizadoEn = LocalDateTime.now();
    }

    @PreUpdate
    public void antesDeActualizar(){
        actualizadoEn = LocalDateTime.now();
    }

    public Long getIdPoliza() {
        return idPoliza;
    }

    public Empleado getEmpleado() {
        return empleado;
    }

    public Inventario getInventario() {
        return inventario;
    }

    public Integer getCantidadFaltante() {
        return cantidadFaltante;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public LocalDateTime getActualizadoEn() {
        return actualizadoEn;
    }

    public void setEmpleado(Empleado empleado) {
        this.empleado = empleado;
    }

    public void setInventario(Inventario inventario) {
        this.inventario = inventario;
    }

    public void setCantidadFaltante(Integer cantidadFaltante) {
        this.cantidadFaltante = cantidadFaltante;
    }

    @Override
    public String toString() {
        return "Poliza{" +
                "idPoliza=" + idPoliza +
                ", empleado=" + empleado +
                ", inventario=" + inventario +
                ", cantidadFaltante=" + cantidadFaltante +
                ", fechaCreacion=" + fechaCreacion +
                ", actualizadoEn=" + actualizadoEn +
                '}';
    }
}
