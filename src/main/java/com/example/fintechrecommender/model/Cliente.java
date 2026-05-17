package com.example.fintechrecommender.model;

import javax.persistence.*;

/**
 * Entidad JPA legacy que representa un cliente con datos de contacto basicos.
 *
 * Pertenece a un modulo anterior (junto con Transaccion) y existe como
 * complemento al modelo principal Usuario/BancoConectado/Movimiento.
 * Se mantiene por compatibilidad con los endpoints /api/clientes.
 */
@Entity
public class Cliente {

    /** Identificador unico autogenerado. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Nombre del cliente. */
    private String nombre;

    /** Correo electronico de contacto. */
    private String correo;

    /** Telefono de contacto. */
    private String telefono;

    // Getters y setters
    /** @return id del cliente. */
    public Long getId() { return id; }
    /** @param id nuevo id a asignar. */
    public void setId(Long id) { this.id = id; }
    /** @return nombre del cliente. */
    public String getNombre() { return nombre; }
    /** @param nombre nuevo nombre a asignar. */
    public void setNombre(String nombre) { this.nombre = nombre; }
    /** @return correo del cliente. */
    public String getCorreo() { return correo; }
    /** @param correo nuevo correo a asignar. */
    public void setCorreo(String correo) { this.correo = correo; }
    /** @return telefono del cliente. */
    public String getTelefono() { return telefono; }
    /** @param telefono nuevo telefono a asignar. */
    public void setTelefono(String telefono) { this.telefono = telefono; }
}
