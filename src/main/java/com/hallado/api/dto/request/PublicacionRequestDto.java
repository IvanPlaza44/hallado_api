package com.hallado.api.dto.request;

import com.hallado.api.enums.CategoriaItem;
import com.hallado.api.enums.FranjaHoraria;
import com.hallado.api.enums.TipoPublicacion;
import java.time.LocalTime;

import java.time.LocalDate;

/**
 * Todos los campos son opcionales a nivel DTO porque se usa tanto para crear como para
 * ir completando la publicación por partes (igual que "guardarPaso" en Ronda); el service
 * sólo pisa los campos no nulos. publicar() exige que estén los obligatorios.
 */
public record PublicacionRequestDto(
        TipoPublicacion tipo,
        CategoriaItem categoria,
        String titulo,
        String descripcion,
        String palabrasClave,
        LocalDate fechaSuceso,
        FranjaHoraria franjaHoraria,
        String barrio,
        Double latitud,
        Double longitud,
        String direccionExacta,
        LocalTime horaSuceso,
        String preguntaVerificacion,
        String respuestaVerificacion
) {}
