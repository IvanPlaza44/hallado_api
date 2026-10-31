package com.hallado.api.entity;

import com.hallado.api.enums.CategoriaItem;
import com.hallado.api.enums.RangoFecha;
import com.hallado.api.enums.TipoPublicacion;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "busquedas_guardadas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BusquedaGuardada {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    private String nombre;

    @Enumerated(EnumType.STRING)
    private TipoPublicacion tipo;

    @Enumerated(EnumType.STRING)
    private CategoriaItem categoria;

    private String palabrasClave;
    private String barrio;

    @Enumerated(EnumType.STRING)
    private RangoFecha rangoFecha;

    private LocalDate fechaDesde;
    private LocalDate fechaHasta;

    @Builder.Default
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    /** Última vez que el usuario revisó esta alerta (para calcular novedades) */
    @Builder.Default
    private LocalDateTime ultimaRevision = LocalDateTime.now();

    /** false = pausada. El default de columna mantiene activas las alertas creadas antes de este campo. */
    @Builder.Default
    @ColumnDefault("1")
    @Column(nullable = false)
    private boolean activa = true;
}
