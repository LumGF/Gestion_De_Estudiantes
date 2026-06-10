package com.colegio.gestionacceso.dto;

public class WhatsAppEstadoDTO {

    private boolean bridgeHabilitado;
    private boolean listo;
    private String mensaje;

    public WhatsAppEstadoDTO() {}

    public WhatsAppEstadoDTO(boolean bridgeHabilitado, boolean listo, String mensaje) {
        this.bridgeHabilitado = bridgeHabilitado;
        this.listo = listo;
        this.mensaje = mensaje;
    }

    public boolean isBridgeHabilitado() { return bridgeHabilitado; }
    public void setBridgeHabilitado(boolean bridgeHabilitado) { this.bridgeHabilitado = bridgeHabilitado; }

    public boolean isListo() { return listo; }
    public void setListo(boolean listo) { this.listo = listo; }

    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }
}
