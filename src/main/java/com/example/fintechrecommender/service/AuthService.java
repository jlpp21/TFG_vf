package com.example.fintechrecommender.service;

import com.example.fintechrecommender.dto.AuthResponse;
import com.example.fintechrecommender.dto.LoginRequest;
import com.example.fintechrecommender.dto.RegistroRequest;
import com.example.fintechrecommender.model.Usuario;
import com.example.fintechrecommender.model.perfil.PerfilFinanciero;
import com.example.fintechrecommender.repository.UsuarioRepository;
import com.example.fintechrecommender.security.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Servicio que implementa la logica de registro y login de usuarios.
 *
 * Encapsula el hashing de contrasena con BCrypt y la generacion del JWT
 * tras una autenticacion correcta. Lo usa AuthController para resolver
 * los endpoints /api/auth/register y /api/auth/login.
 */
@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    /**
     * Construye el servicio con sus dependencias.
     *
     * @param usuarioRepository repositorio para guardar y buscar usuarios.
     * @param passwordEncoder   codificador BCrypt para la contrasena.
     * @param jwtUtil           utilidad para generar el JWT.
     */
    public AuthService(UsuarioRepository usuarioRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    /**
     * Registra un nuevo usuario: comprueba que el correo no este en uso,
     * hashea la contrasena, persiste el usuario y devuelve un AuthResponse
     * con el JWT recien generado.
     *
     * @param req DTO con los datos del registro.
     * @return AuthResponse con el JWT y los datos del usuario.
     * @throws IllegalArgumentException si el correo ya existe o el perfil financiero no es valido.
     */
    public AuthResponse registrar(RegistroRequest req) {
        if (usuarioRepository.existsByCorreo(req.getCorreo())) {
            throw new IllegalArgumentException("Ya existe un usuario con ese correo");
        }
        PerfilFinanciero perfil = parsearPerfil(req.getPerfilFinanciero());

        Usuario u = new Usuario();
        u.setNombre(req.getNombre());
        u.setCorreo(req.getCorreo());
        u.setPasswordHash(passwordEncoder.encode(req.getPassword()));
        u.setPerfilFinanciero(perfil);
        Usuario guardado = usuarioRepository.save(u);

        String token = jwtUtil.generarToken(guardado.getCorreo());
        return new AuthResponse(token, guardado.getId(), guardado.getNombre(),
                guardado.getCorreo(), guardado.getPerfilFinanciero().name());
    }

    /**
     * Valida las credenciales del login y emite un JWT si son correctas.
     *
     * @param req DTO con correo y contrasena.
     * @return AuthResponse con el JWT y los datos del usuario.
     * @throws IllegalArgumentException si el correo no existe o la contrasena no coincide.
     */
    public AuthResponse iniciarSesion(LoginRequest req) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findByCorreo(req.getCorreo());
        if (!usuarioOpt.isPresent()) {
            throw new IllegalArgumentException("Correo o contrasena incorrectos");
        }
        Usuario u = usuarioOpt.get();
        if (!passwordEncoder.matches(req.getPassword(), u.getPasswordHash())) {
            throw new IllegalArgumentException("Correo o contrasena incorrectos");
        }
        String token = jwtUtil.generarToken(u.getCorreo());
        return new AuthResponse(token, u.getId(), u.getNombre(), u.getCorreo(),
                u.getPerfilFinanciero() != null ? u.getPerfilFinanciero().name() : null);
    }

    /**
     * Convierte el String del perfil recibido del frontend en el enum PerfilFinanciero.
     *
     * @param valor texto del perfil (case-insensitive).
     * @return enum PerfilFinanciero correspondiente.
     * @throws IllegalArgumentException si el texto es null o no coincide con ningun valor del enum.
     */
    private PerfilFinanciero parsearPerfil(String valor) {
        if (valor == null) {
            throw new IllegalArgumentException("Perfil financiero requerido");
        }
        try {
            return PerfilFinanciero.valueOf(valor.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Perfil financiero no valido: " + valor);
        }
    }
}
