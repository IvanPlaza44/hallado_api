package com.hallado.api.controller;

import com.hallado.api.dto.request.ActualizarPerfilDto;
import com.hallado.api.dto.response.AgradecimientoResponseDto;
import com.hallado.api.dto.response.HistorialCasoDto;
import com.hallado.api.dto.response.PerfilPublicoResponseDto;
import com.hallado.api.dto.response.PerfilResponseDto;
import com.hallado.api.entity.Usuario;
import com.hallado.api.enums.CategoriaItem;
import com.hallado.api.enums.RolHistorial;
import com.hallado.api.enums.TipoPublicacion;
import com.hallado.api.service.AgradecimientoService;
import com.hallado.api.service.HistorialService;
import com.hallado.api.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final HistorialService historialService;
    private final AgradecimientoService agradecimientoService;

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

    @GetMapping("/me/historial")
    public ResponseEntity<List<HistorialCasoDto>> historial(
            @AuthenticationPrincipal Usuario usuario,
            @RequestParam(required = false) RolHistorial rol,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(required = false) TipoPublicacion tipo,
            @RequestParam(required = false) CategoriaItem categoria) {
        return ResponseEntity.ok(historialService.listar(usuario, rol, desde, hasta, tipo, categoria));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PerfilPublicoResponseDto> perfilPublico(@PathVariable Long id) {
        return ResponseEntity.ok(usuarioService.obtenerPerfilPublico(id));
    }

    @GetMapping("/{id}/agradecimientos")
    public ResponseEntity<List<AgradecimientoResponseDto>> agradecimientos(@PathVariable Long id) {
        return ResponseEntity.ok(agradecimientoService.listarRecibidos(id));
    }
}
