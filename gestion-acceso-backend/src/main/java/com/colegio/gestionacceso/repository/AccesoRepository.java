package com.colegio.gestionacceso.repository;

import com.colegio.gestionacceso.model.Acceso;
import com.colegio.gestionacceso.model.TipoAcceso;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AccesoRepository extends JpaRepository<Acceso, Long> {

    Page<Acceso> findByAlumnoIdOrderByFechaHoraDesc(Long alumnoId, Pageable pageable);

    Page<Acceso> findAllByOrderByFechaHoraDesc(Pageable pageable);

    List<Acceso> findByFechaHoraBetweenOrderByFechaHoraDesc(LocalDateTime desde, LocalDateTime hasta);

    long countByFechaHoraBetween(LocalDateTime desde, LocalDateTime hasta);

    long countByTipoAndFechaHoraBetween(TipoAcceso tipo, LocalDateTime desde, LocalDateTime hasta);
}
