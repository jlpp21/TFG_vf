package com.example.fintechrecommender.dto;

/**
 * DTO de respuesta para los endpoints de autenticacion (registro y login).
 *
 * Es lo que el backend devuelve al frontend cuando el usuario se registra
 * o inicia sesion correctamente. Lleva el token JWT que el cliente debe
 * guardar y enviar en cabecera Authorization en las siguientes peticiones,
 * junto con los datos basicos del usuario que el frontend muestra (nombre,
 * correo, perfil financiero).
 */
public class AuthResponse {

    /** Token JWT firmado que autentica al usuario en el resto de peticiones. */
    private String token;

    /** Tipo de token segun el estandar OAuth/JWT (siempre "Bearer"). */
    private String tipo = "Bearer";

    /** Identificador unico del usuario en la base de datos. */
    private Long id;

    /** Nombre completo del usuario. */
    private String nombre;

    /** Correo electronico del usuario (sirve como login). */
    private String correo;

    /** Perfil financiero del usuario (ENDEUDADO_CRONICO, ENDEUDADO_AL_DIA, HOLGADO). */
    private String perfilFinanciero;

    /**
     * Crea la respuesta de autenticacion lista para enviar al frontend.
     *
     * @param token            JWT generado por el backend.
     * @param id               id del usuario en base de datos.
     * @param nombre           nombre del usuario.
     * @param correo           correo del usuario.
     * @param perfilFinanciero perfil financiero asociado al usuario.
     */
    public AuthResponse(String token, Long id, String nombre, String correo, String perfilFinanciero) {
        this.token = token;
        this.id = id;
        this.nombre = nombre;
        this.correo = correo;
        this.perfilFinanciero = perfilFinanciero;
    }

    /** @return token JWT del usuario. */
    public String getToken() { return token; }
    /** @param token nuevo token JWT a asignar. */
    public void setToken(String token) { this.token = token; }
    /** @return tipo de token (siempre "Bearer"). */
    public String getTipo() { return tipo; }
    /** @param tipo tipo de token a asignar. */
    public void setTipo(String tipo) { this.tipo = tipo; }
    /** @return id del usuario. */
    public Long getId() { return id; }
    /** @param id nuevo id del usuario. */
    public void setId(Long id) { this.id = id; }
    /** @return nombre del usuario. */
    public String getNombre() { return nombre; }
    /** @param nombre nuevo nombre del usuario. */
    public void setNombre(String nombre) { this.nombre = nombre; }
    /** @return correo del usuario. */
    public String getCorreo() { return correo; }
    /** @param correo nuevo correo del usuario. */
    public void setCorreo(String correo) { this.correo = correo; }
    /** @return perfil financiero del usuario en formato String. */
    public String getPerfilFinanciero() { return perfilFinanciero; }
    /** @param perfilFinanciero nuevo perfil financiero a asignar. */
    public void setPerfilFinanciero(String perfilFinanciero) { this.perfilFinanciero = perfilFinanciero; }
}
