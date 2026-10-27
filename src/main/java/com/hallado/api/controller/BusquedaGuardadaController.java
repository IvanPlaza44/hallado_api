package com.hallado.api.controller;

import com.hallado.api.dto.request.BusquedaGuardadaRequestDto;
import com.hallado.api.dto.response.BusquedaGuardadaResponseDto;
import com.hallado.api.entity.Usuario;
import com.hallado.api.service.BusquedaGuardadaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/busquedas-guardadas")
@RequiredArgsConstructor
public class BusquedaGuardadaController {

    private final BusquedaGuardadaService busquedaGuardadaService;

    @PostMapping
    public ResponseEntity<BusquedaGuardadaResponseDto> crear(@AuthenticationPrincipal Usuario usuario,
                                                               @Valid @RequestBody BusquedaGuardadaRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(busquedaGuardadaService.crear(usuario, dto));
    }

    @GetMapping
    public ResponseEntity<List<BusquedaGuardadaResponseDto>> listar(@AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok(busquedaGuardadaService.listar(usuario));
    }

    @PatchMapping("/{id}/revisar")
    public ResponseEntity<Void> marcarRevisada(@AuthenticationPrincipal Usuario usuario, @PathVariable Long id) {
        busquedaGuardadaService.marcarRevisada(usuario, id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@AuthenticationPrincipal Usuario usuario, @PathVariable Long id) {
        busquedaGuardadaService.eliminar(usuario, id);
        return ResponseEntity.noContent().build();
    }
}
