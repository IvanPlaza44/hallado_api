package com.hallado.api.exception;

import org.springframework.http.HttpStatus;

public class ApiException extends RuntimeException {

    private final HttpStatus status;

    public ApiException(String mensaje, HttpStatus status) {
        super(mensaje);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public static ApiException noEncontrado(String mensaje) {
        return new ApiException(mensaje, HttpStatus.NOT_FOUND);
    }

    public static ApiException noAutorizado(String mensaje) {
        return new ApiException(mensaje, HttpStatus.FORBIDDEN);
    }

    public static ApiException solicitudInvalida(String mensaje) {
        return new ApiException(mensaje, HttpStatus.BAD_REQUEST);
    }

    public static ApiException conflicto(String mensaje) {
        return new ApiException(mensaje, HttpStatus.CONFLICT);
    }
}
