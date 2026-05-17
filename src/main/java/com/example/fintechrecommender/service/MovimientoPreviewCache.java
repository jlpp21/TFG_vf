package com.example.fintechrecommender.service;

import com.example.fintechrecommender.model.Movimiento;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Cache en memoria de los movimientos generados durante la previsualizacion del wizard.
 *
 * Cuando el usuario ve la pantalla "selecciona la cuenta" en el sandbox,
 * el backend genera movimientos sinteticos para mostrar saldos. Si el
 * usuario confirma la conexion, este cache permite reutilizar esos mismos
 * movimientos (en vez de generar otros nuevos), de forma que el saldo
 * que vio durante el preview coincida con el balance final que ve en
 * la pantalla de transacciones.
 *
 * Es un singleton de Spring (@Component) y usa ConcurrentHashMap para
 * ser seguro entre hilos.
 */
@Component
public class MovimientoPreviewCache {

    /** Mapa interno: clave compuesta -> lista de movimientos generados. */
    private final Map<String, List<Movimiento>> cache = new ConcurrentHashMap<>();

    /**
     * Guarda en cache los movimientos generados para una combinacion de
     * usuario, banco y tipo de cuenta.
     *
     * @param userId      id del usuario.
     * @param bancoCodigo codigo del banco.
     * @param tipoCuenta  tipo de cuenta (CORRIENTE, AHORRO, ...).
     * @param movs        lista de movimientos generados.
     */
    public void guardar(Long userId, String bancoCodigo, String tipoCuenta, List<Movimiento> movs) {
        cache.put(clave(userId, bancoCodigo, tipoCuenta), movs);
    }

    /**
     * Recupera los movimientos cacheados y los elimina del cache (consume = leer + borrar).
     *
     * @param userId      id del usuario.
     * @param bancoCodigo codigo del banco.
     * @param tipoCuenta  tipo de cuenta.
     * @return lista de movimientos cacheados o null si no habia nada guardado.
     */
    public List<Movimiento> consumir(Long userId, String bancoCodigo, String tipoCuenta) {
        return cache.remove(clave(userId, bancoCodigo, tipoCuenta));
    }

    /**
     * Borra todas las entradas del cache que sean del usuario y banco indicados,
     * sin importar el tipo de cuenta.
     *
     * @param userId      id del usuario.
     * @param bancoCodigo codigo del banco cuyos previews se borran.
     */
    public void limpiar(Long userId, String bancoCodigo) {
        String prefijo = userId + ":" + bancoCodigo + ":";
        cache.keySet().removeIf(k -> k.startsWith(prefijo));
    }

    /**
     * Construye la clave compuesta usada para indexar el cache.
     *
     * @param userId      id del usuario.
     * @param bancoCodigo codigo del banco.
     * @param tipoCuenta  tipo de cuenta.
     * @return clave en formato "userId:bancoCodigo:tipoCuenta".
     */
    private String clave(Long userId, String bancoCodigo, String tipoCuenta) {
        return userId + ":" + bancoCodigo + ":" + tipoCuenta;
    }
}
