package com.colegio.gestionacceso.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public class AlumnoDTO {

    private Long id;

    @NotBlank(message = "El código es requerido")
    private String codigo;

    @NotBlank(message = "El nombre es requerido")
    private String nombre;

    @NotBlank(message = "La descripción es requerida")
    private String descripcion;

    @NotBlank(message = "El DNI es requerido")
    @Size(min = 8, max = 8, message = "El DNI debe tener 8 dígitos")
    @Pattern(regexp = "\\d{8}", message = "El DNI debe contener solo números")
    private String dni;

    @NotNull(message = "La fecha de registro es requerida")
    private LocalDate fechaRegistro;

    @NotBlank(message = "El teléfono del padre/madre 1 es requerido")
    @Pattern(regexp = "51?9\\d{8}|9\\d{8}", message = "Teléfono inválido (celular Perú)")
    private String telefonoPadre1;

    @Pattern(regexp = "^(|51?9\\d{8}|9\\d{8})$", message = "Teléfono inválido (celular Perú)")
    private String telefonoPadre2;

    public AlumnoDTO() {}

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
