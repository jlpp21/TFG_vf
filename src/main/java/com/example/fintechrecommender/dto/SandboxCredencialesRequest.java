package com.example.fintechrecommender.dto;

/**
 * DTO de entrada para el primer paso del flujo OAuth simulado del sandbox.
 *
 * El frontend envia el "usuario" y "password" del banco simulado al backend.
 * En el sandbox no se valida nada real (cualquier credencial es aceptada);
 * el objetivo es imitar el flujo de banca electronica para probar la
 * integracion. Tras este paso, el backend devuelve un codigo temporal.
 */
public class SandboxCredencialesRequest {

    /** Usuario simulado que el cliente introduce en el formulario del sandbox. */
    private String usuario;

    /** Contrasena simulada del banco. */
    private String password;

    /** @return usuario simulado. */
    public String getUsuario() { return usuario; }
    /** @param usuario nuevo usuario a asignar. */
    public void setUsuario(String usuario) { this.usuario = usuario; }
    /** @return contrasena simulada. */
    public String getPassword() { return password; }
    /** @param password nueva contrasena a asignar. */
    public void setPassword(String password) { this.password = password; }
}
