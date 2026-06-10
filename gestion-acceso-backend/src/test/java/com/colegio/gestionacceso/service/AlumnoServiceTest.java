package com.colegio.gestionacceso.service;

import com.colegio.gestionacceso.dto.AlumnoDTO;
import com.colegio.gestionacceso.exception.BusinessException;
import com.colegio.gestionacceso.model.Alumno;
import com.colegio.gestionacceso.repository.AlumnoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AlumnoServiceTest {

    @Mock
    private AlumnoRepository alumnoRepository;

    @InjectMocks
    private AlumnoService alumnoService;

    @Test
    void guardar_normalizaTelefonoPeru() {
        AlumnoDTO dto = new AlumnoDTO();
        dto.setCodigo("A-001");
        dto.setNombre("Juan Pérez");
        dto.setDescripcion("5to A");
        dto.setDni("12345678");
        dto.setFechaRegistro(LocalDate.now());
        dto.setTelefonoPadre1("987654321");

        when(alumnoRepository.findByCodigo("A-001")).thenReturn(Optional.empty());
        when(alumnoRepository.findByDni("12345678")).thenReturn(Optional.empty());
        when(alumnoRepository.save(any(Alumno.class))).thenAnswer(inv -> {
            Alumno a = inv.getArgument(0);
            a.setId(1L);
            return a;
        });

        AlumnoDTO result = alumnoService.guardar(dto);

        assertEquals("51987654321", result.getTelefonoPadre1());
    }

    @Test
    void guardar_rechazaCodigoDuplicado() {
        AlumnoDTO dto = new AlumnoDTO();
        dto.setCodigo("A-001");
        dto.setDni("12345678");

        Alumno existing = new Alumno();
        existing.setId(99L);
        when(alumnoRepository.findByCodigo("A-001")).thenReturn(Optional.of(existing));

        assertThrows(BusinessException.class, () -> alumnoService.guardar(dto));
    }
}
