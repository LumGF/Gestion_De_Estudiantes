package com.colegio.gestionacceso.dto;

public class WhatsAppEnvioResultadoDTO {

    private String telefono;
    private boolean enviado;
    private String detalle;

    public WhatsAppEnvioResultadoDTO() {}

    public WhatsAppEnvioResultadoDTO(String telefono, boolean enviado, String detalle) {
        this.telefono = telefono;
        this.enviado = enviado;
        this.detalle = detalle;
    }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public boolean isEnviado() { return enviado; }
    public void setEnviado(boolean enviado) { this.enviado = enviado; }

    public String getDetalle() { return detalle; }
    public void setDetalle(String detalle) { this.detalle = detalle; }
}
