package com.hallado.api.dto.response;

import com.hallado.api.enums.CategoriaItem;
import com.hallado.api.enums.RangoFecha;
import com.hallado.api.enums.TipoPublicacion;

import java.time.LocalDate;

public record BusquedaGuardadaResponseDto(
        Long id,
        String nombre,
        boolean hayNovedades,
        long cantidadNovedades,
        boolean activa,
        TipoPublicacion tipo,
        CategoriaItem categoria,
        String palabrasClave,
        String barrio,
        RangoFecha rangoFecha,
        LocalDate fechaDesde,
        LocalDate fechaHasta
) {}
