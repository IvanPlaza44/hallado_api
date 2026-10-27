package com.hallado.api.entity;

import com.hallado.api.enums.CategoriaItem;
import com.hallado.api.enums.EstadoPublicacion;
import com.hallado.api.enums.FranjaHoraria;
import com.hallado.api.enums.TipoPublicacion;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "publicaciones")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Publicacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "publicador_id")
    private Usuario publicador;

    /** Perdí algo (PERDIDO) o encontré algo (ENCONTRADO) */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoPublicacion tipo;

    @Enumerated(EnumType.STRING)
    private CategoriaItem categoria;

    private String titulo;

    @Column(length = 2000)
    private String descripcion;

    /** Palabras clave opcionales para la búsqueda por texto */
    private String palabrasClave;

    private LocalDate fechaSuceso;

    @Enumerated(EnumType.STRING)
    private FranjaHoraria franjaHoraria;

    private String barrio;

    private Double latitud;
    private Double longitud;

    /** No se expone hasta que un reclamo sobre esta publicación sea aceptado */
    private String direccionExacta;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private EstadoPublicacion estado = EstadoPublicacion.ABIERTA;

    @Builder.Default
    private LocalDateTime fechaPublicacion = LocalDateTime.now();

    @Builder.Default
    private LocalDateTime fechaActualizacion = LocalDateTime.now();

    @OneToMany(mappedBy = "publicacion", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderColumn(name = "orden")
    @Builder.Default
    private List<Foto> fotos = new ArrayList<>();
}
