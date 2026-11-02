package com.hallado.api.entity;

import com.hallado.api.enums.EstadoPuntoEncuentro;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Punto de encuentro para devolver un objeto (Punto 8).
 * Lo propone quien tiene el objeto; la otra parte lo acepta o propone uno alternativo
 * (en ese caso el anterior queda REEMPLAZADO).
 */
@Entity
@Table(name = "puntos_encuentro")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PuntoEncuentro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reclamo_id")
    private Reclamo reclamo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "propuesto_por_id")
    private Usuario propuestoPor;

    @Column(length = 500)
    private String direccion;

    private Double latitud;
    private Double longitud;

    /** Día, horario o referencia ("sábado 11 h, en la puerta del súper"). */
    @Column(length = 500)
    private String indicaciones;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private EstadoPuntoEncuentro estado = EstadoPuntoEncuentro.PROPUESTO;

    @Builder.Default
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    private LocalDateTime fechaRespuesta;
}