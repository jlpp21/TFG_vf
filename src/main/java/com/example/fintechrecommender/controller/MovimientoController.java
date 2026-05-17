package com.example.fintechrecommender.controller;

import com.example.fintechrecommender.dto.PageMovimientosDto;
import com.example.fintechrecommender.model.Usuario;
import com.example.fintechrecommender.service.MovimientoService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para listar los movimientos del usuario, bajo /api/movimientos.
 *
 * Expone un unico GET con paginacion y filtros opcionales (texto en
 * descripcion, banco concreto, fecha). Devuelve ademas un resumen de
 * balance total y desglose por tipo de cuenta.
 */
@RestController
@RequestMapping("/api/movimientos")
public class MovimientoController {

    private final MovimientoService movService;

    /**
     * Construye el controlador con el servicio de movimientos.
     *
     * @param movService servicio que implementa la consulta paginada.
     */
    public MovimientoController(MovimientoService movService) {
        this.movService = movService;
    }

    /**
     * Devuelve una pagina de movimientos del usuario autenticado, con filtros opcionales.
     * Aplica limites a size (entre 1 y 200) y page (no negativo).
     *
     * @param usuario     usuario autenticado.
     * @param page        numero de pagina (por defecto 0).
     * @param size        tamano de pagina (por defecto 25, max 200).
     * @param descripcion filtro opcional por texto contenido en la descripcion.
     * @param bancoId     filtro opcional por id de banco conectado.
     * @param fecha       filtro opcional por fecha (formato yyyy-MM-dd).
     * @return 200 OK con la pagina de movimientos y resumen de balance.
     */
    @GetMapping
    public ResponseEntity<PageMovimientosDto> listar(
            @AuthenticationPrincipal Usuario usuario,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "25") int size,
            @RequestParam(required = false) String descripcion,
            @RequestParam(required = false) Long bancoId,
            @RequestParam(required = false) String fecha) {

        if (size < 1) size = 25;
        if (size > 200) size = 200;
        if (page < 0) page = 0;

        return ResponseEntity.ok(movService.listarPaginado(usuario, page, size, descripcion, bancoId, fecha));
    }
}
