package com.example.fintechrecommender.dto;

import javax.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO que el frontend envia al backend para registrar una nueva conexion bancaria.
 *
 * Incluye la informacion del banco (codigo, nombre, color, logo, etc.), el tipo
 * de cuenta que se conecta y la lista inicial de movimientos generados durante
 * el flujo del sandbox. El controlador BancoController recibe este DTO y crea
 * la entidad BancoConectado junto con sus movimientos.
 *
 * Los campos bancoCodigo y nombre son obligatorios (@NotBlank).
 */
public class ConectarBancoRequest {

    /** Codigo del banco que se quiere conectar. Obligatorio. */
    @NotBlank
    private String bancoCodigo;

    /** Nombre comercial del banco. Obligatorio. */
    @NotBlank
    private String nombre;

    /** Dominio web del banco (opcional). */
    private String dominio;

    /** Color principal del banco en formato hex (opcional). */
    private String color;

    /** URL del logo del banco (opcional). */
    private String logoUrl;

    /** Tipo de cuenta a conectar (CORRIENTE, AHORRO, CREDITO, PRESTAMO). */
    private String tipoCuenta;

    /** Cuatro ultimos digitos de la cuenta o tarjeta. */
    private String numeroFinal;

    /** Lista de movimientos iniciales que se persistiran junto con la conexion. */
    private List<MovimientoEntradaDto> movimientos = new ArrayList<>();

    /** @return codigo del banco a conectar. */
    public String getBancoCodigo() { return bancoCodigo; }
    /** @param bancoCodigo nuevo codigo del banco. */
    public void setBancoCodigo(String bancoCodigo) { this.bancoCodigo = bancoCodigo; }
    /** @return nombre del banco. */
    public String getNombre() { return nombre; }
    /** @param nombre nuevo nombre del banco. */
    public void setNombre(String nombre) { this.nombre = nombre; }
    /** @return dominio web del banco. */
    public String getDominio() { return dominio; }
    /** @param dominio nuevo dominio del banco. */
    public void setDominio(String dominio) { this.dominio = dominio; }
    /** @return color principal del banco. */
    public String getColor() { return color; }
    /** @param color nuevo color del banco. */
    public void setColor(String color) { this.color = color; }
    /** @return URL del logo del banco. */
    public String getLogoUrl() { return logoUrl; }
    /** @param logoUrl nueva URL del logo. */
    public void setLogoUrl(String logoUrl) { this.logoUrl = logoUrl; }
    /** @return tipo de cuenta a conectar. */
    public String getTipoCuenta() { return tipoCuenta; }
    /** @param tipoCuenta nuevo tipo de cuenta. */
    public void setTipoCuenta(String tipoCuenta) { this.tipoCuenta = tipoCuenta; }
    /** @return numero final (4 ultimos digitos) de la cuenta. */
    public String getNumeroFinal() { return numeroFinal; }
    /** @param numeroFinal nuevos 4 ultimos digitos. */
    public void setNumeroFinal(String numeroFinal) { this.numeroFinal = numeroFinal; }
    /** @return lista de movimientos iniciales a registrar. */
    public List<MovimientoEntradaDto> getMovimientos() { return movimientos; }
    /** @param movimientos nueva lista de movimientos iniciales. */
    public void setMovimientos(List<MovimientoEntradaDto> movimientos) { this.movimientos = movimientos; }
}
