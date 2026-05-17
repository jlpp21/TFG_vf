package com.example.fintechrecommender.security;

import com.example.fintechrecommender.model.Usuario;
import com.example.fintechrecommender.repository.UsuarioRepository;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.Optional;

/**
 * Filtro de Spring Security que valida el JWT en cada peticion.
 *
 * Se ejecuta una vez por peticion (OncePerRequestFilter) y, si encuentra
 * una cabecera "Authorization: Bearer ..." valida, carga el Usuario
 * correspondiente y lo deja en el SecurityContext para que el resto de
 * la aplicacion lo pueda obtener via Authentication. Si no hay token o
 * es invalido, simplemente deja pasar la peticion sin autenticar (las
 * reglas de SecurityConfig deciden despues si se permite o se rechaza).
 */
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UsuarioRepository usuarioRepository;

    /**
     * Construye el filtro con sus dependencias.
     *
     * @param jwtUtil           utilidad para validar y leer el JWT.
     * @param usuarioRepository repositorio para cargar el Usuario por correo.
     */
    public JwtAuthFilter(JwtUtil jwtUtil, UsuarioRepository usuarioRepository) {
        this.jwtUtil = jwtUtil;
        this.usuarioRepository = usuarioRepository;
    }

    /**
     * Logica del filtro: lee la cabecera Authorization, valida el token y
     * autentica al usuario en el SecurityContext si todo es correcto.
     *
     * @param request     peticion HTTP entrante.
     * @param response    respuesta HTTP.
     * @param filterChain cadena de filtros que continua el procesamiento.
     * @throws ServletException si falla algun filtro siguiente.
     * @throws IOException      si hay error de entrada/salida.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);

            if (jwtUtil.esTokenValido(token)) {
                String correo = jwtUtil.extraerCorreo(token);
                Optional<Usuario> usuarioOpt = usuarioRepository.findByCorreo(correo);

                if (usuarioOpt.isPresent() && SecurityContextHolder.getContext().getAuthentication() == null) {
                    Usuario usuario = usuarioOpt.get();
                    UsernamePasswordAuthenticationToken auth =
                            new UsernamePasswordAuthenticationToken(usuario, null, Collections.emptyList());
                    auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}
