package com.colegio.gestionacceso.controller;

import com.colegio.gestionacceso.dto.AccesoDTO;
import com.colegio.gestionacceso.dto.AccesoRequestDTO;
import com.colegio.gestionacceso.service.AccesoService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/accesos")
public class AccesoController {

    private final AccesoService accesoService;

    public AccesoController(AccesoService accesoService) {
        this.accesoService = accesoService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AccesoDTO registrar(@Valid @RequestBody AccesoRequestDTO request) {
        return accesoService.registrar(request);
    }

    @GetMapping
    public Page<AccesoDTO> historial(
            @PageableDefault(size = 20) Pageable pageable,
            @RequestParam(required = false) Long alumnoId
    ) {
        return accesoService.listarHistorial(pageable, alumnoId);
    }

    @GetMapping("/hoy")
    public List<AccesoDTO> listarHoy() {
        return accesoService.listarHoy();
    }

    @GetMapping("/{id}")
    public AccesoDTO buscarPorId(@PathVariable @NonNull Long id) {
        return accesoService.buscarPorId(id);
    }
}
