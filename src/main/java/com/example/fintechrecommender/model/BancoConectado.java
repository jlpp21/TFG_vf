package com.example.fintechrecommender.model;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * Entidad JPA que representa la conexion de un usuario con un banco.
 *
 * Mapea a la tabla "banco_conectado". Cada fila guarda un banco que el
 * usuario ha vinculado en su cuenta junto con el tipo de cuenta concreta
 * (CORRIENTE, AHORRO, CREDITO, PRESTAMO). La restriccion unica
 * (usuario_id, banco_codigo, tipo_cuenta) impide que un mismo usuario
 * conecte dos veces la misma cuenta del mismo banco.
 */
@Entity
@Table(name = "banco_conectado",
       uniqueConstraints = @UniqueConstraint(columnNames = {"usuario_id", "banco_codigo", "tipo_cuenta"}))
public class BancoConectado {

    /** Identificador unico autogenerado de la conexion. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Usuario al que pertenece la conexion. Carga perezosa para no traer el usuario salvo que se necesite. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    /** Codigo del banco (ej: "santander", "bbva"). */
    @Column(name = "banco_codigo", nullable = false, length = 50)
    private String bancoCodigo;

    /** Nombre comercial del banco. */
    @Column(nullable = false, length = 100)
    private String nombre;

    /** Dominio web del banco (opcional). */
    @Column(length = 100)
    private String dominio;

    /** Color principal del banco para usar en la UI. */
    @Column(length = 20)
    private String color;

    /** URL del logo del banco. */
    @Column(name = "logo_url", length = 500)
    private String logoUrl;

    /** Tipo de cuenta vinculada. Por defecto es CORRIENTE. */
    @Column(name = "tipo_cuenta", nullable = false, length = 20)
    private String tipoCuenta = "CORRIENTE";

    /** Cuatro ultimos digitos de la cuenta o tarjeta. */
    @Column(name = "numero_final", length = 4)
    private String numeroFinal;

    /** Fecha en la que el usuario conecto este banco. No se actualiza despues. */
    @Column(name = "fecha_conexion", updatable = false)
    private LocalDateTime fechaConexion;

    /**
     * Callback de JPA que se ejecuta antes de insertar.
     * Asigna la fecha de conexion al momento actual.
     */
    @PrePersist
    void onCreate() {
        this.fechaConexion = LocalDateTime.now();
    }

    /** @return id de la conexion. */
    public Long getId() { return id; }
    /** @param id nuevo id a asignar. */
    public void setId(Long id) { this.id = id; }
    /** @return usuario propietario de la conexion. */
    public Usuario getUsuario() { return usuario; }
    /** @param usuario nuevo usuario a asignar. */
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
    /** @return codigo del banco. */
    public String getBancoCodigo() { return bancoCodigo; }
    /** @param bancoCodigo nuevo codigo del banco. */
    public void setBancoCodigo(String bancoCodigo) { this.bancoCodigo = bancoCodigo; }
    /** @return nombre comercial del banco. */
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
    /** @return tipo de cuenta (CORRIENTE, AHORRO, CREDITO, PRESTAMO). */
    public String getTipoCuenta() { return tipoCuenta; }
    /** @param tipoCuenta nuevo tipo de cuenta. */
    public void setTipoCuenta(String tipoCuenta) { this.tipoCuenta = tipoCuenta; }
    /** @return cuatro ultimos digitos de la cuenta. */
    public String getNumeroFinal() { return numeroFinal; }
    /** @param numeroFinal nuevos 4 ultimos digitos. */
    public void setNumeroFinal(String numeroFinal) { this.numeroFinal = numeroFinal; }
    /** @return fecha en la que se hizo la conexion. */
    public LocalDateTime getFechaConexion() { return fechaConexion; }
    /** @param fechaConexion nueva fecha de conexion a asignar. */
    public void setFechaConexion(LocalDateTime fechaConexion) { this.fechaConexion = fechaConexion; }
}
