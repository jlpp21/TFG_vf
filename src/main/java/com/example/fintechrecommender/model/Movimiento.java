package com.example.fintechrecommender.model;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entidad JPA que representa un movimiento bancario asociado a una conexion.
 *
 * Mapea a la tabla "movimiento". Cada fila es un apunte (ingreso o gasto)
 * de una cuenta concreta del usuario (BancoConectado). El campo monto es
 * positivo para ingresos y negativo para gastos. Tiene un indice por
 * fecha porque las consultas suelen ordenarse o filtrarse por fecha.
 */
@Entity
@Table(name = "movimiento", indexes = @Index(name = "idx_movimiento_fecha", columnList = "fecha"))
public class Movimiento {

    /** Identificador unico autogenerado del movimiento. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Conexion bancaria a la que pertenece el movimiento (carga perezosa). */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "banco_conectado_id", nullable = false)
    private BancoConectado banco;

    /** Fecha y hora en la que se produjo el movimiento. */
    @Column(nullable = false)
    private LocalDateTime fecha;

    /** Texto descriptivo (ej: "Nomina", "Compra Mercadona", "Recibo luz"). */
    @Column(nullable = false, length = 255)
    private String descripcion;

    /** Categoria del movimiento (ej: "Alimentacion", "Ocio", "Ingresos"). */
    @Column(nullable = false, length = 80)
    private String categoria;

    /** Importe del movimiento (positivo ingreso, negativo gasto). 12 digitos con 2 decimales. */
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal monto;

    /** @return id del movimiento. */
    public Long getId() { return id; }
    /** @param id nuevo id a asignar. */
    public void setId(Long id) { this.id = id; }
    /** @return conexion bancaria asociada. */
    public BancoConectado getBanco() { return banco; }
    /** @param banco nueva conexion bancaria a asignar. */
    public void setBanco(BancoConectado banco) { this.banco = banco; }
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
