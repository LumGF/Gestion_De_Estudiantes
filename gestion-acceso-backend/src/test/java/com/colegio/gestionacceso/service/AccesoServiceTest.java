package com.colegio.gestionacceso.service;

import com.colegio.gestionacceso.dto.AccesoRequestDTO;
import com.colegio.gestionacceso.exception.ResourceNotFoundException;
import com.colegio.gestionacceso.model.Alumno;
import com.colegio.gestionacceso.model.TipoAcceso;
import com.colegio.gestionacceso.repository.AccesoRepository;
import com.colegio.gestionacceso.repository.AlumnoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccesoServiceTest {

    @Mock
    private AccesoRepository accesoRepository;
    @Mock
    private AlumnoRepository alumnoRepository;
    @Mock
    private NotificacionService notificacionService;

    @InjectMocks
    private AccesoService accesoService;

    @Test
    void registrar_lanzaErrorSiAlumnoNoExiste() {
        AccesoRequestDTO request = new AccesoRequestDTO();
        request.setAlumnoId(1L);
        request.setTipo(TipoAcceso.ENTRADA);

        when(alumnoRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> accesoService.registrar(request));
        verifyNoInteractions(notificacionService);
    }
}
