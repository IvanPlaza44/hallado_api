package com.hallado.api.dto.response;

import com.hallado.api.enums.CategoriaItem;
import com.hallado.api.enums.RolHistorial;
import com.hallado.api.enums.TipoPublicacion;

import java.time.LocalDateTime;

/** Una devolución concretada vista desde el usuario logueado. */
public record HistorialCasoDto(
        Long publicacionId,
        RolHistorial rol,
        LocalDateTime fechaDevolucion,
        String titulo,
        TipoPublicacion tipo,
        CategoriaItem categoria,
        String fotoPrincipal,
        Contraparte contraparte,
        MiValoracion miValoracion,
        boolean yaValore,
        boolean puedeValorar,
        LocalDateTime fechaLimiteValoracion,
        MiAgradecimiento miAgradecimiento,
        boolean yaAgradeci,
        boolean puedeAgradecer
) {
    public record Contraparte(Long id, String nombre) {}

    public record MiValoracion(boolean positiva, String comentario) {}

    public record MiAgradecimiento(String mensaje) {}
}
