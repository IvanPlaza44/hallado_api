package com.hallado.api.entity;

import com.hallado.api.enums.PropositoOtp;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "codigos_otp")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CodigoOtp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String codigo;

    @Column(nullable = false)
    private LocalDateTime expiracion;

    @Builder.Default
    private boolean usado = false;

    /** Separa el código de "verificar registro/login" del de "recuperar contraseña",
     *  para que uno no sirva para la otra operación. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private PropositoOtp proposito = PropositoOtp.REGISTRO;
}
