package com.hallado.api.repository;

import com.hallado.api.entity.BusquedaGuardada;
import com.hallado.api.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BusquedaGuardadaRepository extends JpaRepository<BusquedaGuardada, Long> {
    List<BusquedaGuardada> findByUsuario(Usuario usuario);
}
