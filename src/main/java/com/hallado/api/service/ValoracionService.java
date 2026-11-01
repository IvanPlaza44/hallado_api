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

import java.time.LocalDateTime;

/**
 * Permite calificar (positiva/negativa) a la otra parte de una devolución, una vez
 * que la publicación quedó RESUELTA. Sólo el publicador y el reclamante aceptado
 * pueden calificarse entre sí, cada uno puede dejar como máximo una valoración
 * por publicación hacia la otra parte, y sólo dentro de los 7 días posteriores a la devolución.
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

        Reclamo aceptado = reclamoRepository.findFirstByPublicacionAndEstado(p, EstadoReclamo.ACEPTADO)
                .orElseThrow(() -> ApiException.solicitudInvalida("Esta publicación no tiene un reclamo aceptado"));
        if (!HistorialService.participa(aceptado, emisor)) {
            throw ApiException.noAutorizado("No participaste en la devolución de esta publicación");
        }

        if (valoracionRepository.findFirstByEmisorAndPublicacion(emisor, p).isPresent()) {
            throw ApiException.conflicto("Ya calificaste esta devolución");
        }

        if (LocalDateTime.now().isAfter(HistorialService.fechaLimiteValoracion(aceptado))) {
            throw ApiException.solicitudInvalida(
                    "El plazo de " + HistorialService.PLAZO_VALORACION_DIAS + " días para valorar ya venció");
        }

        Valoracion valoracion = Valoracion.builder()
                .emisor(emisor)
                .receptor(HistorialService.contraparte(aceptado, emisor))
                .publicacion(p)
                .positiva(dto.positiva())
                .comentario(dto.comentario())
                .build();
        valoracionRepository.save(valoracion);
    }
}
