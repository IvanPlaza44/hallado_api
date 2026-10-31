package com.hallado.api.repository;

import com.hallado.api.entity.Agradecimiento;
import com.hallado.api.entity.Publicacion;
import com.hallado.api.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AgradecimientoRepository extends JpaRepository<Agradecimiento, Long> {
    Optional<Agradecimiento> findByAutorAndPublicacion(Usuario autor, Publicacion publicacion);
    boolean existsByAutorAndPublicacion(Usuario autor, Publicacion publicacion);
    List<Agradecimiento> findByReceptorOrderByFechaCreacionDesc(Usuario receptor);
}
