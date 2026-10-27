package com.hallado.api.controller;

import com.hallado.api.dto.request.ValoracionRequestDto;
import com.hallado.api.entity.Usuario;
import com.hallado.api.service.ValoracionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/valoraciones")
@RequiredArgsConstructor
public class ValoracionController {

    private final ValoracionService valoracionService;

    @PostMapping
    public ResponseEntity<Void> calificar(@AuthenticationPrincipal Usuario usuario,
                                           @Valid @RequestBody ValoracionRequestDto dto) {
        valoracionService.calificar(usuario, dto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
