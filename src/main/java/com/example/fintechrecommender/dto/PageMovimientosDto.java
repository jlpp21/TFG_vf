package com.example.fintechrecommender.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * DTO de salida que representa una pagina de movimientos del usuario.
 *
 * Se devuelve al frontend en la pantalla de transacciones para mostrar
 * un listado paginado, e incluye ademas un resumen del balance total y
 * un desglose por tipo de cuenta (cuanto hay en CORRIENTE, AHORRO,
 * CREDITO, PRESTAMO) para pintar las tarjetas de balance.
 */
public class PageMovimientosDto {

    /** Lista de movimientos de la pagina actual. */
    private List<MovimientoDto> items;

    /** Numero de pagina (empezando en 0). */
    private int page;

    /** Tamano de la pagina (movimientos por pagina). */
    private int size;

    /** Numero total de movimientos del usuario (todas las paginas). */
    private long total;

    /** Numero total de paginas calculado a partir de total y size. */
    private int totalPaginas;

    /** Suma de todos los saldos del usuario. */
    private BigDecimal balanceTotal;

    /** Saldo desglosado por tipo de cuenta (clave: tipo, valor: saldo). */
    private Map<String, BigDecimal> desgloseTipoCuenta;

    /**
     * Construye la pagina con sus items y los datos agregados.
     * Calcula automaticamente el numero total de paginas.
     *
     * @param items              movimientos de la pagina.
     * @param page               numero de pagina actual.
     * @param size               tamano de la pagina.
     * @param total              total de movimientos del usuario.
     * @param balanceTotal       suma de saldos del usuario.
     * @param desgloseTipoCuenta saldo por tipo de cuenta.
     */
    public PageMovimientosDto(List<MovimientoDto> items, int page, int size, long total,
                              BigDecimal balanceTotal, Map<String, BigDecimal> desgloseTipoCuenta) {
        this.items = items;
        this.page = page;
        this.size = size;
        this.total = total;
        this.totalPaginas = (int) Math.ceil((double) total / Math.max(size, 1));
        this.balanceTotal = balanceTotal;
        this.desgloseTipoCuenta = desgloseTipoCuenta;
    }

    /** @return movimientos de la pagina actual. */
    public List<MovimientoDto> getItems() { return items; }
    /** @return numero de pagina actual. */
    public int getPage() { return page; }
    /** @return tamano de la pagina. */
    public int getSize() { return size; }
    /** @return total de movimientos en todas las paginas. */
    public long getTotal() { return total; }
    /** @return numero total de paginas. */
    public int getTotalPaginas() { return totalPaginas; }
    /** @return balance total (suma de saldos). */
    public BigDecimal getBalanceTotal() { return balanceTotal; }
    /** @return mapa con el saldo agrupado por tipo de cuenta. */
    public Map<String, BigDecimal> getDesgloseTipoCuenta() { return desgloseTipoCuenta; }
}
