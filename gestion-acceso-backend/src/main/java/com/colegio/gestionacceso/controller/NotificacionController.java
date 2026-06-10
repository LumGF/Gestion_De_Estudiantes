package com.colegio.gestionacceso.controller;

import com.colegio.gestionacceso.dto.NotificacionDTO;
import com.colegio.gestionacceso.service.NotificacionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notificaciones")
public class NotificacionController {

    private final NotificacionService notificacionService;

    public NotificacionController(NotificacionService notificacionService) {
        this.notificacionService = notificacionService;
    }

    @GetMapping
    public Page<NotificacionDTO> listarLog(@PageableDefault(size = 20) Pageable pageable) {
        return notificacionService.listarLog(pageable);
    }

    @PostMapping("/reintentar")
    public void reintentarPendientes() {
        notificacionService.reintentarPendientes();
    }
}
