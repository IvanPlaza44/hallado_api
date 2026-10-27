package com.hallado.api.dto.response;

import java.time.LocalDateTime;
import java.util.List;

/** Sin teléfono ni historial de publicaciones, tal como pide el enunciado. */
public record PerfilPublicoResponseDto(
        Long id,
        String nombre,
        String barrio,
        String fotoPerfil,
        LocalDateTime fechaAlta,
        long devolucionesConcretadas,
        long valoracionesPositivas,
        long valoracionesNegativas,
        List<String> insignias
) {}
