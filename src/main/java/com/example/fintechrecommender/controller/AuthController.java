package com.example.fintechrecommender.controller;

import com.example.fintechrecommender.dto.AuthResponse;
import com.example.fintechrecommender.dto.LoginRequest;
import com.example.fintechrecommender.dto.RegistroRequest;
import com.example.fintechrecommender.model.Usuario;
import com.example.fintechrecommender.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

/**
 * Controlador REST para los endpoints de autenticacion bajo /api/auth.
 *
 * Expone tres operaciones:
 * - POST /api/auth/register: registra un nuevo usuario.
 * - POST /api/auth/login: valida credenciales y devuelve el JWT.
 * - GET /api/auth/me: devuelve los datos basicos del usuario autenticado.
 *
 * Delegan la logica de negocio en AuthService.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    /**
     * Construye el controlador con el servicio de autenticacion.
     *
     * @param authService servicio que implementa registro y login.
     */
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Registra un nuevo usuario y devuelve el JWT junto con sus datos.
     *
     * @param req DTO validado con nombre, correo, password y perfil financiero.
     * @return 200 OK con AuthResponse si el registro ha ido bien;
     *         400 Bad Request con un objeto {error} si el correo ya existe o el perfil es invalido.
     */
    @PostMapping("/register")
    public ResponseEntity<?> registrar(@Valid @RequestBody RegistroRequest req) {
        try {
            AuthResponse resp = authService.registrar(req);
            return ResponseEntity.ok(resp);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(error(e.getMessage()));
        }
    }

    /**
     * Inicia sesion validando las credenciales y devuelve el JWT.
     *
     * @param req DTO validado con correo y password en claro.
     * @return 200 OK con AuthResponse si las credenciales son correctas;
     *         401 Unauthorized con {error} si el correo no existe o la password no coincide.
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest req) {
        try {
            AuthResponse resp = authService.iniciarSesion(req);
            return ResponseEntity.ok(resp);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(401).body(error(e.getMessage()));
        }
    }

    /**
     * Devuelve los datos del usuario autenticado actual a partir del JWT.
     *
     * @param authentication objeto Authentication que Spring inyecta y que contiene el Usuario como principal.
     * @return 200 OK con un mapa {id, nombre, correo} si esta autenticado;
     *         401 Unauthorized si no hay sesion o el principal no es un Usuario.
     */
    @GetMapping("/me")
    public ResponseEntity<?> me(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof Usuario)) {
            return ResponseEntity.status(401).body(error("No autenticado"));
        }
        Usuario u = (Usuario) authentication.getPrincipal();
        Map<String, Object> body = new HashMap<>();
        body.put("id", u.getId());
        body.put("nombre", u.getNombre());
        body.put("correo", u.getCorreo());
        return ResponseEntity.ok(body);
    }

    /**
     * Helper que envuelve un mensaje de error en un mapa {"error": mensaje}
     * para devolverlo como cuerpo JSON.
     *
     * @param mensaje texto del error.
     * @return mapa con la clave "error" y el mensaje.
     */
    private Map<String, String> error(String mensaje) {
        Map<String, String> m = new HashMap<>();
        m.put("error", mensaje);
        return m;
    }
}
