package com.example.fintechrecommender.controller;

import com.example.fintechrecommender.dto.BancoCatalogoDto;
import com.example.fintechrecommender.service.CatalogoBancosService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST que expone el catalogo de bancos disponibles para conectar.
 *
 * Bajo el prefijo /api/catalogo, devuelve la lista estatica de bancos que
 * el frontend muestra en la pantalla de "conectar banco". No depende del
 * usuario autenticado (es informacion publica).
 */
@RestController
@RequestMapping("/api/catalogo")
public class CatalogoBancosController {

    private final CatalogoBancosService catalogo;

    /**
     * Construye el controlador con el servicio del catalogo.
     *
     * @param catalogo servicio que provee la lista de bancos disponibles.
     */
    public CatalogoBancosController(CatalogoBancosService catalogo) {
        this.catalogo = catalogo;
    }

    /**
     * Devuelve la lista de bancos disponibles en el catalogo.
     *
     * @return 200 OK con la lista de BancoCatalogoDto.
     */
    @GetMapping("/bancos")
    public ResponseEntity<List<BancoCatalogoDto>> listar() {
        return ResponseEntity.ok(catalogo.listar());
    }
}
