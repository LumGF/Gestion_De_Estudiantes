package com.colegio.gestionacceso.service;

import com.colegio.gestionacceso.dto.LoginRequestDTO;
import com.colegio.gestionacceso.dto.LoginResponseDTO;
import com.colegio.gestionacceso.dto.UsuarioRequestDTO;
import com.colegio.gestionacceso.exception.BusinessException;
import com.colegio.gestionacceso.config.JwtService;
import com.colegio.gestionacceso.model.Usuario;
import com.colegio.gestionacceso.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService
    ) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public LoginResponseDTO login(LoginRequestDTO request) {
        Usuario usuario = usuarioRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BusinessException("Credenciales inválidas"));

        if (!usuario.isActivo() || !passwordEncoder.matches(request.getPassword(), usuario.getPassword())) {
            throw new BusinessException("Credenciales inválidas");
        }

        String token = jwtService.generateToken(usuario.getEmail(), usuario.getRol().name());
        return new LoginResponseDTO(token, usuario.getEmail(), usuario.getNombre(), usuario.getRol());
    }

    public void crearUsuario(UsuarioRequestDTO request) {
        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("El email ya está registrado");
        }
        Usuario usuario = new Usuario();
        usuario.setEmail(request.getEmail());
        usuario.setPassword(passwordEncoder.encode(request.getPassword()));
        usuario.setNombre(request.getNombre());
        usuario.setRol(request.getRol());
        usuarioRepository.save(usuario);
    }
}
