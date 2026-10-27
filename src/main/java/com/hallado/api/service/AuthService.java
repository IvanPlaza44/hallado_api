package com.hallado.api.service;

import com.hallado.api.dto.request.LoginDto;
import com.hallado.api.dto.request.OtpVerifyDto;
import com.hallado.api.dto.request.RestablecerPasswordDto;
import com.hallado.api.dto.response.AuthResponseDto;
import com.hallado.api.entity.Usuario;
import com.hallado.api.exception.ApiException;
import com.hallado.api.repository.UsuarioRepository;
import com.hallado.api.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final OtpService otpService;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    public void solicitarOtp(String email) {
        otpService.generarYEnviar(email);
    }

    public void reenviarOtp(String email) {
        otpService.reenviar(email);
    }

    @Transactional
    public AuthResponseDto verificarOtp(OtpVerifyDto dto) {
        otpService.verificar(dto.email(), dto.codigo());

        Usuario usuario = usuarioRepository.findByEmail(dto.email())
                .orElseGet(() -> usuarioRepository.save(Usuario.builder()
                        .email(dto.email())
                        .emailVerificado(true)
                        .build()));

        if (!usuario.isEmailVerificado()) {
            usuario.setEmailVerificado(true);
            usuarioRepository.save(usuario);
        }

        String token = jwtService.generarToken(usuario.getEmail(), usuario.getId());
        return new AuthResponseDto(token, usuario.getId(), usuario.getEmail(), usuario.getUsername());
    }

    public AuthResponseDto login(LoginDto dto) {
        Usuario usuario = usuarioRepository.findByUsername(dto.usernameOrEmail())
                .or(() -> usuarioRepository.findByEmail(dto.usernameOrEmail()))
                .orElseThrow(() -> new ApiException("Usuario o contraseña incorrectos", HttpStatus.UNAUTHORIZED));

        if (usuario.getPassword() == null || !passwordEncoder.matches(dto.password(), usuario.getPassword())) {
            throw new ApiException("Usuario o contraseña incorrectos", HttpStatus.UNAUTHORIZED);
        }

        String token = jwtService.generarToken(usuario.getEmail(), usuario.getId());
        return new AuthResponseDto(token, usuario.getId(), usuario.getEmail(), usuario.getUsername());
    }

    public void solicitarRecuperacion(String email) {
        // No revelamos si el email existe o no (evita enumerar usuarios registrados)
        if (usuarioRepository.existsByEmail(email)) {
            otpService.generarYEnviarRecuperacion(email);
        }
    }

    public void reenviarRecuperacion(String email) {
        if (usuarioRepository.existsByEmail(email)) {
            otpService.reenviarRecuperacion(email);
        }
    }

    @Transactional
    public void restablecerPassword(RestablecerPasswordDto dto) {
        otpService.verificarRecuperacion(dto.email(), dto.codigo());

        Usuario usuario = usuarioRepository.findByEmail(dto.email())
                .orElseThrow(() -> ApiException.noEncontrado("Usuario no encontrado"));

        usuario.setPassword(passwordEncoder.encode(dto.passwordNueva()));
        usuarioRepository.save(usuario);
    }

    @Transactional
    public void registrarUsuarioConPassword(LoginDto dto) {
        // Verificamos que no exista
        if (usuarioRepository.findByEmail(dto.usernameOrEmail()).isPresent()) {
            throw new ApiException("El usuario ya existe", HttpStatus.BAD_REQUEST);
        }
        
        Usuario nuevoUsuario = Usuario.builder()
                .email(dto.usernameOrEmail())
                .username(dto.usernameOrEmail()) // Usamos el email como username por ahora
                .password(passwordEncoder.encode(dto.password())) // Encriptamos la clave!
                .emailVerificado(true)
                .build();
                
        usuarioRepository.save(nuevoUsuario);
    }
}
