package com.hallado.api.dto.request;

public record ValoracionRequestDto(
        boolean positiva,
        String comentario,
        Long publicacionId
) {}
