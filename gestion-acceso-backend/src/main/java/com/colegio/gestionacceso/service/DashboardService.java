package com.colegio.gestionacceso.service;

import com.colegio.gestionacceso.dto.DashboardDTO;
import com.colegio.gestionacceso.model.EstadoNotificacion;
import com.colegio.gestionacceso.model.TipoAcceso;
import com.colegio.gestionacceso.repository.AlumnoRepository;
import com.colegio.gestionacceso.service.whatsapp.WhatsAppSender;
import org.springframework.stereotype.Service;

@Service
public class DashboardService {

    private final AlumnoRepository alumnoRepository;
    private final AccesoService accesoService;
    private final NotificacionService notificacionService;
    private final WhatsAppSender whatsAppSender;

    public DashboardService(
            AlumnoRepository alumnoRepository,
            AccesoService accesoService,
            NotificacionService notificacionService,
            WhatsAppSender whatsAppSender
    ) {
        this.alumnoRepository = alumnoRepository;
        this.accesoService = accesoService;
        this.notificacionService = notificacionService;
        this.whatsAppSender = whatsAppSender;
    }

    public DashboardDTO obtenerResumen() {
        DashboardDTO dto = new DashboardDTO();
        dto.setTotalAlumnos(alumnoRepository.count());
        dto.setAccesosHoy(accesoService.contarHoy());
        dto.setEntradasHoy(accesoService.contarHoyPorTipo(TipoAcceso.ENTRADA));
        dto.setSalidasHoy(accesoService.contarHoyPorTipo(TipoAcceso.SALIDA));
        dto.setNoAsistioHoy(accesoService.contarHoyPorTipo(TipoAcceso.NO_ASISTIO));
        dto.setNoAsistioConPermisoHoy(accesoService.contarHoyPorTipo(TipoAcceso.NO_ASISTIO_CON_PERMISO));
        dto.setTardanzasHoy(accesoService.contarHoyPorTipo(TipoAcceso.TARDANZA));
        dto.setNotificacionesEnviadas(notificacionService.contarPorEstado(EstadoNotificacion.ENVIADO));
        dto.setNotificacionesFallidas(notificacionService.contarPorEstado(EstadoNotificacion.FALLIDO));

        if (whatsAppSender.isEnabled()) {
            var status = whatsAppSender.consultarEstado();
            dto.setWhatsAppListo(status.ready());
            dto.setWhatsAppMensaje(status.message());
        } else {
            dto.setWhatsAppListo(false);
            dto.setWhatsAppMensaje("WhatsApp deshabilitado");
        }
        return dto;
    }
}
