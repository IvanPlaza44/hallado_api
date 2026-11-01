package com.hallado.api.repository;

import com.hallado.api.entity.Publicacion;
import com.hallado.api.entity.Seguimiento;
import com.hallado.api.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SeguimientoRepository extends JpaRepository<Seguimiento, Long> {
    Optional<Seguimiento> findByUsuarioAndPublicacion(Usuario usuario, Publicacion publicacion);
    boolean existsByUsuarioAndPublicacion(Usuario usuario, Publicacion publicacion);
    long countByPublicacion(Publicacion publicacion);
    List<Seguimiento> findByUsuarioOrderByFechaCreacionDesc(Usuario usuario);
}
