// Paquete renombrado: com.t2lguevara.backend.controller → com.colegio.gestionacceso.controller
package com.colegio.gestionacceso.controller;

import com.colegio.gestionacceso.dto.AlumnoDTO;
import com.colegio.gestionacceso.service.AlumnoService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/alumnos")
public class AlumnoController {

    private final AlumnoService alumnoService;

    public AlumnoController(AlumnoService alumnoService) {
        this.alumnoService = alumnoService;
    }

    @GetMapping
    public Page<AlumnoDTO> listar(
            @PageableDefault(size = 10) Pageable pageable,
            @RequestParam(required = false) String q
    ) {
        return alumnoService.listar(pageable, q);
    }

    @GetMapping("/{id}")
    public AlumnoDTO buscarPorId(@PathVariable @NonNull Long id) {
        return alumnoService.obtenerOError(id);
    }

    @GetMapping("/codigo/{codigo}")
    public AlumnoDTO buscarPorCodigo(@PathVariable String codigo) {
        return alumnoService.buscarPorCodigo(codigo)
                .orElseThrow(() -> new com.colegio.gestionacceso.exception.ResourceNotFoundException(
                        "Alumno no encontrado con código " + codigo));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AlumnoDTO guardar(@Valid @RequestBody @NonNull AlumnoDTO dto) {
        return alumnoService.guardar(dto);
    }

    @PutMapping("/{id}")
    public AlumnoDTO actualizar(@PathVariable @NonNull Long id, @Valid @RequestBody @NonNull AlumnoDTO dto) {
        return alumnoService.actualizar(id, dto)
                .orElseThrow(() -> new com.colegio.gestionacceso.exception.ResourceNotFoundException("Alumno no encontrado"));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable @NonNull Long id) {
        if (!alumnoService.eliminar(id)) {
            throw new com.colegio.gestionacceso.exception.ResourceNotFoundException("Alumno no encontrado");
        }
    }
}
