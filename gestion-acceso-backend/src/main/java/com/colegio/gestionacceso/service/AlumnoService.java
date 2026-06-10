// Paquete renombrado: com.t2lguevara.backend.service → com.colegio.gestionacceso.service
package com.colegio.gestionacceso.service;

import com.colegio.gestionacceso.dto.AlumnoDTO;
import com.colegio.gestionacceso.exception.BusinessException;
import com.colegio.gestionacceso.exception.ResourceNotFoundException;
import com.colegio.gestionacceso.model.Alumno;
import com.colegio.gestionacceso.repository.AlumnoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AlumnoService {

    private final AlumnoRepository alumnoRepository;

    public AlumnoService(AlumnoRepository alumnoRepository) {
        this.alumnoRepository = alumnoRepository;
    }

    private AlumnoDTO toDTO(Alumno alumno) {
        AlumnoDTO dto = new AlumnoDTO();
        dto.setId(alumno.getId());
        dto.setCodigo(alumno.getCodigo());
        dto.setNombre(alumno.getNombre());
        dto.setDescripcion(alumno.getDescripcion());
        dto.setDni(alumno.getDni());
        dto.setFechaRegistro(alumno.getFechaRegistro());
        dto.setTelefonoPadre1(alumno.getTelefonoPadre1());
        dto.setTelefonoPadre2(alumno.getTelefonoPadre2());
        return dto;
    }

    private @NonNull Alumno toEntity(@NonNull AlumnoDTO dto) {
        Alumno alumno = new Alumno();
        alumno.setId(dto.getId());
        alumno.setCodigo(dto.getCodigo());
        alumno.setNombre(dto.getNombre());
        alumno.setDescripcion(dto.getDescripcion());
        alumno.setDni(dto.getDni());
        alumno.setFechaRegistro(dto.getFechaRegistro());
        alumno.setTelefonoPadre1(normalizarTelefonoPeru(dto.getTelefonoPadre1()));
        alumno.setTelefonoPadre2(normalizarTelefonoPeru(dto.getTelefonoPadre2()));
        return alumno;
    }

    private String normalizarTelefonoPeru(String telefono) {
        if (telefono == null || telefono.isBlank()) {
            return null;
        }
        String digits = telefono.replaceAll("\\D", "");
        if (digits.startsWith("51") && digits.length() == 11) {
            return digits;
        }
        if (digits.length() == 9) {
            return "51" + digits;
        }
        return digits;
    }

    public Page<AlumnoDTO> listar(Pageable pageable, String busqueda) {
        Page<Alumno> page = (busqueda != null && !busqueda.isBlank())
                ? alumnoRepository.buscar(busqueda.trim(), pageable)
                : alumnoRepository.findAll(pageable);
        return page.map(this::toDTO);
    }

    public Optional<AlumnoDTO> buscarPorId(@NonNull Long id) {
        return alumnoRepository.findById(id).map(this::toDTO);
    }

    public Optional<AlumnoDTO> buscarPorCodigo(String codigo) {
        return alumnoRepository.findByCodigo(codigo).map(this::toDTO);
    }

    public AlumnoDTO guardar(@NonNull AlumnoDTO dto) {
        validarUnicidad(dto, null);
        Alumno alumno = toEntity(dto);
        return toDTO(alumnoRepository.save(alumno));
    }

    public Optional<AlumnoDTO> actualizar(@NonNull Long id, @NonNull AlumnoDTO dto) {
        return alumnoRepository.findById(id).map(existing -> {
            validarUnicidad(dto, id);
            existing.setCodigo(dto.getCodigo());
            existing.setNombre(dto.getNombre());
            existing.setDescripcion(dto.getDescripcion());
            existing.setDni(dto.getDni());
            existing.setFechaRegistro(dto.getFechaRegistro());
            existing.setTelefonoPadre1(normalizarTelefonoPeru(dto.getTelefonoPadre1()));
            existing.setTelefonoPadre2(normalizarTelefonoPeru(dto.getTelefonoPadre2()));
            return toDTO(alumnoRepository.save(existing));
        });
    }

    public boolean eliminar(@NonNull Long id) {
        if (alumnoRepository.existsById(id)) {
            alumnoRepository.deleteById(id);
            return true;
        }
        return false;
    }

    private void validarUnicidad(AlumnoDTO dto, Long excludeId) {
        alumnoRepository.findByCodigo(dto.getCodigo()).ifPresent(existing -> {
            if (excludeId == null || !existing.getId().equals(excludeId)) {
                throw new BusinessException("Ya existe un alumno con el código " + dto.getCodigo());
            }
        });
        alumnoRepository.findByDni(dto.getDni()).ifPresent(existing -> {
            if (excludeId == null || !existing.getId().equals(excludeId)) {
                throw new BusinessException("Ya existe un alumno con el DNI " + dto.getDni());
            }
        });
    }

    public AlumnoDTO obtenerOError(@NonNull Long id) {
        return buscarPorId(id).orElseThrow(() -> new ResourceNotFoundException("Alumno no encontrado"));
    }
}
