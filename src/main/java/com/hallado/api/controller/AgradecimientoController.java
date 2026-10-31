package com.hallado.api.controller;

import com.hallado.api.dto.request.AgradecimientoRequestDto;
import com.hallado.api.dto.response.AgradecimientoResponseDto;
import com.hallado.api.entity.Usuario;
import com.hallado.api.service.AgradecimientoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/agradecimientos")
@RequiredArgsConstructor
public class AgradecimientoController {

    private final AgradecimientoService agradecimientoService;

    @PostMapping
    public ResponseEntity<AgradecimientoResponseDto> crear(@AuthenticationPrincipal Usuario usuario,
                                                           @RequestBody AgradecimientoRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(agradecimientoService.crear(usuario, dto));
    }
}
