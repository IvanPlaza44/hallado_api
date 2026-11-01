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

    /** Un reclamo pendiente vence si nadie lo responde en este plazo. */
    public static final int HORAS_VENCIMIENTO = 48;

    /** Máximo de reclamos que una persona puede hacer sobre una misma publicación. */
    public static final int MAX_INTENTOS_POR_PUBLICACION = 3;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "publicacion_id")
    private Publicacion publicacion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reclamante_id")
    private Usuario reclamante;

    /** Descripción libre del reclamante (opcional). */
    @Column(length = 1000)
    private String mensaje;

    /** "Es mío": respuesta a la pregunta de verificación de la publicación. */
    @Column(length = 500)
    private String respuestaVerificacion;

    /** Foto que acredita el reclamo (opcional en "Es mío", obligatoria en "Lo encontré"). */
    private String fotoUrl;

    /** "Lo encontré": barrio donde se encontró el objeto o la mascota. */
    private String barrio;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private EstadoReclamo estado = EstadoReclamo.PENDIENTE;

    @Builder.Default
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    private LocalDateTime fechaResolucion;

    /** Fecha límite para que el publicador responda. Se calcula, no se guarda. */
    public LocalDateTime calcularVencimiento() {
        return fechaCreacion == null ? null : fechaCreacion.plusHours(HORAS_VENCIMIENTO);
    }

    /** true si sigue PENDIENTE pero ya pasó el plazo de 48 h. */
    public boolean estaVencido() {
        LocalDateTime vencimiento = calcularVencimiento();
        return estado == EstadoReclamo.PENDIENTE
                && vencimiento != null
                && LocalDateTime.now().isAfter(vencimiento);
    }
}