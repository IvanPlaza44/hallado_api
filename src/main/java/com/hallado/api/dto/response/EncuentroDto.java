package com.hallado.api.dto.response;

/**
 * Estado del acuerdo de devolución de un reclamo aceptado, visto por quien consulta.
 * punto es null si todavía nadie propuso. puedoProponer / puedoAceptar le dicen a la app
 * qué botones mostrar.
 */
public record EncuentroDto(
        Long reclamoId,
        Long publicacionId,
        String publicacionTitulo,
        boolean tengoElObjeto,
        String contraparteNombre,
        PuntoEncuentroDto punto,
        boolean puedoProponer,
        boolean puedoAceptar
) {}