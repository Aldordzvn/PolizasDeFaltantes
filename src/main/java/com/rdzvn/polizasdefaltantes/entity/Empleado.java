package com.rdzvn.polizasdefaltantes.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

@Entity
@Table(name = "empleados")
public class Empleado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idEmpleado;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, length = 100)
    private String apellido;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private Roles puesto;

    @Column(name = "creado_en", updatable = false)
    private LocalDateTime creadoEn;

    @Column(name = "actualizado_en")
    private LocalDateTime actualizadoEn;

    @Column(nullable = false)
    private boolean activo = true;

    public Empleado() {
    }

    public Empleado(String nombre, String apellido, Roles puesto) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.puesto = puesto;
    }

    @PrePersist
    void antesDeInsertar(){
        creadoEn = LocalDateTime.now();
        actualizadoEn = LocalDateTime.now();
    }

    @PreUpdate
    void antesDeActualizar(){
        actualizadoEn = LocalDateTime.now();
    }

    public Long getIdEmpleado() {
        return idEmpleado;
    }

    public boolean isActivo() {
        return activo;
    }

    public String getNombre() {
        return nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public Roles getPuesto() {
        return puesto;
    }

    public LocalDateTime getCreadoEn() {
        return creadoEn;
    }

    public LocalDateTime getActualizadoEn() {
        return actualizadoEn;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public void setPuesto(Roles puesto) {
        this.puesto = puesto;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    @Override
    public String toString() {
        return "Empleado{" +
                "idEmpleado=" + idEmpleado +
                ", nombre='" + nombre + '\'' +
                ", apellido='" + apellido + '\'' +
                ", puesto=" + puesto +
                ", creadoEn=" + creadoEn +
                ", actualizadoEn=" + actualizadoEn +
                ", activo=" + activo +
                '}';
    }
}
