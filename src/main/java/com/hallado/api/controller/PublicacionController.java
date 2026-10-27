package com.hallado.api.controller;

import com.hallado.api.dto.request.PublicacionRequestDto;
import com.hallado.api.dto.request.ReclamoRequestDto;
import com.hallado.api.dto.response.PaginaDto;
import com.hallado.api.dto.response.PublicacionDetalleDto;
import com.hallado.api.dto.response.PublicacionResumenDto;
import com.hallado.api.dto.response.ReclamoResponseDto;
import com.hallado.api.entity.Usuario;
import com.hallado.api.enums.CategoriaItem;
import com.hallado.api.enums.OrdenResultados;
import com.hallado.api.enums.RangoFecha;
import com.hallado.api.enums.TipoPublicacion;
import com.hallado.api.service.PublicacionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/publicaciones")
@RequiredArgsConstructor
public class PublicacionController {

    private final PublicacionService publicacionService;

    // ---------- Búsqueda pública (no requiere login) ----------

    @GetMapping
    public ResponseEntity<PaginaDto<PublicacionResumenDto>> buscar(
            @RequestParam(required = false) TipoPublicacion tipo,
            @RequestParam(required = false) CategoriaItem categoria,
            @RequestParam(required = false) String palabrasClave,
            @RequestParam(required = false) String barrio,
            @RequestParam(required = false) RangoFecha rango,
            @RequestParam(required = false) LocalDate fechaDesde,
            @RequestParam(required = false) LocalDate fechaHasta,
            @RequestParam(required = false, defaultValue = "RECIENTES") OrdenResultados orden,
            @RequestParam(required = false, defaultValue = "0") int pagina,
            @RequestParam(required = false, defaultValue = "20") int tamanio) {

        return ResponseEntity.ok(publicacionService.buscar(
                tipo, categoria, palabrasClave, barrio, rango, fechaDesde, fechaHasta, orden, pagina, tamanio));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PublicacionDetalleDto> obtenerDetalle(@PathVariable Long id,
                                                                  @AuthenticationPrincipal Usuario usuario) {
        // usuario puede ser null: este endpoint es público, pero si viene logueado
        // se calculan esPropia/siguiendo/miReclamoEstado para esa persona.
        return ResponseEntity.ok(publicacionService.obtenerDetalle(id, usuario));
    }

    // ---------- Publicar (requiere login) ----------

    @PostMapping
    public ResponseEntity<PublicacionDetalleDto> crear(@AuthenticationPrincipal Usuario usuario,
                                                         @RequestBody PublicacionRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(publicacionService.crear(usuario, dto, usuario));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PublicacionDetalleDto> actualizar(@AuthenticationPrincipal Usuario usuario,
                                                              @PathVariable Long id,
                                                              @RequestBody PublicacionRequestDto dto) {
        return ResponseEntity.ok(publicacionService.actualizar(usuario, id, dto));
    }

    @PostMapping(value = "/{id}/fotos", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PublicacionDetalleDto> agregarFoto(@AuthenticationPrincipal Usuario usuario,
                                                               @PathVariable Long id,
                                                               @RequestParam("archivo") MultipartFile archivo) {
        return ResponseEntity.ok(publicacionService.agregarFoto(usuario, id, archivo));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@AuthenticationPrincipal Usuario usuario, @PathVariable Long id) {
        publicacionService.eliminar(usuario, id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/mias")
    public ResponseEntity<List<PublicacionResumenDto>> misPublicaciones(@AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok(publicacionService.misPublicaciones(usuario));
    }

    // ---------- Reclamos ----------

    @PostMapping("/{id}/reclamos")
    public ResponseEntity<ReclamoResponseDto> reclamar(@AuthenticationPrincipal Usuario usuario,
                                                         @PathVariable Long id,
                                                         @Valid @RequestBody ReclamoRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(publicacionService.reclamar(usuario, id, dto));
    }

    @GetMapping("/{id}/reclamos")
    public ResponseEntity<List<ReclamoResponseDto>> listarReclamos(@AuthenticationPrincipal Usuario usuario,
                                                                     @PathVariable Long id) {
        return ResponseEntity.ok(publicacionService.listarReclamos(usuario, id));
    }

    @PatchMapping("/{id}/reclamos/{reclamoId}/aceptar")
    public ResponseEntity<Void> aceptarReclamo(@AuthenticationPrincipal Usuario usuario,
                                                @PathVariable Long id, @PathVariable Long reclamoId) {
        publicacionService.aceptarReclamo(usuario, id, reclamoId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/reclamos/{reclamoId}/rechazar")
    public ResponseEntity<Void> rechazarReclamo(@AuthenticationPrincipal Usuario usuario,
                                                 @PathVariable Long id, @PathVariable Long reclamoId) {
        publicacionService.rechazarReclamo(usuario, id, reclamoId);
        return ResponseEntity.noContent().build();
    }

    // ---------- Seguir ----------

    @PostMapping("/{id}/seguir")
    public ResponseEntity<Void> seguir(@AuthenticationPrincipal Usuario usuario, @PathVariable Long id) {
        publicacionService.seguir(usuario, id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}/seguir")
    public ResponseEntity<Void> dejarDeSeguir(@AuthenticationPrincipal Usuario usuario, @PathVariable Long id) {
        publicacionService.dejarDeSeguir(usuario, id);
        return ResponseEntity.noContent().build();
    }
}
