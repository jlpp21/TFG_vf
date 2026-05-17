package com.example.fintechrecommender.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

/**
 * Utilidad para generar y validar tokens JWT.
 *
 * Encapsula la libreria JJWT para que el resto del codigo no tenga que
 * tocarla directamente. Lee la clave secreta y el tiempo de expiracion
 * desde application.properties (app.jwt.secret y app.jwt.expiration-ms).
 */
@Component
public class JwtUtil {

    /** Clave HMAC usada para firmar y validar los tokens. */
    private final Key clave;

    /** Tiempo de validez del token en milisegundos. */
    private final long expiracionMs;

    /**
     * Construye la utilidad leyendo la configuracion del JWT.
     *
     * @param secret       clave secreta en texto (se convierte a HMAC).
     * @param expiracionMs duracion del token en milisegundos.
     */
    public JwtUtil(@Value("${app.jwt.secret}") String secret,
                   @Value("${app.jwt.expiration-ms}") long expiracionMs) {
        this.clave = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expiracionMs = expiracionMs;
    }

    /**
     * Genera un nuevo token JWT firmado para un usuario.
     *
     * @param correo correo del usuario, que va como subject del token.
     * @return token JWT firmado y serializado en formato compacto.
     */
    public String generarToken(String correo) {
        Date ahora = new Date();
        Date expira = new Date(ahora.getTime() + expiracionMs);
        return Jwts.builder()
                .setSubject(correo)
                .setIssuedAt(ahora)
                .setExpiration(expira)
                .signWith(clave, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Extrae el correo (subject) de un token JWT ya validado.
     *
     * @param token token JWT.
     * @return correo del usuario incluido en el token.
     */
    public String extraerCorreo(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(clave)
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject();
    }

    /**
     * Comprueba si un token es valido (firma correcta y no caducado).
     *
     * @param token token JWT a validar.
     * @return true si el token es valido, false si esta mal firmado o caducado.
     */
    public boolean esTokenValido(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(clave).build().parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
