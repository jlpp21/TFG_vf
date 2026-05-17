package com.example.fintechrecommender.dto;

/**
 * DTO de entrada para el segundo paso del flujo OAuth simulado del sandbox.
 *
 * Despues de enviar las credenciales, el frontend recibe un codigo
 * temporal y lo reenvia al backend con este DTO para confirmar la
 * conexion del banco. Imita el flujo "authorization code" de OAuth pero
 * todo es simulado (no hay banco real detras).
 */
public class SandboxCodigoRequest {

    /** Codigo temporal devuelto por el sandbox en el paso anterior. */
    private String codigo;

    /** @return codigo de autorizacion. */
    public String getCodigo() { return codigo; }
    /** @param codigo nuevo codigo a asignar. */
    public void setCodigo(String codigo) { this.codigo = codigo; }
}
