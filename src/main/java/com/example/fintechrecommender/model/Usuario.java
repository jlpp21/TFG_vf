package com.example.fintechrecommender.model;

import com.example.fintechrecommender.model.perfil.PerfilFinanciero;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * Entidad JPA que representa un usuario registrado en la aplicacion.
 *
 * Mapea a la tabla "usuario" y guarda los datos de la cuenta del usuario:
 * nombre, correo (que sirve como login), hash de la contrasena y perfil
 * financiero declarado al registrarse. Es la entidad central a la que
 * se asocian BancoConectado, Movimiento y ObjetivoUsuario.
 */
@Entity
@Table(name = "usuario")
public class Usuario {

    /** Identificador unico autogenerado del usuario. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Nombre completo del usuario. */
    @Column(nullable = false, length = 100)
    private String nombre;

    /** Correo electronico del usuario. Es unico y se usa como identificador de login. */
    @Column(nullable = false, unique = true, length = 150)
    private String correo;

    /** Hash BCrypt de la contrasena. Nunca se guarda la contrasena en claro. */
    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    /** Perfil financiero declarado por el usuario (ENDEUDADO_CRONICO, ENDEUDADO_AL_DIA, HOLGADO). */
    @Enumerated(EnumType.STRING)
    @Column(name = "perfil_financiero", nullable = false, length = 40)
    private PerfilFinanciero perfilFinanciero;

    /** Fecha en la que se creo la cuenta. Solo se asigna al insertar. */
    @Column(name = "fecha_creacion", updatable = false)
    private LocalDateTime fechaCreacion;

    /**
     * Callback de JPA que se ejecuta justo antes de insertar el usuario.
     * Pone la fecha de creacion al momento actual.
     */
    @PrePersist
    protected void onCreate() {
        this.fechaCreacion = LocalDateTime.now();
    }

    /** @return id del usuario. */
    public Long getId() { return id; }
    /** @param id nuevo id a asignar. */
    public void setId(Long id) { this.id = id; }
    /** @return nombre del usuario. */
    public String getNombre() { return nombre; }
    /** @param nombre nuevo nombre a asignar. */
    public void setNombre(String nombre) { this.nombre = nombre; }
    /** @return correo del usuario. */
    public String getCorreo() { return correo; }
    /** @param correo nuevo correo a asignar. */
    public void setCorreo(String correo) { this.correo = correo; }
    /** @return hash BCrypt de la contrasena. */
    public String getPasswordHash() { return passwordHash; }
    /** @param passwordHash nuevo hash de contrasena a asignar. */
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    /** @return perfil financiero del usuario. */
    public PerfilFinanciero getPerfilFinanciero() { return perfilFinanciero; }
    /** @param perfilFinanciero nuevo perfil financiero a asignar. */
    public void setPerfilFinanciero(PerfilFinanciero perfilFinanciero) { this.perfilFinanciero = perfilFinanciero; }
    /** @return fecha en la que se creo la cuenta. */
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    /** @param fechaCreacion nueva fecha de creacion a asignar. */
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
}
