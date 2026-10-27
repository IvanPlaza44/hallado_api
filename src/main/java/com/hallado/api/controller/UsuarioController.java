package com.hallado.api.controller;

import com.hallado.api.dto.request.ActualizarPerfilDto;
import com.hallado.api.dto.response.PerfilPublicoResponseDto;
import com.hallado.api.dto.response.PerfilResponseDto;
import com.hallado.api.entity.Usuario;
import com.hallado.api.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    @GetMapping("/me")
    public ResponseEntity<PerfilResponseDto> perfilPropio(@AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok(usuarioService.obtenerPerfilPropio(usuario));
    }

    @PutMapping("/me")
    public ResponseEntity<PerfilResponseDto> actualizarPerfil(@AuthenticationPrincipal Usuario usuario,
                                                               @Valid @RequestBody ActualizarPerfilDto dto) {
        return ResponseEntity.ok(usuarioService.actualizarPerfil(usuario, dto));
    }

    @PostMapping(value = "/me/foto", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PerfilResponseDto> actualizarFoto(@AuthenticationPrincipal Usuario usuario,
                                                             @RequestParam("archivo") MultipartFile archivo) {
        return ResponseEntity.ok(usuarioService.actualizarFotoPerfil(usuario, archivo));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PerfilPublicoResponseDto> perfilPublico(@PathVariable Long id) {
        return ResponseEntity.ok(usuarioService.obtenerPerfilPublico(id));
    }
}
