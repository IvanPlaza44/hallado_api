package com.hallado.api.service;

import com.hallado.api.entity.Usuario;
import com.hallado.api.enums.EstadoPublicacion;
import com.hallado.api.enums.EstadoReclamo;
import com.hallado.api.repository.PublicacionRepository;
import com.hallado.api.repository.ReclamoRepository;
import com.hallado.api.repository.ValoracionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Calcula reputación e insignias al vuelo, sin persistirlas: evita datos
 * duplicados/desactualizados y es trivial de recalcular en cualquier momento.
 *
 * "Devolución concretada" = el usuario estuvo del lado que cerró una publicación resuelta:
 * la publicó (y alguien se la devolvió / la entregó) o tuvo el reclamo aceptado sobre ella.
 */
@Service
@RequiredArgsConstructor
public class ReputacionService {

    private static final long META_VECINO_SOLIDARIO = 5;

    private final PublicacionRepository publicacionRepository;
    private final ReclamoRepository reclamoRepository;
    private final ValoracionRepository valoracionRepository;

    public long devolucionesConcretadas(Usuario usuario) {
        long comoPublicador = publicacionRepository
                .findByPublicadorAndEstadoIn(usuario, List.of(EstadoPublicacion.RESUELTA))
                .size();
        long comoReclamante = reclamoRepository.countByReclamanteAndEstado(usuario, EstadoReclamo.ACEPTADO);
        return comoPublicador + comoReclamante;
    }

    public long valoracionesPositivas(Usuario usuario) {
        return valoracionRepository.findByReceptor(usuario).stream().filter(v -> v.isPositiva()).count();
    }

    public long valoracionesNegativas(Usuario usuario) {
        return valoracionRepository.findByReceptor(usuario).stream().filter(v -> !v.isPositiva()).count();
    }

    public List<String> insignias(Usuario usuario) {
        List<String> insignias = new ArrayList<>();
        if (devolucionesConcretadas(usuario) >= META_VECINO_SOLIDARIO) {
            insignias.add("Vecino solidario");
        }
        return insignias;
    }
}
