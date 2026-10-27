package com.hallado.api.dto.response;

import com.hallado.api.enums.CategoriaItem;
import com.hallado.api.enums.EstadoPublicacion;
import com.hallado.api.enums.EstadoReclamo;
import com.hallado.api.enums.FranjaHoraria;
import com.hallado.api.enums.TipoPublicacion;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record PublicacionDetalleDto(
        Long id,
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
        /** null salvo que el que mira sea el publicador, o sea el reclamante de un reclamo ACEPTADO */
        String direccionExacta,
        EstadoPublicacion estado,
        LocalDateTime fechaPublicacion,
        List<String> fotos,
        Long publicadorId,
        String publicadorNombre,
        double publicadorReputacion,
        boolean esPropia,
        boolean siguiendo,
        /** Estado del reclamo que el usuario logueado hizo sobre esta publicación, si hizo alguno */
        EstadoReclamo miReclamoEstado
) {}
