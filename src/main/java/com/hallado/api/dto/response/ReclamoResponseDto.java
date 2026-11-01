package com.hallado.api.dto.response;

import com.hallado.api.enums.EstadoReclamo;
import com.hallado.api.enums.TipoPublicacion;

import java.time.LocalDateTime;

/**
 * respuestaVerificacion solo viene completa cuando lo mira el publicador.
 * fechaVencimiento = fechaCreacion + 48 h.
 */
public record ReclamoResponseDto(
        Long id,
        Long publicacionId,
        String publicacionTitulo,
        TipoPublicacion publicacionTipo,
        Long reclamanteId,
        String reclamanteNombre,
        double reclamanteReputacion,
        String mensaje,
        String respuestaVerificacion,
        String barrio,
        String fotoUrl,
        EstadoReclamo estado,
        LocalDateTime fechaCreacion,
        LocalDateTime fechaVencimiento
) {}