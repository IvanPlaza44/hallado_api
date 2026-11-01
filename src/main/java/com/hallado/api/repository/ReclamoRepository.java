package com.hallado.api.repository;

import com.hallado.api.entity.Publicacion;
import com.hallado.api.entity.Reclamo;
import com.hallado.api.entity.Usuario;
import com.hallado.api.enums.EstadoReclamo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReclamoRepository extends JpaRepository<Reclamo, Long> {
    List<Reclamo> findByPublicacion(Publicacion publicacion);
    Optional<Reclamo> findByPublicacionAndReclamanteAndEstado(Publicacion publicacion, Usuario reclamante, EstadoReclamo estado);
    long countByReclamanteAndEstado(Usuario reclamante, EstadoReclamo estado);
    List<Reclamo> findByReclamanteAndEstado(Usuario reclamante, EstadoReclamo estado);
    List<Reclamo> findByPublicacion_PublicadorAndEstado(Usuario publicador, EstadoReclamo estado);
    Optional<Reclamo> findFirstByPublicacionAndEstado(Publicacion publicacion, EstadoReclamo estado);
        void deleteByPublicacion(Publicacion publicacion);
}
