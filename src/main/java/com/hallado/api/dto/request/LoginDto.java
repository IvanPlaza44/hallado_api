package com.hallado.api.dto.request;

import jakarta.validation.constraints.NotBlank;

public record LoginDto(
        @NotBlank String usernameOrEmail,
        @NotBlank String password
) {}
