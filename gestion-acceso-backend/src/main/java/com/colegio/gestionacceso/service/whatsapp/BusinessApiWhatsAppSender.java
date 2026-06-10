package com.colegio.gestionacceso.service.whatsapp;

import com.colegio.gestionacceso.config.WhatsAppProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * Implementación para WhatsApp Business API (Meta Cloud API o proveedor compatible).
 * Configure whatsapp.provider=business-api y las credenciales en application.properties.
 */
@Component
@ConditionalOnProperty(name = "whatsapp.provider", havingValue = "business-api")
public class BusinessApiWhatsAppSender implements WhatsAppSender {

    private static final Logger log = LoggerFactory.getLogger(BusinessApiWhatsAppSender.class);

    private final RestTemplate restTemplate;
    private final WhatsAppProperties properties;

    public BusinessApiWhatsAppSender(RestTemplate restTemplate, WhatsAppProperties properties) {
        this.restTemplate = restTemplate;
        this.properties = properties;
    }

    @Override
    public boolean isEnabled() {
        return properties.isEnabled()
                && properties.getBusinessApiUrl() != null
                && !properties.getBusinessApiUrl().isBlank()
                && properties.getBusinessApiToken() != null
                && !properties.getBusinessApiToken().isBlank();
    }

    @Override
    public WhatsAppStatus consultarEstado() {
        if (!isEnabled()) {
            return new WhatsAppStatus(false, "WhatsApp Business API no configurada");
        }
        return new WhatsAppStatus(true, "WhatsApp Business API configurada");
    }

    @Override
    @SuppressWarnings("unchecked")
    public WhatsAppSendResult enviarMensaje(String telefono, String mensaje) {
        if (!isEnabled()) {
            return WhatsAppSendResult.fail("WhatsApp Business API no configurada");
        }
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(properties.getBusinessApiToken());

            Map<String, Object> body = Map.of(
                    "messaging_product", "whatsapp",
                    "to", telefono,
                    "type", "text",
                    "text", Map.of("body", mensaje)
            );

            restTemplate.postForEntity(
                    properties.getBusinessApiUrl(),
                    new HttpEntity<>(body, headers),
                    Map.class
            );
            log.info("Mensaje enviado vía Business API a {}", telefono);
            return WhatsAppSendResult.ok("Mensaje enviado vía Business API");
        } catch (RestClientException ex) {
            log.error("Error Business API WhatsApp: {}", ex.getMessage());
            return WhatsAppSendResult.fail("Error Business API: " + ex.getMessage());
        }
    }
}
