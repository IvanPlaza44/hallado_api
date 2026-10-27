package com.hallado.api.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "usuarios")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(unique = true)
    private String username;

    /** Null si el usuario solo se registró/logueó vía OTP y nunca definió contraseña */
    private String password;

    private String nombre;

    private String telefono;

    /** Si el teléfono se muestra a la otra parte una vez aceptado un reclamo */
    @Builder.Default
    private boolean telefonoVisible = false;

    private String barrio;

    private String fotoPerfil;

    @Builder.Default
    private boolean emailVerificado = false;

    @Builder.Default
    private LocalDateTime fechaAlta = LocalDateTime.now();
}
