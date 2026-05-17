package com.example.fintechrecommender.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO de salida que representa un movimiento bancario para el frontend.
 *
 * Se usa cuando el backend devuelve la lista de movimientos del usuario.
 * Aplana la informacion de la entidad Movimiento e incluye datos del banco
 * asociado (nombre, color, logo, etc.) para que el frontend pueda pintar
 * cada movimiento con la marca de su banco sin hacer otra peticion.
 */
public class MovimientoDto {

    /** Identificador unico del movimiento. */
    private Long id;

    /** Fecha y hora en la que se produjo el movimiento. */
    private LocalDateTime fecha;

    /** Texto descriptivo del movimiento (ej: "Nomina", "Compra Mercadona"). */
    private String descripcion;

    /** Categoria del movimiento (ej: "Alimentacion", "Ingresos"). */
    private String categoria;

    /** Importe del movimiento. Negativo si es gasto, positivo si es ingreso. */
    private BigDecimal monto;

    /** Saldo de la cuenta justo despues de aplicar este movimiento. */
    private BigDecimal balanceDespues;

    /** Id de la conexion bancaria a la que pertenece el movimiento. */
    private Long bancoId;

    /** Codigo del banco. */
    private String bancoCodigo;

    /** Nombre comercial del banco. */
    private String bancoNombre;

    /** Color principal del banco. */
    private String bancoColor;

    /** Dominio web del banco. */
    private String bancoDominio;

    /** URL del logo del banco. */
    private String bancoLogoUrl;

    /** Tipo de cuenta del banco (CORRIENTE, AHORRO, etc.). */
    private String bancoTipoCuenta;

    /** Cuatro ultimos digitos de la cuenta del banco. */
    private String bancoNumeroFinal;

    /** @return id del movimiento. */
    public Long getId() { return id; }
    /** @param id nuevo id del movimiento. */
    public void setId(Long id) { this.id = id; }
    /** @return fecha del movimiento. */
    public LocalDateTime getFecha() { return fecha; }
    /** @param fecha nueva fecha a asignar. */
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }
    /** @return descripcion del movimiento. */
    public String getDescripcion() { return descripcion; }
    /** @param descripcion nueva descripcion a asignar. */
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    /** @return categoria del movimiento. */
    public String getCategoria() { return categoria; }
    /** @param categoria nueva categoria a asignar. */
    public void setCategoria(String categoria) { this.categoria = categoria; }
    /** @return importe del movimiento (negativo gasto, positivo ingreso). */
    public BigDecimal getMonto() { return monto; }
    /** @param monto nuevo importe a asignar. */
    public void setMonto(BigDecimal monto) { this.monto = monto; }
    /** @return saldo despues del movimiento. */
    public BigDecimal getBalanceDespues() { return balanceDespues; }
    /** @param balanceDespues nuevo saldo despues a asignar. */
    public void setBalanceDespues(BigDecimal balanceDespues) { this.balanceDespues = balanceDespues; }
    /** @return id del banco asociado. */
    public Long getBancoId() { return bancoId; }
    /** @param bancoId nuevo id del banco. */
    public void setBancoId(Long bancoId) { this.bancoId = bancoId; }
    /** @return codigo del banco. */
    public String getBancoCodigo() { return bancoCodigo; }
    /** @param bancoCodigo nuevo codigo del banco. */
    public void setBancoCodigo(String bancoCodigo) { this.bancoCodigo = bancoCodigo; }
    /** @return nombre comercial del banco. */
    public String getBancoNombre() { return bancoNombre; }
    /** @param bancoNombre nuevo nombre del banco. */
    public void setBancoNombre(String bancoNombre) { this.bancoNombre = bancoNombre; }
    /** @return color del banco. */
    public String getBancoColor() { return bancoColor; }
    /** @param bancoColor nuevo color a asignar. */
    public void setBancoColor(String bancoColor) { this.bancoColor = bancoColor; }
    /** @return dominio del banco. */
    public String getBancoDominio() { return bancoDominio; }
    /** @param bancoDominio nuevo dominio a asignar. */
    public void setBancoDominio(String bancoDominio) { this.bancoDominio = bancoDominio; }
    /** @return URL del logo del banco. */
    public String getBancoLogoUrl() { return bancoLogoUrl; }
    /** @param bancoLogoUrl nueva URL del logo. */
    public void setBancoLogoUrl(String bancoLogoUrl) { this.bancoLogoUrl = bancoLogoUrl; }
    /** @return tipo de cuenta del banco. */
    public String getBancoTipoCuenta() { return bancoTipoCuenta; }
    /** @param bancoTipoCuenta nuevo tipo de cuenta. */
    public void setBancoTipoCuenta(String bancoTipoCuenta) { this.bancoTipoCuenta = bancoTipoCuenta; }
    /** @return numero final (4 ultimos digitos) del banco. */
    public String getBancoNumeroFinal() { return bancoNumeroFinal; }
    /** @param bancoNumeroFinal nuevos 4 ultimos digitos. */
    public void setBancoNumeroFinal(String bancoNumeroFinal) { this.bancoNumeroFinal = bancoNumeroFinal; }
}
