package com.hallado.api.controller;

import com.hallado.api.dto.response.ReclamoResponseDto;
import com.hallado.api.entity.Usuario;
import com.hallado.api.service.PublicacionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * "Mis reclamos" y foto del reclamo (Punto 7).
 * Crear, aceptar y rechazar siguen en PublicacionController (/api/publicaciones/{id}/reclamos/...).
 */
@RestController
@RequestMapping("/api/reclamos")
@RequiredArgsConstructor
public class ReclamoController {

    private final PublicacionService publicacionService;

    /** Reclamos que hice yo ("Es mío" / "Lo encontré"). */
    @GetMapping("/enviados")
    public ResponseEntity<List<ReclamoResponseDto>> enviados(@AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok(publicacionService.reclamosEnviados(usuario));
    }

    /** Reclamos que recibí sobre mis publicaciones (incluyen la respuesta de verificación). */
    @GetMapping("/recibidos")
    public ResponseEntity<List<ReclamoResponseDto>> recibidos(@AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok(publicacionService.reclamosRecibidos(usuario));
    }

    /** Foto que acredita el reclamo. Se llama justo después de crearlo. */
    @PostMapping(value = "/{id}/foto", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ReclamoResponseDto> subirFoto(@AuthenticationPrincipal Usuario usuario,
                                                         @PathVariable Long id,
                                                         @RequestParam("archivo") MultipartFile archivo) {
        return ResponseEntity.ok(publicacionService.subirFotoReclamo(usuario, id, archivo));
    }
}