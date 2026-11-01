package com.hallado.api.entity;

import com.hallado.api.enums.CategoriaItem;
import java.time.LocalTime;
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

    /** Hora exacta (o aproximada) del suceso */
    private LocalTime horaSuceso;

    /** Solo para ENCONTRADO: pregunta sobre un detalle que no se ve en las fotos */
    private String preguntaVerificacion;

    /** Respuesta correcta a la pregunta. Privada: solo la ve el publicador */
    private String respuestaVerificacion;

    /** A los 30 días de publicada pasa a VENCIDA, salvo que se renueve */
    private LocalDateTime fechaVencimiento;

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
