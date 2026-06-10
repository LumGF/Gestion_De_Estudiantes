package com.colegio.gestionacceso.dto;

import java.util.ArrayList;
import java.util.List;

public class WhatsAppPruebaResponseDTO {

    private Long alumnoId;
    private String alumnoNombre;
    private String mensajeEnviado;
    private boolean exito;
    private String resumen;
    private List<WhatsAppEnvioResultadoDTO> resultados = new ArrayList<>();

    public Long getAlumnoId() { return alumnoId; }
    public void setAlumnoId(Long alumnoId) { this.alumnoId = alumnoId; }

    public String getAlumnoNombre() { return alumnoNombre; }
    public void setAlumnoNombre(String alumnoNombre) { this.alumnoNombre = alumnoNombre; }

    public String getMensajeEnviado() { return mensajeEnviado; }
    public void setMensajeEnviado(String mensajeEnviado) { this.mensajeEnviado = mensajeEnviado; }

    public boolean isExito() { return exito; }
    public void setExito(boolean exito) { this.exito = exito; }

    public String getResumen() { return resumen; }
    public void setResumen(String resumen) { this.resumen = resumen; }

    public List<WhatsAppEnvioResultadoDTO> getResultados() { return resultados; }
    public void setResultados(List<WhatsAppEnvioResultadoDTO> resultados) { this.resultados = resultados; }
}
