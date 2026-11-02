package com.hallado.api.dto.response;

import com.hallado.api.enums.EstadoPuntoEncuentro;

import java.time.LocalDateTime;

public record PuntoEncuentroDto(
        Long id,
        String direccion,
        Double latitud,
        Double longitud,
        String indicaciones,
        EstadoPuntoEncuentro estado,
        boolean propuestoPorMi,
        String propuestoPorNombre,
        LocalDateTime fechaCreacion
) {}