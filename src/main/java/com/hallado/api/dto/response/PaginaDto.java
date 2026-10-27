package com.hallado.api.dto.response;

import java.util.List;

public record PaginaDto<T>(
        List<T> contenido,
        int pagina,
        int tamanio,
        long totalElementos,
        int totalPaginas
) {}
