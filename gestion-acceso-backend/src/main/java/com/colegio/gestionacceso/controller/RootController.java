package com.colegio.gestionacceso.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
public class RootController {

    @GetMapping("/")
    public Map<String, Object> inicio() {
        Map<String, Object> links = new LinkedHashMap<>();
        links.put("swagger", "/swagger-ui/index.html");
        links.put("openapi", "/v3/api-docs");
        links.put("health", "/actuator/health");
        links.put("login", "POST /api/auth/login");
        links.put("alumnos", "GET /api/alumnos (requiere JWT)");
        links.put("accesos", "GET /api/accesos (requiere JWT)");
        links.put("dashboard", "GET /api/dashboard (requiere JWT)");

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("nombre", "Colegio - Gestión de Acceso API");
        body.put("version", "1.0.0");
        body.put("estado", "activo");
        body.put("autenticacion", "JWT Bearer — POST /api/auth/login con email y password");
        body.put("nota", "Los endpoints /api/* (excepto /api/auth) requieren header Authorization: Bearer <token>");
        body.put("enlaces", links);
        return body;
    }
}
