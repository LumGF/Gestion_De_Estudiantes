package com.colegio.gestionacceso.controller;

import com.colegio.gestionacceso.dto.UsuarioRequestDTO;
import com.colegio.gestionacceso.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final AuthService authService;

    public UsuarioController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public void crear(@Valid @RequestBody UsuarioRequestDTO request) {
        authService.crearUsuario(request);
    }
}
