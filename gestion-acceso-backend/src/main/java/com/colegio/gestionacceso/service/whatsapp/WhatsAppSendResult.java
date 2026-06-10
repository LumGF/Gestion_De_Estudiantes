package com.colegio.gestionacceso.service.whatsapp;

public record WhatsAppSendResult(boolean success, String detail) {

    public static WhatsAppSendResult ok(String detail) {
        return new WhatsAppSendResult(true, detail);
    }

    public static WhatsAppSendResult fail(String detail) {
        return new WhatsAppSendResult(false, detail);
    }
}
