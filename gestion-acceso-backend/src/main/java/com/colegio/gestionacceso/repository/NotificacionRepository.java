package com.colegio.gestionacceso.repository;

import com.colegio.gestionacceso.model.EstadoNotificacion;
import com.colegio.gestionacceso.model.Notificacion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificacionRepository extends JpaRepository<Notificacion, Long> {

    Page<Notificacion> findAllByOrderByFechaCreacionDesc(Pageable pageable);

    List<Notificacion> findByAccesoIdOrderByFechaCreacionDesc(Long accesoId);

    List<Notificacion> findByEstadoOrderByFechaCreacionAsc(EstadoNotificacion estado);

    long countByEstado(EstadoNotificacion estado);
}
