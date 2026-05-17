package com.example.fintechrecommender.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * Configuracion central de Spring Security para la aplicacion.
 *
 * Define:
 * - El codificador de contrasenas (BCrypt) que usa AuthService al registrar y validar.
 * - La configuracion CORS para que el frontend pueda llamar a la API.
 * - La cadena de filtros de seguridad: desactiva CSRF, hace la sesion stateless
 *   (autenticacion por JWT en cada peticion), declara los endpoints publicos
 *   (auth, swagger, ping/hello, OPTIONS) y exige autenticacion al resto de /api/**.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    /**
     * Construye la configuracion de seguridad inyectando el filtro JWT.
     *
     * @param jwtAuthFilter filtro que autentica las peticiones por JWT.
     */
    public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    /**
     * Bean del codificador de contrasenas. Usa BCrypt, que es el estandar
     * recomendado por Spring Security.
     *
     * @return un PasswordEncoder basado en BCrypt.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Bean con la configuracion CORS aplicada a todas las rutas.
     * Permite cualquier origen, los metodos HTTP comunes y todas las cabeceras.
     *
     * @return fuente de configuracion CORS para Spring Security.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration cfg = new CorsConfiguration();
        cfg.setAllowedOriginPatterns(Arrays.asList("*"));
        cfg.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        cfg.setAllowedHeaders(Arrays.asList("*"));
        cfg.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cfg);
        return source;
    }

    /**
     * Define la cadena de filtros de seguridad: desactiva CSRF (no lo
     * necesitamos al ser stateless con JWT), activa CORS, fija la sesion
     * a STATELESS, declara las rutas publicas y exige autenticacion en
     * el resto de /api/**. Anyade el JwtAuthFilter antes del filtro de
     * usuario/contrasena estandar de Spring.
     *
     * @param http objeto de configuracion HTTP de Spring Security.
     * @return cadena de filtros configurada.
     * @throws Exception si Spring no puede construir la cadena.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf().disable()
            .cors().and()
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .authorizeRequests()
                .antMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .antMatchers("/api/auth/**").permitAll()
                .antMatchers("/swagger-ui/**", "/v3/api-docs/**", "/v3/**", "/ping", "/hello").permitAll()
                .antMatchers("/api/**").authenticated()
                .anyRequest().permitAll()
            .and()
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
