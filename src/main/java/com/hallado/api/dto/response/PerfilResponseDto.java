package com.hallado.api.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record PerfilResponseDto(
        Long id,
        String nombre,
        String email,
        String username,
        String telefono,
        boolean telefonoVisible,
        String barrio,
        String fotoPerfil,
        LocalDateTime fechaAlta,
        long devolucionesConcretadas,
        long valoracionesPositivas,
        long valoracionesNegativas,
        List<String> insignias
) {}
