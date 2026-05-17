package com.example.fintechrecommender.controller;

import com.example.fintechrecommender.dto.CuentaMockDto;
import com.example.fintechrecommender.dto.SandboxCodigoRequest;
import com.example.fintechrecommender.dto.SandboxCredencialesRequest;
import com.example.fintechrecommender.model.Usuario;
import com.example.fintechrecommender.service.SandboxBancoService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controlador REST para el flujo OAuth simulado del sandbox de bancos,
 * bajo /api/sandbox.
 *
 * Imita los pasos de un acceso de banca electronica:
 * - POST /credenciales: validar usuario y password.
 * - POST /sca: validar el codigo SCA (autenticacion fuerte) recibido por SMS.
 * - GET /cuentas/{bancoCodigo}: previsualizar las cuentas mock del banco.
 *
 * Toda la validacion es ficticia (acepta cualquier credencial). Sirve solo
 * como demo del flujo antes de persistir la conexion real.
 */
@RestController
@RequestMapping("/api/sandbox")
public class SandboxBancoController {

    private final SandboxBancoService sandbox;

    /**
     * Construye el controlador con el servicio del sandbox.
     *
     * @param sandbox servicio que implementa la simulacion del banco.
     */
    public SandboxBancoController(SandboxBancoService sandbox) {
        this.sandbox = sandbox;
    }

    /**
     * Valida las credenciales simuladas que envia el frontend.
     *
     * @param req DTO con usuario y password del banco simulado.
     * @return 200 OK con {ok: true} si pasan la validacion;
     *         400 Bad Request con {error} si no.
     */
    @PostMapping("/credenciales")
    public ResponseEntity<?> validarCredenciales(@RequestBody SandboxCredencialesRequest req) {
        try {
            sandbox.validarCredenciales(req.getUsuario(), req.getPassword());
            return ok();
        } catch (IllegalArgumentException e) {
            return error(e.getMessage());
        }
    }

    /**
     * Valida el codigo SCA (segundo paso de autenticacion) simulado.
     *
     * @param req DTO con el codigo enviado por el usuario.
     * @return 200 OK con {ok: true} si el codigo es valido;
     *         400 Bad Request con {error} si no.
     */
    @PostMapping("/sca")
    public ResponseEntity<?> validarSCA(@RequestBody SandboxCodigoRequest req) {
        try {
            sandbox.validarCodigoSCA(req.getCodigo());
            return ok();
        } catch (IllegalArgumentException e) {
            return error(e.getMessage());
        }
    }

    /**
     * Devuelve la lista de cuentas mock generadas para previsualizar
     * antes de confirmar la conexion del banco.
     *
     * @param usuario     usuario autenticado.
     * @param bancoCodigo codigo del banco para el que se generan las cuentas.
     * @return 200 OK con la lista de CuentaMockDto;
     *         400 Bad Request con {error} si el codigo de banco no existe.
     */
    @GetMapping("/cuentas/{bancoCodigo}")
    public ResponseEntity<?> cuentasMock(@AuthenticationPrincipal Usuario usuario,
                                         @PathVariable String bancoCodigo) {
        try {
            List<CuentaMockDto> cuentas = sandbox.cuentasMock(usuario, bancoCodigo);
            return ResponseEntity.ok(cuentas);
        } catch (IllegalArgumentException e) {
            return error(e.getMessage());
        }
    }

    /**
     * Helper que envuelve un cuerpo de exito {ok: true}.
     *
     * @return ResponseEntity 200 OK con el mapa.
     */
    private ResponseEntity<Map<String, Boolean>> ok() {
        Map<String, Boolean> body = new HashMap<>();
        body.put("ok", true);
        return ResponseEntity.ok(body);
    }

    /**
     * Helper que devuelve una respuesta 400 con {error: msg}.
     *
     * @param msg mensaje de error.
     * @return ResponseEntity 400 Bad Request con el mapa.
     */
    private ResponseEntity<Map<String, String>> error(String msg) {
        Map<String, String> body = new HashMap<>();
        body.put("error", msg);
        return ResponseEntity.badRequest().body(body);
    }
}
