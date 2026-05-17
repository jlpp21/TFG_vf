package com.example.fintechrecommender.dto;

/**
 * DTO que representa un banco del catalogo de bancos disponibles para conectar.
 *
 * Se usa cuando el frontend pide la lista de bancos a los que el usuario
 * puede vincularse (por ejemplo en la pantalla de "Bancos"). Solo lleva
 * informacion publica/visual del banco; no representa una conexion activa
 * (eso lo hace BancoDto).
 */
public class BancoCatalogoDto {

    /** Codigo unico del banco (ej: "santander", "bbva"). */
    private String codigo;

    /** Nombre comercial del banco. */
    private String nombre;

    /** Dominio web del banco, util para mostrar el favicon. */
    private String dominio;

    /** Color principal del banco en formato hex, para pintar la UI. */
    private String color;

    /** URL del logo del banco para mostrarlo en la pantalla. */
    private String logoUrl;

    /** Constructor vacio requerido por frameworks de serializacion (Jackson). */
    public BancoCatalogoDto() {}

    /**
     * Crea un DTO de catalogo con todos los datos visuales del banco.
     *
     * @param codigo  codigo identificador del banco.
     * @param nombre  nombre comercial del banco.
     * @param dominio dominio web (ej: "santander.es").
     * @param color   color principal en formato hex.
     * @param logoUrl URL del logo del banco.
     */
    public BancoCatalogoDto(String codigo, String nombre, String dominio, String color, String logoUrl) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.dominio = dominio;
        this.color = color;
        this.logoUrl = logoUrl;
    }

    /** @return codigo identificador del banco. */
    public String getCodigo() { return codigo; }
    /** @return nombre comercial del banco. */
    public String getNombre() { return nombre; }
    /** @return dominio web del banco. */
    public String getDominio() { return dominio; }
    /** @return color principal del banco en formato hex. */
    public String getColor() { return color; }
    /** @return URL del logo del banco. */
    public String getLogoUrl() { return logoUrl; }
}
