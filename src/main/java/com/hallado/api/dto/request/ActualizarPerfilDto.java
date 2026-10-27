package com.hallado.api.dto.request;

public record ActualizarPerfilDto(
        String nombre,
        String telefono,
        Boolean telefonoVisible,
        String barrio,
        String username,
        String password
) {}
