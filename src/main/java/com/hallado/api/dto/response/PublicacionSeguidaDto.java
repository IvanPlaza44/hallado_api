package com.hallado.api.dto.response;

import com.hallado.api.enums.CategoriaItem;
import com.hallado.api.enums.EstadoPublicacion;
import com.hallado.api.enums.TipoPublicacion;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** Mismos campos que PublicacionResumenDto más el aviso de cambio de estado desde la última vista. */
public record PublicacionSeguidaDto(
        Long id,
        TipoPublicacion tipo,
        CategoriaItem categoria,
        String titulo,
        String barrio,
        LocalDate fechaSuceso,
        Double latitud,
        Double longitud,
        String fotoPrincipal,
        EstadoPublicacion estado,
        LocalDateTime fechaPublicacion,
        String publicadorNombre,
        boolean cambioEstado
) {}
