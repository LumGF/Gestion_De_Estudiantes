package com.colegio.gestionacceso.service;

import com.colegio.gestionacceso.dto.WhatsAppEnvioResultadoDTO;
import com.colegio.gestionacceso.dto.WhatsAppEstadoDTO;
import com.colegio.gestionacceso.dto.WhatsAppPruebaResponseDTO;
import com.colegio.gestionacceso.model.Alumno;
import com.colegio.gestionacceso.repository.AlumnoRepository;
import com.colegio.gestionacceso.service.whatsapp.WhatsAppSendResult;
import com.colegio.gestionacceso.service.whatsapp.WhatsAppSender;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class WhatsAppService {

    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm");

    private final AlumnoRepository alumnoRepository;
    private final WhatsAppSender whatsAppSender;

    public WhatsAppService(AlumnoRepository alumnoRepository, WhatsAppSender whatsAppSender) {
        this.alumnoRepository = alumnoRepository;
        this.whatsAppSender = whatsAppSender;
    }

    public WhatsAppEstadoDTO consultarEstado() {
        if (!whatsAppSender.isEnabled()) {
            return new WhatsAppEstadoDTO(false, false, "Integración WhatsApp deshabilitada");
        }
        var status = whatsAppSender.consultarEstado();
        return new WhatsAppEstadoDTO(true, status.ready(), status.message());
    }

    public Optional<WhatsAppPruebaResponseDTO> enviarPruebaAlumno(@NonNull Long alumnoId) {
        return alumnoRepository.findById(alumnoId).map(this::enviarPrueba);
    }

    private WhatsAppPruebaResponseDTO enviarPrueba(Alumno alumno) {
        String mensaje = construirMensajePrueba(alumno);
        List<WhatsAppEnvioResultadoDTO> resultados = new ArrayList<>();

        enviarSiTieneTelefono(resultados, alumno.getTelefonoPadre1(), mensaje);
        enviarSiTieneTelefono(resultados, alumno.getTelefonoPadre2(), mensaje);

        long enviados = resultados.stream().filter(WhatsAppEnvioResultadoDTO::isEnviado).count();

        WhatsAppPruebaResponseDTO response = new WhatsAppPruebaResponseDTO();
        response.setAlumnoId(alumno.getId());
        response.setAlumnoNombre(alumno.getNombre());
        response.setMensajeEnviado(mensaje);
        response.setResultados(resultados);
        response.setExito(enviados > 0);
        response.setResumen(resultados.isEmpty()
                ? "El alumno no tiene teléfonos registrados"
                : enviados + " de " + resultados.size() + " mensaje(s) enviado(s)");
        return response;
    }

    private void enviarSiTieneTelefono(
            List<WhatsAppEnvioResultadoDTO> resultados,
            String telefono,
            String mensaje
    ) {
        if (telefono == null || telefono.isBlank()) {
            return;
        }
        WhatsAppSendResult result = whatsAppSender.enviarMensaje(telefono, mensaje);
        resultados.add(new WhatsAppEnvioResultadoDTO(
                telefono,
                result.success(),
                result.detail()
        ));
    }

    private String construirMensajePrueba(Alumno alumno) {
        String hora = LocalDateTime.now().format(HORA);
        return "Prueba - Colegio Gestión de Acceso\n\n"
                + "Alumno: " + alumno.getNombre() + " (" + alumno.getCodigo() + ")\n"
                + "Hora: " + hora + "\n\n"
                + "Si recibe este mensaje, las notificaciones por WhatsApp están funcionando correctamente.";
    }
}
