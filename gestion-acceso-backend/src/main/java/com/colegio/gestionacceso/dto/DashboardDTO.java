package com.colegio.gestionacceso.dto;

public class DashboardDTO {

    private long totalAlumnos;
    private long accesosHoy;
    private long entradasHoy;
    private long salidasHoy;
    private long noAsistioHoy;
    private long noAsistioConPermisoHoy;
    private long tardanzasHoy;
    private long notificacionesEnviadas;
    private long notificacionesFallidas;
    private boolean whatsAppListo;
    private String whatsAppMensaje;

    public DashboardDTO() {}

    public long getTotalAlumnos() { return totalAlumnos; }
    public void setTotalAlumnos(long totalAlumnos) { this.totalAlumnos = totalAlumnos; }

    public long getAccesosHoy() { return accesosHoy; }
    public void setAccesosHoy(long accesosHoy) { this.accesosHoy = accesosHoy; }

    public long getEntradasHoy() { return entradasHoy; }
    public void setEntradasHoy(long entradasHoy) { this.entradasHoy = entradasHoy; }

    public long getSalidasHoy() { return salidasHoy; }
    public void setSalidasHoy(long salidasHoy) { this.salidasHoy = salidasHoy; }

    public long getNoAsistioHoy() { return noAsistioHoy; }
    public void setNoAsistioHoy(long noAsistioHoy) { this.noAsistioHoy = noAsistioHoy; }

    public long getNoAsistioConPermisoHoy() { return noAsistioConPermisoHoy; }
    public void setNoAsistioConPermisoHoy(long noAsistioConPermisoHoy) { this.noAsistioConPermisoHoy = noAsistioConPermisoHoy; }

    public long getTardanzasHoy() { return tardanzasHoy; }
    public void setTardanzasHoy(long tardanzasHoy) { this.tardanzasHoy = tardanzasHoy; }

    public long getNotificacionesEnviadas() { return notificacionesEnviadas; }
    public void setNotificacionesEnviadas(long notificacionesEnviadas) { this.notificacionesEnviadas = notificacionesEnviadas; }

    public long getNotificacionesFallidas() { return notificacionesFallidas; }
    public void setNotificacionesFallidas(long notificacionesFallidas) { this.notificacionesFallidas = notificacionesFallidas; }

    public boolean isWhatsAppListo() { return whatsAppListo; }
    public void setWhatsAppListo(boolean whatsAppListo) { this.whatsAppListo = whatsAppListo; }

    public String getWhatsAppMensaje() { return whatsAppMensaje; }
    public void setWhatsAppMensaje(String whatsAppMensaje) { this.whatsAppMensaje = whatsAppMensaje; }
}
