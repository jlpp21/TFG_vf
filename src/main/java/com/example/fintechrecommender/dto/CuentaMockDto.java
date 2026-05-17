package com.example.fintechrecommender.dto;

import java.math.BigDecimal;

/**
 * DTO que representa una cuenta simulada (mock) durante el flujo del sandbox.
 *
 * Cuando el usuario "se conecta" a un banco simulado, el backend genera una
 * o varias cuentas falsas con saldo y movimientos que el frontend muestra
 * para previsualizar antes de confirmar la conexion. Este DTO es uno de
 * esos elementos de la previsualizacion.
 */
public class CuentaMockDto {

    /** Tipo tecnico de la cuenta: CORRIENTE | AHORRO | CREDITO. */
    private String tipoCuenta;

    /** Texto humano para mostrar el tipo de cuenta en la UI. */
    private String tipoLabel;

    /** IBAN o numero de tarjeta enmascarado. */
    private String numero;

    /** Saldo actual de la cuenta simulada. */
    private BigDecimal saldo;

    /** Descripcion adicional de la cuenta para mostrar al usuario. */
    private String descripcion;

    /** Constructor vacio requerido para serializacion. */
    public CuentaMockDto() {}

    /**
     * Crea una cuenta simulada con todos sus campos.
     *
     * @param tipoCuenta  tipo tecnico (CORRIENTE, AHORRO, CREDITO).
     * @param tipoLabel   texto legible del tipo de cuenta.
     * @param numero      IBAN o numero enmascarado.
     * @param saldo       saldo actual.
     * @param descripcion descripcion para mostrar.
     */
    public CuentaMockDto(String tipoCuenta, String tipoLabel, String numero, BigDecimal saldo, String descripcion) {
        this.tipoCuenta = tipoCuenta;
        this.tipoLabel = tipoLabel;
        this.numero = numero;
        this.saldo = saldo;
        this.descripcion = descripcion;
    }

    /** @return tipo tecnico de la cuenta. */
    public String getTipoCuenta() { return tipoCuenta; }
    /** @return texto legible del tipo de cuenta. */
    public String getTipoLabel() { return tipoLabel; }
    /** @return numero IBAN o tarjeta enmascarado. */
    public String getNumero() { return numero; }
    /** @return saldo actual de la cuenta. */
    public BigDecimal getSaldo() { return saldo; }
    /** @return descripcion de la cuenta. */
    public String getDescripcion() { return descripcion; }
}
