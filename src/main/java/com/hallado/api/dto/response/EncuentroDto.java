package com.hallado.api.dto.response;

/**
 * Estado del acuerdo de devolución de un reclamo aceptado, visto por quien consulta.
 * punto es null si todavía nadie propuso. puedoProponer / puedoAceptar le dicen a la app
 * qué botones mostrar.
 * contraparteTelefono solo viene si la otra persona eligió mostrar su teléfono
 * una vez aceptado el reclamo (Punto 2); si no, es null.
 */
public record EncuentroDto(
        Long reclamoId,
        Long publicacionId,
        String publicacionTitulo,
        boolean tengoElObjeto,
        String contraparteNombre,
        String contraparteTelefono,
        PuntoEncuentroDto punto,
        boolean puedoProponer,
        boolean puedoAceptar
) {}