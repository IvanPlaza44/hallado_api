package com.hallado.api.dto.response;

import com.hallado.api.enums.EstadoReclamo;

import java.time.LocalDateTime;

public record ReclamoResponseDto(
        Long id,
        Long publicacionId,
        Long reclamanteId,
        String reclamanteNombre,
        double reclamanteReputacion,
        String mensaje,
        EstadoReclamo estado,
        LocalDateTime fechaCreacion
) {}
