package com.hallado.api.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/** Mensaje público que quien recuperó su objeto le deja a quien se lo devolvió. */
@Entity
@Table(name = "agradecimientos", uniqueConstraints = @UniqueConstraint(columnNames = {"autor_id", "publicacion_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Agradecimiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "autor_id")
    private Usuario autor;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "receptor_id")
    private Usuario receptor;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "publicacion_id")
    private Publicacion publicacion;

    @Column(nullable = false, length = 300)
    private String mensaje;

    @Builder.Default
    private LocalDateTime fechaCreacion = LocalDateTime.now();
}
