package com.hallado.api.repository;

import com.hallado.api.entity.Publicacion;
import com.hallado.api.entity.Usuario;
import com.hallado.api.enums.EstadoPublicacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDateTime;
import java.util.List;

public interface PublicacionRepository extends JpaRepository<Publicacion, Long>, JpaSpecificationExecutor<Publicacion> {
    List<Publicacion> findByPublicadorAndEstadoIn(Usuario publicador, List<EstadoPublicacion> estados);
    List<Publicacion> findByEstadoAndFechaVencimientoBefore(EstadoPublicacion estado, LocalDateTime limite);
}
