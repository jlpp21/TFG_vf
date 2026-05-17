package com.example.fintechrecommender.controller;

import com.example.fintechrecommender.dto.BancoDto;
import com.example.fintechrecommender.dto.ConectarBancoRequest;
import com.example.fintechrecommender.dto.MovimientoEntradaDto;
import com.example.fintechrecommender.model.Usuario;
import com.example.fintechrecommender.service.BancoService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controlador REST para gestionar las conexiones bancarias del usuario,
 * bajo el prefijo /api/bancos.
 *
 * Permite listar bancos conectados, conectar un banco nuevo, anyadir
 * movimientos a un banco existente y desconectarlo. Todas las operaciones
 * usan el Usuario autenticado como contexto, asi que un usuario solo puede
 * tocar sus propias conexiones.
 */
@RestController
@RequestMapping("/api/bancos")
public class BancoController {

    private final BancoService bancoService;

    /**
     * Construye el controlador con el servicio de bancos.
     *
     * @param bancoService servicio que implementa la logica de bancos conectados.
     */
    public BancoController(BancoService bancoService) {
        this.bancoService = bancoService;
    }

    /**
     * Lista todos los bancos conectados por el usuario autenticado.
     *
     * @param usuario usuario autenticado (lo inyecta Spring desde el JWT).
     * @return 200 OK con la lista de BancoDto del usuario (vacia si no tiene ninguno).
     */
    @GetMapping
    public ResponseEntity<List<BancoDto>> listar(@AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok(bancoService.listar(usuario));
    }

    /**
     * Conecta un nuevo banco al usuario autenticado y persiste sus movimientos iniciales.
     *
     * @param usuario usuario autenticado.
     * @param req     datos del banco a conectar (validados) y movimientos iniciales.
     * @return 200 OK con el BancoDto creado;
     *         400 Bad Request con {error} si el banco ya esta conectado o los datos no son validos.
     */
    @PostMapping
    public ResponseEntity<?> conectar(@AuthenticationPrincipal Usuario usuario,
                                      @Valid @RequestBody ConectarBancoRequest req) {
        try {
            BancoDto dto = bancoService.conectar(usuario, req);
            return ResponseEntity.ok(dto);
        } catch (IllegalArgumentException e) {
            Map<String, String> err = new HashMap<>();
            err.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(err);
        }
    }

    /**
     * Anyade movimientos adicionales a un banco ya conectado.
     *
     * @param usuario     usuario autenticado.
     * @param id          id de la conexion bancaria.
     * @param movimientos lista de movimientos a insertar.
     * @return 200 OK con {insertados: n} si todo va bien;
     *         404 Not Found con {error} si el banco no existe o no es del usuario.
     */
    @PostMapping("/{id}/movimientos")
    public ResponseEntity<?> agregarMovimientos(@AuthenticationPrincipal Usuario usuario,
                                                @PathVariable Long id,
                                                @RequestBody List<MovimientoEntradaDto> movimientos) {
        try {
            int insertados = bancoService.agregarMovimientos(usuario, id, movimientos);
            Map<String, Object> body = new HashMap<>();
            body.put("insertados", insertados);
            return ResponseEntity.ok(body);
        } catch (IllegalArgumentException e) {
            Map<String, String> err = new HashMap<>();
            err.put("error", e.getMessage());
            return ResponseEntity.status(404).body(err);
        }
    }

    /**
     * Desconecta un banco del usuario, borrando la conexion y sus movimientos asociados.
     *
     * @param usuario usuario autenticado.
     * @param id      id de la conexion bancaria a borrar.
     * @return 204 No Content si se borro correctamente;
     *         404 Not Found con {error} si la conexion no existe o no es del usuario.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> desconectar(@AuthenticationPrincipal Usuario usuario,
                                         @PathVariable Long id) {
        try {
            bancoService.desconectar(usuario, id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            Map<String, String> err = new HashMap<>();
            err.put("error", e.getMessage());
            return ResponseEntity.status(404).body(err);
        }
    }
}
