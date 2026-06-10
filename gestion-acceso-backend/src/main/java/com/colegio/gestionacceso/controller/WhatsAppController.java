package com.colegio.gestionacceso.controller;

import com.colegio.gestionacceso.dto.WhatsAppEstadoDTO;
import com.colegio.gestionacceso.dto.WhatsAppPruebaResponseDTO;
import com.colegio.gestionacceso.exception.ResourceNotFoundException;
import com.colegio.gestionacceso.service.WhatsAppService;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/whatsapp")
public class WhatsAppController {

    private final WhatsAppService whatsAppService;

    public WhatsAppController(WhatsAppService whatsAppService) {
        this.whatsAppService = whatsAppService;
    }

    @GetMapping("/estado")
    public WhatsAppEstadoDTO estado() {
        return whatsAppService.consultarEstado();
    }

    @PostMapping("/prueba/{alumnoId}")
    public WhatsAppPruebaResponseDTO prueba(@PathVariable @NonNull Long alumnoId) {
        return whatsAppService.enviarPruebaAlumno(alumnoId)
                .orElseThrow(() -> new ResourceNotFoundException("Alumno no encontrado"));
    }
}
