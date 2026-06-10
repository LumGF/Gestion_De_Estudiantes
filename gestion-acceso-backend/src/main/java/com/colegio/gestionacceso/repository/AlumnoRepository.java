// Paquete renombrado: com.t2lguevara.backend.repository → com.colegio.gestionacceso.repository
package com.colegio.gestionacceso.repository;

import com.colegio.gestionacceso.model.Alumno;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AlumnoRepository extends JpaRepository<Alumno, Long> {

    Optional<Alumno> findByCodigo(String codigo);

    Optional<Alumno> findByDni(String dni);

    @Query("""
            SELECT a FROM Alumno a
            WHERE LOWER(a.nombre) LIKE LOWER(CONCAT('%', :q, '%'))
               OR LOWER(a.codigo) LIKE LOWER(CONCAT('%', :q, '%'))
               OR a.dni LIKE CONCAT('%', :q, '%')
            """)
    Page<Alumno> buscar(@Param("q") String q, Pageable pageable);
}
