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
    public void verificarOtp(OtpVerifyDto dto) {
        otpService.verificar(dto.email(), dto.codigo());
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
        String email = dto.usernameOrEmail();
        if (usuarioRepository.findByEmail(email).isPresent()) {
            throw new ApiException("El usuario ya existe", HttpStatus.BAD_REQUEST);
        }
        if (!otpService.tieneVerificacionRegistroVigente(email)) {
            throw ApiException.solicitudInvalida("Tenés que verificar el correo antes de crear la cuenta");
        }

        Usuario nuevoUsuario = Usuario.builder()
                .email(email)
                .username(email)
                .password(passwordEncoder.encode(dto.password()))
                .emailVerificado(true)
                .build();

        usuarioRepository.save(nuevoUsuario);
    }
}
