package com.hallado.api.service;

import com.hallado.api.dto.request.ActualizarPerfilDto;
import com.hallado.api.dto.response.PerfilPublicoResponseDto;
import com.hallado.api.dto.response.PerfilResponseDto;
import com.hallado.api.entity.Usuario;
import com.hallado.api.exception.ApiException;
import com.hallado.api.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final CloudinaryService cloudinaryService;
    private final ReputacionService reputacionService;

    public PerfilResponseDto obtenerPerfilPropio(Usuario usuario) {
        return new PerfilResponseDto(
                usuario.getId(), usuario.getNombre(), usuario.getEmail(), usuario.getUsername(),
                usuario.getTelefono(), usuario.isTelefonoVisible(), usuario.getBarrio(), usuario.getFotoPerfil(),
                usuario.getFechaAlta(),
                reputacionService.devolucionesConcretadas(usuario),
                reputacionService.valoracionesPositivas(usuario),
                reputacionService.valoracionesNegativas(usuario),
                reputacionService.insignias(usuario)
        );
    }

    @Transactional
    public PerfilResponseDto actualizarPerfil(Usuario usuario, ActualizarPerfilDto dto) {
        if (dto.nombre() != null) usuario.setNombre(dto.nombre());
        if (dto.telefono() != null) usuario.setTelefono(dto.telefono());
        if (dto.telefonoVisible() != null) usuario.setTelefonoVisible(dto.telefonoVisible());
        if (dto.barrio() != null) usuario.setBarrio(dto.barrio());
        if (dto.username() != null) {
            if (!dto.username().equals(usuario.getUsername()) && usuarioRepository.existsByUsername(dto.username())) {
                throw ApiException.conflicto("El nombre de usuario ya está en uso");
            }
            usuario.setUsername(dto.username());
        }
        if (dto.password() != null && !dto.password().isBlank()) {
            usuario.setPassword(passwordEncoder.encode(dto.password()));
        }
        usuarioRepository.save(usuario);
        return obtenerPerfilPropio(usuario);
    }

    @Transactional
    public PerfilResponseDto actualizarFotoPerfil(Usuario usuario, MultipartFile archivo) {
        if (archivo == null || archivo.isEmpty()) {
            throw ApiException.solicitudInvalida("Debe adjuntar una imagen");
        }
        String url = cloudinaryService.subirImagen(archivo, "hallado/perfiles");
        usuario.setFotoPerfil(url);
        usuarioRepository.save(usuario);
        return obtenerPerfilPropio(usuario);
    }

    public PerfilPublicoResponseDto obtenerPerfilPublico(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> ApiException.noEncontrado("Usuario no encontrado"));

        return new PerfilPublicoResponseDto(
                usuario.getId(), usuario.getNombre(), usuario.getBarrio(), usuario.getFotoPerfil(),
                usuario.getFechaAlta(),
                reputacionService.devolucionesConcretadas(usuario),
                reputacionService.valoracionesPositivas(usuario),
                reputacionService.valoracionesNegativas(usuario),
                reputacionService.insignias(usuario)
        );
    }
}
