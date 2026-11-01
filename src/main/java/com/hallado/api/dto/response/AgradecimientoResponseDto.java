package com.hallado.api.dto.response;

import java.time.LocalDateTime;

public record AgradecimientoResponseDto(
        Long id,
        String autorNombre,
        String mensaje,
        String publicacionTitulo,
        LocalDateTime fecha
) {}
