package com.colegio.gestionacceso.service;

import com.colegio.gestionacceso.config.WhatsAppProperties;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
public class WhatsAppBridgeClient {

    private final RestTemplate restTemplate;
    private final WhatsAppProperties properties;

    public WhatsAppBridgeClient(RestTemplate restTemplate, WhatsAppProperties properties) {
        this.restTemplate = restTemplate;
        this.properties = properties;
    }

    public boolean isEnabled() {
        return properties.isEnabled();
    }

    public BridgeStatus consultarEstado() {
        if (!properties.isEnabled()) {
            return new BridgeStatus(false, "WhatsApp deshabilitado en application.properties");
        }
        try {
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    properties.getBridgeUrl() + "/status",
                    HttpMethod.GET,
                    new HttpEntity<>(buildHeaders()),
                    new ParameterizedTypeReference<>() {}
            );
            Map<String, Object> body = response.getBody();
            if (body == null) {
                return new BridgeStatus(false, "Sin respuesta del bridge");
            }
            boolean ready = Boolean.TRUE.equals(body.get("ready"));
            String message = body.get("message") != null ? body.get("message").toString() : "";
            return new BridgeStatus(ready, message);
        } catch (HttpStatusCodeException ex) {
            return new BridgeStatus(false, extractError(ex));
        } catch (RestClientException ex) {
            return new BridgeStatus(false,
                    "No se pudo conectar al bridge en " + properties.getBridgeUrl()
                            + ". Ejecute: cd whatsapp-bridge && npm start");
        }
    }

    public SendResult enviarMensaje(String telefono, String mensaje) {
        if (!properties.isEnabled()) {
            return SendResult.fail("WhatsApp deshabilitado en configuración");
        }
        try {
            Map<String, String> body = Map.of("to", telefono, "message", mensaje);
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    properties.getBridgeUrl() + "/send",
                    HttpMethod.POST,
                    new HttpEntity<>(body, buildHeaders()),
                    new ParameterizedTypeReference<>() {}
            );
            Map<String, Object> result = response.getBody();
            if (result != null && Boolean.TRUE.equals(result.get("ok"))) {
                return SendResult.ok("Mensaje enviado");
            }
            String error = result != null && result.get("error") != null
                    ? result.get("error").toString()
                    : "Respuesta inválida del bridge";
            return SendResult.fail(error);
        } catch (HttpStatusCodeException ex) {
            return SendResult.fail(extractError(ex));
        } catch (RestClientException ex) {
            return SendResult.fail("Error de conexión con el bridge: " + ex.getMessage());
        }
    }

    private HttpHeaders buildHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (properties.getBridgeApiKey() != null && !properties.getBridgeApiKey().isBlank()) {
            headers.set("X-API-Key", properties.getBridgeApiKey());
        }
        return headers;
    }

    private String extractError(HttpStatusCodeException ex) {
        String body = ex.getResponseBodyAsString();
        if (body != null && !body.isBlank()) {
            return body;
        }
        return ex.getStatusCode() + " — " + ex.getStatusText();
    }

    public record BridgeStatus(boolean ready, String message) {}

    public record SendResult(boolean success, String detail) {
        static SendResult ok(String detail) {
            return new SendResult(true, detail);
        }

        static SendResult fail(String detail) {
            return new SendResult(false, detail);
        }
    }
}
