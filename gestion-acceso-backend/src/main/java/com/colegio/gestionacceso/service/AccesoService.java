package com.colegio.gestionacceso.service;

import com.colegio.gestionacceso.dto.AccesoDTO;
import com.colegio.gestionacceso.dto.AccesoRequestDTO;
import com.colegio.gestionacceso.exception.BusinessException;
import com.colegio.gestionacceso.exception.ResourceNotFoundException;
import com.colegio.gestionacceso.model.Acceso;
import com.colegio.gestionacceso.model.Alumno;
import com.colegio.gestionacceso.model.TipoAcceso;
import com.colegio.gestionacceso.repository.AccesoRepository;
import com.colegio.gestionacceso.repository.AlumnoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AccesoService {

    private final AccesoRepository accesoRepository;
    private final AlumnoRepository alumnoRepository;
    private final NotificacionService notificacionService;

    public AccesoService(
            AccesoRepository accesoRepository,
            AlumnoRepository alumnoRepository,
            NotificacionService notificacionService
    ) {
        this.accesoRepository = accesoRepository;
        this.alumnoRepository = alumnoRepository;
        this.notificacionService = notificacionService;
    }

    @Transactional
    public AccesoDTO registrar(@NonNull AccesoRequestDTO request) {
        Alumno alumno = alumnoRepository.findById(request.getAlumnoId())
                .orElseThrow(() -> new ResourceNotFoundException("Alumno no encontrado"));

        validarRequest(request);

        Acceso acceso = new Acceso();
        acceso.setAlumno(alumno);
        acceso.setTipo(request.getTipo());
        acceso.setFechaHora(LocalDateTime.now());
        acceso.setRegistradoPor(obtenerUsuarioActual());
        acceso.setObservacion(normalizarObservacion(request.getObservacion()));

        Acceso guardado = accesoRepository.save(acceso);
        notificacionService.notificarAcceso(guardado);

        return toDTO(guardado, true);
    }

    @Transactional(readOnly = true)
    public Page<AccesoDTO> listarHistorial(Pageable pageable, Long alumnoId) {
        Page<Acceso> page = alumnoId != null
                ? accesoRepository.findByAlumnoIdOrderByFechaHoraDesc(alumnoId, pageable)
                : accesoRepository.findAllByOrderByFechaHoraDesc(pageable);
        return page.map(a -> toDTO(a, false));
    }

    @Transactional(readOnly = true)
    public AccesoDTO buscarPorId(@NonNull Long id) {
        Acceso acceso = accesoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Acceso no encontrado"));
        return toDTO(acceso, true);
    }

    @Transactional(readOnly = true)
    public List<AccesoDTO> listarHoy() {
        LocalDateTime inicio = LocalDate.now().atStartOfDay();
        LocalDateTime fin = LocalDate.now().atTime(LocalTime.MAX);
        return accesoRepository.findByFechaHoraBetweenOrderByFechaHoraDesc(inicio, fin)
                .stream()
                .map(a -> toDTO(a, false))
                .collect(Collectors.toList());
    }

    public long contarHoy() {
        LocalDateTime inicio = LocalDate.now().atStartOfDay();
        LocalDateTime fin = LocalDate.now().atTime(LocalTime.MAX);
        return accesoRepository.countByFechaHoraBetween(inicio, fin);
    }

    public long contarHoyPorTipo(TipoAcceso tipo) {
        LocalDateTime inicio = LocalDate.now().atStartOfDay();
        LocalDateTime fin = LocalDate.now().atTime(LocalTime.MAX);
        return accesoRepository.countByTipoAndFechaHoraBetween(tipo, inicio, fin);
    }

    private String obtenerUsuarioActual() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getPrincipal() == null) {
            return "sistema";
        }
        return auth.getPrincipal().toString();
    }

    private AccesoDTO toDTO(Acceso acceso, boolean incluirNotificaciones) {
        AccesoDTO dto = new AccesoDTO();
        dto.setId(acceso.getId());
        dto.setAlumnoId(acceso.getAlumno().getId());
        dto.setAlumnoNombre(acceso.getAlumno().getNombre());
        dto.setAlumnoCodigo(acceso.getAlumno().getCodigo());
        dto.setTipo(acceso.getTipo());
        dto.setFechaHora(acceso.getFechaHora());
        dto.setRegistradoPor(acceso.getRegistradoPor());
        dto.setObservacion(acceso.getObservacion());
        if (incluirNotificaciones) {
            dto.setNotificaciones(notificacionService.listarPorAcceso(acceso.getId()));
        }
        return dto;
    }

    private void validarRequest(AccesoRequestDTO request) {
        if (request.getTipo() == TipoAcceso.NO_ASISTIO_CON_PERMISO) {
            String obs = normalizarObservacion(request.getObservacion());
            if (obs == null || obs.isBlank()) {
                throw new BusinessException("Indique el motivo del permiso (observación)");
            }
        }
    }

    private String normalizarObservacion(String observacion) {
        if (observacion == null) {
            return null;
        }
        String trimmed = observacion.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
