package com.hallado.api.repository;

import com.hallado.api.entity.PuntoEncuentro;
import com.hallado.api.entity.Reclamo;
import com.hallado.api.enums.EstadoPuntoEncuentro;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PuntoEncuentroRepository extends JpaRepository<PuntoEncuentro, Long> {
    /** El punto vigente: el último que no fue reemplazado. */
    Optional<PuntoEncuentro> findFirstByReclamoAndEstadoNotOrderByIdDesc(Reclamo reclamo, EstadoPuntoEncuentro estado);
}