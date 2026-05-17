package com.example.fintechrecommender.dto;

import com.example.fintechrecommender.model.BancoConectado;

import java.time.LocalDateTime;

/**
 * DTO que representa una conexion bancaria activa de un usuario.
 *
 * Se devuelve al frontend cuando el usuario consulta sus bancos conectados
 * (a diferencia de BancoCatalogoDto, que es el catalogo de bancos disponibles).
 * Anyade informacion propia de la conexion: tipo de cuenta, numero final
 * enmascarado y fecha en la que se vinculo.
 */
public class BancoDto {

    /** Identificador de la conexion en base de datos. */
    private Long id;

    /** Codigo del banco al que se conecto el usuario. */
    private String bancoCodigo;

    /** Nombre comercial del banco. */
    private String nombre;

    /** Dominio web del banco. */
    private String dominio;

    /** Color principal del banco. */
    private String color;

    /** URL del logo del banco. */
    private String logoUrl;

    /** Tipo de cuenta vinculada (CORRIENTE, AHORRO, CREDITO, PRESTAMO). */
    private String tipoCuenta;

    /** Texto en espanol para mostrar el tipo de cuenta (ej: "Cuenta de ahorro"). */
    private String tipoCuentaLabel;

    /** Cuatro ultimos digitos de la cuenta o tarjeta. */
    private String numeroFinal;

    /** Fecha en la que el usuario conecto este banco. */
    private LocalDateTime fechaConexion;

    /**
     * Construye un BancoDto a partir de la entidad BancoConectado de la base de datos.
     *
     * @param b entidad de banco conectado leida de la base de datos.
     * @return DTO listo para enviar al frontend con los datos de la conexion.
     */
    public static BancoDto from(BancoConectado b) {
        BancoDto dto = new BancoDto();
        dto.id = b.getId();
        dto.bancoCodigo = b.getBancoCodigo();
        dto.nombre = b.getNombre();
        dto.dominio = b.getDominio();
        dto.color = b.getColor();
        dto.logoUrl = b.getLogoUrl();
        dto.tipoCuenta = b.getTipoCuenta();
        dto.tipoCuentaLabel = etiquetaTipo(b.getTipoCuenta());
        dto.numeroFinal = b.getNumeroFinal();
        dto.fechaConexion = b.getFechaConexion();
        return dto;
    }

    /**
     * Convierte el codigo tecnico del tipo de cuenta a un texto legible en espanol.
     *
     * @param tipo codigo del tipo de cuenta (CORRIENTE, AHORRO, CREDITO).
     * @return texto en espanol para mostrar al usuario.
     */
    private static String etiquetaTipo(String tipo) {
        if (tipo == null) return "Cuenta corriente";
        switch (tipo.toUpperCase()) {
            case "AHORRO":  return "Cuenta de ahorro";
            case "CREDITO": return "Tarjeta de credito";
            default:        return "Cuenta corriente";
        }
    }

    /** @return id de la conexion en base de datos. */
    public Long getId() { return id; }
    /** @return codigo del banco. */
    public String getBancoCodigo() { return bancoCodigo; }
    /** @return nombre comercial del banco. */
    public String getNombre() { return nombre; }
    /** @return dominio web del banco. */
    public String getDominio() { return dominio; }
    /** @return color principal del banco. */
    public String getColor() { return color; }
    /** @return URL del logo del banco. */
    public String getLogoUrl() { return logoUrl; }
    /** @return tipo de cuenta (CORRIENTE, AHORRO, CREDITO, PRESTAMO). */
    public String getTipoCuenta() { return tipoCuenta; }
    /** @return texto legible del tipo de cuenta para mostrar en pantalla. */
    public String getTipoCuentaLabel() { return tipoCuentaLabel; }
    /** @return cuatro ultimos digitos de la cuenta. */
    public String getNumeroFinal() { return numeroFinal; }
    /** @return fecha en la que el usuario conecto el banco. */
    public LocalDateTime getFechaConexion() { return fechaConexion; }
}
