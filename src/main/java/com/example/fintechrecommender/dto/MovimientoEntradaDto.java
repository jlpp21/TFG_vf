package com.example.fintechrecommender.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO de entrada que representa un movimiento que viene desde el cliente
 * para ser guardado en la base de datos.
 *
 * Lo usa principalmente ConectarBancoRequest cuando se conecta un nuevo
 * banco y se envia la lista inicial de movimientos generados en el sandbox.
 * Es la version "minima" de un movimiento (sin id, sin balance calculado),
 * porque esos campos los rellena el backend al persistir.
 */
public class MovimientoEntradaDto {

    /** Fecha en la que se produjo el movimiento. */
    private LocalDateTime fecha;

    /** Texto descriptivo del movimiento. */
    private String descripcion;

    /** Categoria a la que pertenece el movimiento. */
    private String categoria;

    /** Importe del movimiento (positivo ingreso, negativo gasto). */
    private BigDecimal monto;

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
    /** @return importe del movimiento. */
    public BigDecimal getMonto() { return monto; }
    /** @param monto nuevo importe a asignar. */
    public void setMonto(BigDecimal monto) { this.monto = monto; }
}
