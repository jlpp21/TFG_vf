package com.example.fintechrecommender.dto;

/**
 * DTO que representa un objetivo financiero del usuario.
 *
 * Se usa tanto de entrada (cuando el usuario crea un objetivo desde el
 * formulario) como de salida (cuando el backend devuelve la lista de
 * objetivos al frontend). Cada objetivo tiene un codigo (ej: "comprar_piso")
 * y un plazo (CORTO, MEDIO, LARGO).
 */
public class ObjetivoUsuarioDto {

    /** Codigo del objetivo (ej: "comprar_piso", "plan_jubilacion", "fondo_emergencia"). */
    private String objetivo;

    /** Plazo en el que se quiere conseguir: CORTO, MEDIO o LARGO. */
    private String plazo;

    /** Constructor vacio requerido para serializacion. */
    public ObjetivoUsuarioDto() {}

    /**
     * Crea un DTO de objetivo con codigo y plazo.
     *
     * @param objetivo codigo identificador del objetivo.
     * @param plazo    plazo del objetivo (CORTO, MEDIO, LARGO).
     */
    public ObjetivoUsuarioDto(String objetivo, String plazo) {
        this.objetivo = objetivo;
        this.plazo = plazo;
    }

    /** @return codigo del objetivo. */
    public String getObjetivo() { return objetivo; }
    /** @param objetivo nuevo codigo de objetivo a asignar. */
    public void setObjetivo(String objetivo) { this.objetivo = objetivo; }
    /** @return plazo del objetivo. */
    public String getPlazo() { return plazo; }
    /** @param plazo nuevo plazo a asignar. */
    public void setPlazo(String plazo) { this.plazo = plazo; }
}
