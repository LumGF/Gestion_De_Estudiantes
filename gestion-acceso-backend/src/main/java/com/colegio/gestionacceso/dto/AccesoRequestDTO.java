package com.colegio.gestionacceso.dto;

import com.colegio.gestionacceso.model.TipoAcceso;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class AccesoRequestDTO {

    @NotNull(message = "El alumno es requerido")
    private Long alumnoId;

    @NotNull(message = "El tipo de acceso es requerido")
    private TipoAcceso tipo;

    @Size(max = 500, message = "La observación no puede superar 500 caracteres")
    private String observacion;

    public AccesoRequestDTO() {}

    public Long getAlumnoId() { return alumnoId; }
    public void setAlumnoId(Long alumnoId) { this.alumnoId = alumnoId; }

    public TipoAcceso getTipo() { return tipo; }
    public void setTipo(TipoAcceso tipo) { this.tipo = tipo; }

    public String getObservacion() { return observacion; }
    public void setObservacion(String observacion) { this.observacion = observacion; }
}
