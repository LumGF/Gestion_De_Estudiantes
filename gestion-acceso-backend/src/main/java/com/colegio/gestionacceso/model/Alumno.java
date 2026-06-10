// Paquete renombrado: com.t2lguevara.backend.model → com.colegio.gestionacceso.model
package com.colegio.gestionacceso.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "alumnos")
public class Alumno {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String codigo;
    private String nombre;
    private String descripcion;
    private String dni;
    private LocalDate fechaRegistro;

    /** Número completo Perú: 51 + 9 dígitos (ej. 51987654321), para notificaciones WhatsApp. */
    @Column(name = "telefono_padre1")
    private String telefonoPadre1;

    @Column(name = "telefono_padre2")
    private String telefonoPadre2;

    public Alumno() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getDni() { return dni; }
    public void setDni(String dni) { this.dni = dni; }

    public LocalDate getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(LocalDate fechaRegistro) { this.fechaRegistro = fechaRegistro; }

    public String getTelefonoPadre1() { return telefonoPadre1; }
    public void setTelefonoPadre1(String telefonoPadre1) { this.telefonoPadre1 = telefonoPadre1; }

    public String getTelefonoPadre2() { return telefonoPadre2; }
    public void setTelefonoPadre2(String telefonoPadre2) { this.telefonoPadre2 = telefonoPadre2; }
}