package com.hallado.api.entity;

import com.hallado.api.enums.EstadoReclamo;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * "Es mío" (sobre una publicación de algo ENCONTRADO) o "Lo encontré" (sobre una
 * publicación de algo PERDIDO). El tipo de acción se infiere del tipo de la publicación,
 * no hace falta guardarlo aparte.
 */
@Entity
@Table(name = "reclamos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Reclamo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "publicacion_id")
    private Publicacion publicacion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reclamante_id")
    private Usuario reclamante;

    @Column(length = 1000)
    private String mensaje;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private EstadoReclamo estado = EstadoReclamo.PENDIENTE;

    @Builder.Default
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    private LocalDateTime fechaResolucion;
}
