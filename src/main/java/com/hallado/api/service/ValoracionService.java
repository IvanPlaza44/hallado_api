package com.hallado.api.service;

import com.hallado.api.dto.request.ValoracionRequestDto;
import com.hallado.api.entity.Publicacion;
import com.hallado.api.entity.Reclamo;
import com.hallado.api.entity.Usuario;
import com.hallado.api.entity.Valoracion;
import com.hallado.api.enums.EstadoPublicacion;
import com.hallado.api.enums.EstadoReclamo;
import com.hallado.api.exception.ApiException;
import com.hallado.api.repository.PublicacionRepository;
import com.hallado.api.repository.ReclamoRepository;
import com.hallado.api.repository.ValoracionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Permite calificar (positiva/negativa) a la otra parte de una devolución, una vez
 * que la publicación quedó RESUELTA. Sólo el publicador y el reclamante aceptado
 * pueden calificarse entre sí, y cada uno puede dejar como máximo una valoración
 * por publicación hacia la otra parte.
 */
@Service
@RequiredArgsConstructor
public class ValoracionService {

    private final ValoracionRepository valoracionRepository;
    private final PublicacionRepository publicacionRepository;
    private final ReclamoRepository reclamoRepository;

    @Transactional
    public void calificar(Usuario emisor, ValoracionRequestDto dto) {
        if (dto.publicacionId() == null) {
            throw ApiException.solicitudInvalida("Debe indicar la publicación");
        }
        Publicacion p = publicacionRepository.findById(dto.publicacionId())
                .orElseThrow(() -> ApiException.noEncontrado("Publicación no encontrada"));

        if (p.getEstado() != EstadoPublicacion.RESUELTA) {
            throw ApiException.solicitudInvalida("Sólo se puede calificar una devolución ya resuelta");
        }

        Usuario receptor = determinarContraparte(p, emisor);

        boolean yaCalifico = valoracionRepository.findByReceptor(receptor).stream()
                .anyMatch(v -> v.getPublicacion().getId().equals(p.getId()) && v.getEmisor().getId().equals(emisor.getId()));
        if (yaCalifico) {
            throw ApiException.conflicto("Ya calificaste esta devolución");
        }

        Valoracion valoracion = Valoracion.builder()
                .emisor(emisor)
                .receptor(receptor)
                .publicacion(p)
                .positiva(dto.positiva())
                .comentario(dto.comentario())
                .build();
        valoracionRepository.save(valoracion);
    }

    private Usuario determinarContraparte(Publicacion p, Usuario emisor) {
        if (p.getPublicador().getId().equals(emisor.getId())) {
            Reclamo aceptado = reclamoRepository.findByPublicacion(p).stream()
                    .filter(r -> r.getEstado() == EstadoReclamo.ACEPTADO)
                    .findFirst()
                    .orElseThrow(() -> ApiException.solicitudInvalida("Esta publicación no tiene un reclamo aceptado"));
            return aceptado.getReclamante();
        }

        Reclamo miReclamo = reclamoRepository.findByPublicacion(p).stream()
                .filter(r -> r.getReclamante().getId().equals(emisor.getId()) && r.getEstado() == EstadoReclamo.ACEPTADO)
                .findFirst()
                .orElseThrow(() -> ApiException.noAutorizado("No participaste en la devolución de esta publicación"));
        return p.getPublicador();
    }
}
