package com.colegio.gestionacceso.dto;

import com.colegio.gestionacceso.model.EstadoNotificacion;

import java.time.LocalDateTime;

public class NotificacionDTO {

    private Long id;
    private Long accesoId;
    private String telefono;
    private String mensaje;
    private EstadoNotificacion estado;
    private int intentos;
    private String error;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaEnvio;
    private String alumnoNombre;

    public NotificacionDTO() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getAccesoId() { return accesoId; }
    public void setAccesoId(Long accesoId) { this.accesoId = accesoId; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }

    public EstadoNotificacion getEstado() { return estado; }
    public void setEstado(EstadoNotificacion estado) { this.estado = estado; }

    public int getIntentos() { return intentos; }
    public void setIntentos(int intentos) { this.intentos = intentos; }

    public String getError() { return error; }
    public void setError(String error) { this.error = error; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public LocalDateTime getFechaEnvio() { return fechaEnvio; }
    public void setFechaEnvio(LocalDateTime fechaEnvio) { this.fechaEnvio = fechaEnvio; }

    public String getAlumnoNombre() { return alumnoNombre; }
    public void setAlumnoNombre(String alumnoNombre) { this.alumnoNombre = alumnoNombre; }
}
