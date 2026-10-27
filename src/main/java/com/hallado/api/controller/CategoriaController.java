package com.hallado.api.controller;

import com.hallado.api.enums.CategoriaItem;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Hallado tiene un listado cerrado de categorías de objetos (enum CategoriaItem),
 * a diferencia de Ronda donde eran una entidad editable. Este endpoint sólo expone
 * los valores fijos para que el frontend pueda armar el selector sin hardcodearlos.
 */
@RestController
@RequestMapping("/api/categorias")
public class CategoriaController {

    @GetMapping
    public ResponseEntity<CategoriaItem[]> listar() {
        return ResponseEntity.ok(CategoriaItem.values());
    }
}
