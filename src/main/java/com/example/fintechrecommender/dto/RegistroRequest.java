package com.example.fintechrecommender.dto;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * DTO de entrada para el endpoint POST /api/auth/register.
 *
 * Lleva los datos que el formulario de registro envia al backend para
 * crear una cuenta nueva. El AuthService valida que el correo no este
 * ya en uso, hashea la password con BCrypt y persiste el Usuario.
 *
 * Validaciones:
 * - nombre: obligatorio, entre 3 y 100 caracteres.
 * - correo: obligatorio y con formato de email.
 * - password: obligatorio, entre 6 y 100 caracteres.
 * - perfilFinanciero: obligatorio (debe coincidir con un valor del enum PerfilFinanciero).
 */
public class RegistroRequest {

    /** Nombre completo del usuario. Entre 3 y 100 caracteres. */
    @NotBlank
    @Size(min = 3, max = 100)
    private String nombre;

    /** Correo electronico del usuario. Debe ser unico. */
    @NotBlank
    @Email
    private String correo;

    /** Contrasena en claro. Entre 6 y 100 caracteres. Se hashea antes de guardar. */
    @NotBlank
    @Size(min = 6, max = 100)
    private String password;

    /** Perfil financiero declarado por el usuario (ENDEUDADO_CRONICO, ENDEUDADO_AL_DIA, HOLGADO). */
    @NotBlank
    private String perfilFinanciero;

    /** @return nombre del usuario. */
    public String getNombre() { return nombre; }
    /** @param nombre nuevo nombre a asignar. */
    public void setNombre(String nombre) { this.nombre = nombre; }
    /** @return correo del usuario. */
    public String getCorreo() { return correo; }
    /** @param correo nuevo correo a asignar. */
    public void setCorreo(String correo) { this.correo = correo; }
    /** @return contrasena en claro. */
    public String getPassword() { return password; }
    /** @param password nueva contrasena a asignar. */
    public void setPassword(String password) { this.password = password; }
    /** @return perfil financiero como String (matchea con el enum PerfilFinanciero). */
    public String getPerfilFinanciero() { return perfilFinanciero; }
    /** @param perfilFinanciero nuevo perfil a asignar. */
    public void setPerfilFinanciero(String perfilFinanciero) { this.perfilFinanciero = perfilFinanciero; }
}
