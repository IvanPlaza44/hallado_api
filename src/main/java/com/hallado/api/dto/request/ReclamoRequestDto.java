package com.hallado.api.dto.request;

import jakarta.validation.constraints.Size;

/**
 * "Es mío":      respuestaVerificacion obligatoria (si la publicación tiene pregunta), mensaje opcional.
 * "Lo encontré": barrio obligatorio, mensaje opcional.
 * La foto se sube aparte con POST /api/reclamos/{id}/foto.
 */
public record ReclamoRequestDto(
        @Size(max = 1000) String mensaje,
        @Size(max = 500) String respuestaVerificacion,
        @Size(max = 255) String barrio
) {}