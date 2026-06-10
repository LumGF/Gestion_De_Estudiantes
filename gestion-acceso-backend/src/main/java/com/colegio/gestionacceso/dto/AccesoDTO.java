package com.colegio.gestionacceso.dto;

import com.colegio.gestionacceso.model.TipoAcceso;

import java.time.LocalDateTime;
import java.util.List;

public class AccesoDTO {

    private Long id;
    private Long alumnoId;
    private String alumnoNombre;
    private String alumnoCodigo;
    private TipoAcceso tipo;
    private LocalDateTime fechaHora;
    private String registradoPor;
    private String observacion;
    private List<NotificacionDTO> notificaciones;

    public AccesoDTO() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getAlumnoId() { return alumnoId; }
    public void setAlumnoId(Long alumnoId) { this.alumnoId = alumnoId; }

    public String getAlumnoNombre() { return alumnoNombre; }
    public void setAlumnoNombre(String alumnoNombre) { this.alumnoNombre = alumnoNombre; }

    public String getAlumnoCodigo() { return alumnoCodigo; }
    public void setAlumnoCodigo(String alumnoCodigo) { this.alumnoCodigo = alumnoCodigo; }

    public TipoAcceso getTipo() { return tipo; }
    public void setTipo(TipoAcceso tipo) { this.tipo = tipo; }

    public LocalDateTime getFechaHora() { return fechaHora; }
    public void setFechaHora(LocalDateTime fechaHora) { this.fechaHora = fechaHora; }

    public String getRegistradoPor() { return registradoPor; }
    public void setRegistradoPor(String registradoPor) { this.registradoPor = registradoPor; }

    public String getObservacion() { return observacion; }
    public void setObservacion(String observacion) { this.observacion = observacion; }

    public List<NotificacionDTO> getNotificaciones() { return notificaciones; }
    public void setNotificaciones(List<NotificacionDTO> notificaciones) { this.notificaciones = notificaciones; }
}
