package com.hallado.api.controller;

import com.hallado.api.dto.request.PuntoEncuentroRequestDto;
import com.hallado.api.dto.response.EncuentroDto;
import com.hallado.api.dto.response.ReclamoResponseDto;
import com.hallado.api.entity.Usuario;
import com.hallado.api.service.PublicacionService;
import com.hallado.api.service.PuntoEncuentroService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * "Mis reclamos", foto del reclamo (Punto 7) y punto de encuentro (Punto 8).
 * Crear, aceptar y rechazar reclamos siguen en PublicacionController (/api/publicaciones/{id}/reclamos/...).
 */
@RestController
@RequestMapping("/api/reclamos")
@RequiredArgsConstructor
public class ReclamoController {

    private final PublicacionService publicacionService;
    private final PuntoEncuentroService puntoEncuentroService;

    // ---------- Punto 7 ----------

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

    // ---------- Punto 8: punto de encuentro ----------

    /** Estado del acuerdo: punto vigente (o null) y qué puede hacer quien consulta. */
    @GetMapping("/{id}/punto-encuentro")
    public ResponseEntity<EncuentroDto> obtenerEncuentro(@AuthenticationPrincipal Usuario usuario,
                                                         @PathVariable Long id) {
        return ResponseEntity.ok(puntoEncuentroService.obtener(usuario, id));
    }

    /** Propone un punto (la primera vez, quien tiene el objeto) o una alternativa (la otra parte). */
    @PostMapping("/{id}/punto-encuentro")
    public ResponseEntity<EncuentroDto> proponerEncuentro(@AuthenticationPrincipal Usuario usuario,
                                                          @PathVariable Long id,
                                                          @Valid @RequestBody PuntoEncuentroRequestDto dto) {
        return ResponseEntity.ok(puntoEncuentroService.proponer(usuario, id, dto));
    }

    /** Acepta la propuesta de la otra parte. */
    @PatchMapping("/{id}/punto-encuentro/{puntoId}/aceptar")
    public ResponseEntity<EncuentroDto> aceptarEncuentro(@AuthenticationPrincipal Usuario usuario,
                                                         @PathVariable Long id,
                                                         @PathVariable Long puntoId) {
        return ResponseEntity.ok(puntoEncuentroService.aceptar(usuario, id, puntoId));
    }
}