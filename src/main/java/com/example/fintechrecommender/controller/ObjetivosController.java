package com.example.fintechrecommender.controller;

import com.example.fintechrecommender.dto.ObjetivoUsuarioDto;
import com.example.fintechrecommender.model.Usuario;
import com.example.fintechrecommender.service.ObjetivoUsuarioService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controlador REST para gestionar los objetivos financieros del usuario,
 * bajo el prefijo /api/objetivos.
 *
 * Expone dos operaciones:
 * - GET para listar los objetivos actuales.
 * - PUT para reemplazarlos por completo (borra los antiguos y guarda los nuevos).
 */
@RestController
@RequestMapping("/api/objetivos")
public class ObjetivosController {

    private final ObjetivoUsuarioService service;

    /**
     * Construye el controlador con el servicio de objetivos.
     *
     * @param service servicio que implementa la logica de objetivos.
     */
    public ObjetivosController(ObjetivoUsuarioService service) {
        this.service = service;
    }

    /**
     * Lista los objetivos del usuario autenticado en orden de creacion.
     *
     * @param usuario usuario autenticado.
     * @return 200 OK con la lista de ObjetivoUsuarioDto.
     */
    @GetMapping
    public ResponseEntity<List<ObjetivoUsuarioDto>> listar(@AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok(service.listar(usuario));
    }

    /**
     * Reemplaza la lista de objetivos del usuario por la lista enviada.
     *
     * @param usuario  usuario autenticado.
     * @param entradas nueva lista completa de objetivos (codigo y plazo).
     * @return 200 OK con la lista persistida;
     *         400 Bad Request con {error} si algun codigo o plazo no es valido.
     */
    @PutMapping
    public ResponseEntity<?> reemplazar(@AuthenticationPrincipal Usuario usuario,
                                        @RequestBody List<ObjetivoUsuarioDto> entradas) {
        try {
            return ResponseEntity.ok(service.reemplazar(usuario, entradas));
        } catch (IllegalArgumentException e) {
            Map<String, String> err = new HashMap<>();
            err.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(err);
        }
    }
}
