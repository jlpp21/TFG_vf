package com.example.fintechrecommender.model;

import javax.persistence.*;
import java.util.Date;

/**
 * Entidad JPA legacy que representa una transaccion asociada a un Cliente.
 *
 * Modelo anterior, paralelo al de Movimiento (que es el que usa el flujo
 * principal con BancoConectado y Usuario). Se conserva por compatibilidad
 * con los endpoints /api/transacciones.
 */
@Entity
public class Transaccion {

    /** Identificador unico autogenerado. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Cliente al que pertenece la transaccion. */
    @ManyToOne
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    /** Fecha en la que se realizo la transaccion. */
    private Date fecha;

    /** Importe de la transaccion. */
    private Double monto;

    /** Texto descriptivo de la transaccion. */
    private String descripcion;

    // Getters y setters
    /** @return id de la transaccion. */
    public Long getId() { return id; }
    /** @param id nuevo id a asignar. */
    public void setId(Long id) { this.id = id; }
    /** @return cliente asociado. */
    public Cliente getCliente() { return cliente; }
    /** @param cliente nuevo cliente a asignar. */
    public void setCliente(Cliente cliente) { this.cliente = cliente; }
    /** @return fecha de la transaccion. */
    public Date getFecha() { return fecha; }
    /** @param fecha nueva fecha a asignar. */
    public void setFecha(Date fecha) { this.fecha = fecha; }
    /** @return importe de la transaccion. */
    public Double getMonto() { return monto; }
    /** @param monto nuevo importe a asignar. */
    public void setMonto(Double monto) { this.monto = monto; }
    /** @return descripcion de la transaccion. */
    public String getDescripcion() { return descripcion; }
    /** @param descripcion nueva descripcion a asignar. */
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
}
