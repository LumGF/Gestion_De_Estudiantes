package com.colegio.gestionacceso.service;

import com.colegio.gestionacceso.dto.NotificacionDTO;
import com.colegio.gestionacceso.model.Acceso;
import com.colegio.gestionacceso.model.Alumno;
import com.colegio.gestionacceso.model.EstadoNotificacion;
import com.colegio.gestionacceso.model.Notificacion;
import com.colegio.gestionacceso.model.TipoAcceso;
import com.colegio.gestionacceso.repository.NotificacionRepository;
import com.colegio.gestionacceso.service.whatsapp.WhatsAppSendResult;
import com.colegio.gestionacceso.service.whatsapp.WhatsAppSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificacionService {

    private static final Logger log = LoggerFactory.getLogger(NotificacionService.class);
    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm");

    private final NotificacionRepository notificacionRepository;
    private final WhatsAppSender whatsAppSender;

    public NotificacionService(NotificacionRepository notificacionRepository, WhatsAppSender whatsAppSender) {
        this.notificacionRepository = notificacionRepository;
        this.whatsAppSender = whatsAppSender;
    }

    @Transactional
    public void notificarAcceso(Acceso acceso) {
        Alumno alumno = acceso.getAlumno();
        String mensaje = construirMensajeAcceso(acceso);
        List<String> telefonos = new ArrayList<>();
        if (tieneTelefono(alumno.getTelefonoPadre1())) {
            telefonos.add(alumno.getTelefonoPadre1());
        }
        if (tieneTelefono(alumno.getTelefonoPadre2())) {
            telefonos.add(alumno.getTelefonoPadre2());
        }

        for (String telefono : telefonos) {
            Notificacion notificacion = new Notificacion();
            notificacion.setAcceso(acceso);
            notificacion.setTelefono(telefono);
            notificacion.setMensaje(mensaje);
            notificacion.setEstado(EstadoNotificacion.PENDIENTE);
            notificacion.setIntentos(0);
            notificacion.setFechaCreacion(LocalDateTime.now());
            notificacionRepository.save(notificacion);
            procesarEnvio(notificacion);
        }

        if (telefonos.isEmpty()) {
            log.warn("Acceso {} sin teléfonos registrados para alumno {}", acceso.getId(), alumno.getId());
        }
    }

    @Transactional
    public void reintentarPendientes() {
        List<Notificacion> pendientes = notificacionRepository
                .findByEstadoOrderByFechaCreacionAsc(EstadoNotificacion.PENDIENTE);
        pendientes.forEach(n -> procesarEnvio(n));
    }

    @Transactional(readOnly = true)
    public Page<NotificacionDTO> listarLog(Pageable pageable) {
        return notificacionRepository.findAllByOrderByFechaCreacionDesc(pageable)
                .map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public List<NotificacionDTO> listarPorAcceso(Long accesoId) {
        return notificacionRepository.findByAccesoIdOrderByFechaCreacionDesc(accesoId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public long contarPorEstado(EstadoNotificacion estado) {
        return notificacionRepository.countByEstado(estado);
    }

    private void procesarEnvio(Notificacion notificacion) {
        if (!whatsAppSender.isEnabled()) {
            notificacion.setEstado(EstadoNotificacion.FALLIDO);
            notificacion.setError("WhatsApp deshabilitado");
            notificacionRepository.save(notificacion);
            return;
        }

        notificacion.setIntentos(notificacion.getIntentos() + 1);
        WhatsAppSendResult result = whatsAppSender.enviarMensaje(
                notificacion.getTelefono(),
                notificacion.getMensaje()
        );

        if (result.success()) {
            notificacion.setEstado(EstadoNotificacion.ENVIADO);
            notificacion.setFechaEnvio(LocalDateTime.now());
            notificacion.setError(null);
            log.info("Notificación {} enviada a {}", notificacion.getId(), notificacion.getTelefono());
        } else {
            notificacion.setEstado(EstadoNotificacion.FALLIDO);
            notificacion.setError(result.detail());
            log.warn("Notificación {} falló: {}", notificacion.getId(), result.detail());
        }
        notificacionRepository.save(notificacion);
    }

    private String construirMensajeAcceso(Acceso acceso) {
        Alumno alumno = acceso.getAlumno();
        String hora = acceso.getFechaHora().format(HORA);
        String accion = switch (acceso.getTipo()) {
            case ENTRADA -> "ingresó al colegio";
            case SALIDA -> "salió del colegio";
            case TARDANZA -> "llegó tarde al colegio";
            case NO_ASISTIO -> "NO ASISTIÓ al colegio el día de hoy (sin permiso registrado)";
            case NO_ASISTIO_CON_PERMISO -> "NO ASISTIÓ al colegio el día de hoy (con permiso autorizado)";
        };

        StringBuilder mensaje = new StringBuilder();
        mensaje.append("Colegio - Gestión de Acceso\n\n");
        mensaje.append("El alumno ").append(alumno.getNombre()).append(" (").append(alumno.getCodigo()).append(") ");
        mensaje.append(accion).append(".\n");
        mensaje.append("Registrado a las ").append(hora).append(".");

        if (acceso.getObservacion() != null && !acceso.getObservacion().isBlank()) {
            if (acceso.getTipo() == TipoAcceso.NO_ASISTIO_CON_PERMISO) {
                mensaje.append("\nMotivo del permiso: ").append(acceso.getObservacion()).append(".");
            } else if (acceso.getTipo() == TipoAcceso.TARDANZA) {
                mensaje.append("\nDetalle: ").append(acceso.getObservacion()).append(".");
            }
        }

        mensaje.append("\n\nEste es un mensaje automático del sistema de control de acceso.");
        return mensaje.toString();
    }

    private boolean tieneTelefono(String telefono) {
        return telefono != null && !telefono.isBlank();
    }

    private NotificacionDTO toDTO(Notificacion n) {
        NotificacionDTO dto = new NotificacionDTO();
        dto.setId(n.getId());
        dto.setAccesoId(n.getAcceso().getId());
        dto.setTelefono(n.getTelefono());
        dto.setMensaje(n.getMensaje());
        dto.setEstado(n.getEstado());
        dto.setIntentos(n.getIntentos());
        dto.setError(n.getError());
        dto.setFechaCreacion(n.getFechaCreacion());
        dto.setFechaEnvio(n.getFechaEnvio());
        dto.setAlumnoNombre(n.getAcceso().getAlumno().getNombre());
        return dto;
    }
}
