package com.colegio.gestionacceso.service.whatsapp;

import com.colegio.gestionacceso.config.WhatsAppProperties;
import com.colegio.gestionacceso.service.WhatsAppBridgeClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "whatsapp.provider", havingValue = "bridge", matchIfMissing = true)
public class BridgeWhatsAppSender implements WhatsAppSender {

    private final WhatsAppBridgeClient bridgeClient;
    private final WhatsAppProperties properties;

    public BridgeWhatsAppSender(WhatsAppBridgeClient bridgeClient, WhatsAppProperties properties) {
        this.bridgeClient = bridgeClient;
        this.properties = properties;
    }

    @Override
    public boolean isEnabled() {
        return properties.isEnabled();
    }

    @Override
    public WhatsAppStatus consultarEstado() {
        if (!isEnabled()) {
            return new WhatsAppStatus(false, "Integración WhatsApp deshabilitada");
        }
        WhatsAppBridgeClient.BridgeStatus status = bridgeClient.consultarEstado();
        return new WhatsAppStatus(status.ready(), status.message());
    }

    @Override
    public WhatsAppSendResult enviarMensaje(String telefono, String mensaje) {
        WhatsAppBridgeClient.SendResult result = bridgeClient.enviarMensaje(telefono, mensaje);
        return result.success()
                ? WhatsAppSendResult.ok(result.detail())
                : WhatsAppSendResult.fail(result.detail());
    }
}
