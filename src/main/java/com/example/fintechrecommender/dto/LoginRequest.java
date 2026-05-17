package com.example.fintechrecommender.dto;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

/**
 * DTO de entrada para el endpoint POST /api/auth/login.
 *
 * Lleva las credenciales (correo + password en claro) que el frontend envia
 * para autenticarse. Si todo es correcto, el backend responde con un
 * AuthResponse que contiene el JWT.
 *
 * Validaciones: ambos campos son obligatorios y el correo debe tener
 * formato valido.
 */
public class LoginRequest {

    /** Correo electronico del usuario. Obligatorio y con formato de email valido. */
    @NotBlank
    @Email
    private String correo;

    /** Contrasena en claro que el usuario introduce en el formulario. Obligatoria. */
    @NotBlank
    private String password;

    /** @return correo del usuario. */
    public String getCorreo() { return correo; }
    /** @param correo nuevo correo a asignar. */
    public void setCorreo(String correo) { this.correo = correo; }
    /** @return contrasena en claro. */
    public String getPassword() { return password; }
    /** @param password nueva contrasena a asignar. */
    public void setPassword(String password) { this.password = password; }
}
