package com.hallado.api.dto.request;

import jakarta.validation.constraints.NotBlank;

public record ReclamoRequestDto(
        @NotBlank String mensaje
) {}
