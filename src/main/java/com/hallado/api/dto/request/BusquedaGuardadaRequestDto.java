package com.hallado.api.dto.request;

import com.hallado.api.enums.CategoriaItem;
import com.hallado.api.enums.RangoFecha;
import com.hallado.api.enums.TipoPublicacion;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

public record BusquedaGuardadaRequestDto(
        @NotBlank String nombre,
        TipoPublicacion tipo,
        CategoriaItem categoria,
        String palabrasClave,
        String barrio,
        RangoFecha rangoFecha,
        LocalDate fechaDesde,
        LocalDate fechaHasta
) {}
