package com.hallado.api.repository;

import com.hallado.api.entity.Publicacion;
import com.hallado.api.entity.Usuario;
import com.hallado.api.entity.Valoracion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ValoracionRepository extends JpaRepository<Valoracion, Long> {
    List<Valoracion> findByReceptor(Usuario receptor);
    Optional<Valoracion> findFirstByEmisorAndPublicacion(Usuario emisor, Publicacion publicacion);
}
