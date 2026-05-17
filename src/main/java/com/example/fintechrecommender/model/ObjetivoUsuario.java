package com.example.fintechrecommender.model;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * Entidad JPA que representa un objetivo financiero declarado por el usuario.
 *
 * Mapea a la tabla "objetivo_usuario". Cada fila es una meta como
 * "comprar_piso", "plan_jubilacion" o "fondo_emergencia" con un plazo
 * (CORTO, MEDIO, LARGO). La restriccion unica (usuario_id, objetivo)
 * evita que un usuario duplique el mismo objetivo. Estos objetivos se
 * envian al motor de IA para personalizar las recomendaciones.
 */
@Entity
@Table(name = "objetivo_usuario",
       uniqueConstraints = @UniqueConstraint(columnNames = {"usuario_id", "objetivo"}))
public class ObjetivoUsuario {

    /** Identificador unico autogenerado del objetivo. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Usuario al que pertenece el objetivo. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    /** Codigo del objetivo (ej: "comprar_piso", "plan_jubilacion"). */
    @Column(nullable = false, length = 60)
    private String objetivo;

    /** Plazo del objetivo: CORTO, MEDIO o LARGO. */
    @Column(nullable = false, length = 20)
    private String plazo;

    /** Fecha en la que el usuario creo el objetivo. */
    @Column(name = "fecha_creacion", updatable = false)
    private LocalDateTime fechaCreacion;

    /**
     * Callback de JPA que se ejecuta antes de insertar.
     * Asigna la fecha de creacion al momento actual.
     */
    @PrePersist
    void onCreate() {
        this.fechaCreacion = LocalDateTime.now();
    }

    /** @return id del objetivo. */
    public Long getId() { return id; }
    /** @param id nuevo id a asignar. */
    public void setId(Long id) { this.id = id; }
    /** @return usuario propietario del objetivo. */
    public Usuario getUsuario() { return usuario; }
    /** @param usuario nuevo usuario a asignar. */
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
    /** @return codigo del objetivo. */
    public String getObjetivo() { return objetivo; }
    /** @param objetivo nuevo codigo de objetivo a asignar. */
    public void setObjetivo(String objetivo) { this.objetivo = objetivo; }
    /** @return plazo del objetivo. */
    public String getPlazo() { return plazo; }
    /** @param plazo nuevo plazo a asignar. */
    public void setPlazo(String plazo) { this.plazo = plazo; }
    /** @return fecha en la que se creo el objetivo. */
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
}
