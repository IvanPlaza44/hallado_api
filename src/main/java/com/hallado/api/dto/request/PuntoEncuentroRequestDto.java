package com.hallado.api.dto.request;

import jakarta.validation.constraints.Size;

/** Hay que mandar una dirección, o la ubicación (latitud + longitud), o ambas. */
public record PuntoEncuentroRequestDto(
        @Size(max = 500) String direccion,
        Double latitud,
        Double longitud,
        @Size(max = 500) String indicaciones
) {}