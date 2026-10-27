package com.hallado.api.dto.response;

public record AuthResponseDto(
        String token,
        Long usuarioId,
        String email,
        String username
) {}
