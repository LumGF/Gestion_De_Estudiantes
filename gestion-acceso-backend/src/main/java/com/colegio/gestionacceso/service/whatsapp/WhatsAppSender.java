package com.colegio.gestionacceso.service.whatsapp;

public interface WhatsAppSender {

    boolean isEnabled();

    WhatsAppStatus consultarEstado();

    WhatsAppSendResult enviarMensaje(String telefono, String mensaje);
}
