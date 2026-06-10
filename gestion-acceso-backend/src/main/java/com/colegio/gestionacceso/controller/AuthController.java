package com.colegio.gestionacceso.controller;

import com.colegio.gestionacceso.dto.LoginRequestDTO;
import com.colegio.gestionacceso.dto.LoginResponseDTO;
import com.colegio.gestionacceso.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public LoginResponseDTO login(@Valid @RequestBody LoginRequestDTO request) {
        return authService.login(request);
    }
}
