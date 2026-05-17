package com.example.fintechrecommender.model.perfil;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Enum que clasifica al usuario en uno de tres perfiles financieros generales.
 *
 * Cada valor agrupa una lista de SubPerfil con rangos numericos (ingresos,
 * gastos, probabilidad de deuda, etc.) que el generador de movimientos
 * (MovimientoGeneratorService) usa para crear datos realistas en el
 * sandbox segun el perfil declarado por el usuario al registrarse.
 *
 * Valores:
 * - ENDEUDADO_CRONICO: usuarios con dificultades de pago y deuda alta.
 * - ENDEUDADO_AL_DIA: usuarios con creditos o hipoteca pero al corriente.
 * - HOLGADO: usuarios con capacidad de ahorro consistente.
 */
public enum PerfilFinanciero {

    /** Perfil con deuda persistente y dificultad para llegar a fin de mes. */
    ENDEUDADO_CRONICO(Arrays.asList(
            new SubPerfil("Gastador",
                    1500, 1900, 850, 1200,
                    0.20, 0.85, 250, 500,
                    1.25, 0, 30,
                    0.45, 0.65,
                    0.90, 1.20,
                    0, 30,
                    20, 150,
                    8000, 18000, 0.10, 60, 84),
            new SubPerfil("Sobreendeudado",
                    1400, 1750, 900, 1300,
                    0.40, 1.0, 400, 750,
                    1.30, 0, 20,
                    0.70, 0.85,
                    1.00, 1.30,
                    0, 20,
                    0, 80,
                    12000, 25000, 0.12, 72, 96)
    )),

    /** Perfil con creditos o hipoteca al corriente y capacidad limitada de ahorro. */
    ENDEUDADO_AL_DIA(Arrays.asList(
            new SubPerfil("Joven profesional",
                    1700, 2100, 700, 1000,
                    0.05, 0.35, 90, 220,
                    1.05, 0, 100,
                    0.05, 0.10,
                    0.50, 0.80,
                    50, 150,
                    200, 700,
                    5000, 12000, 0.075, 48, 72),
            new SubPerfil("Familia con hipoteca",
                    2200, 2700, 800, 1100,
                    0.80, 0.50, 200, 450,
                    0.95, 30, 150,
                    0.05, 0.10,
                    0.45, 0.75,
                    50, 200,
                    300, 900,
                    6000, 14000, 0.075, 48, 72)
    )),

    /** Perfil con capacidad de ahorro estable y sin deuda problematica. */
    HOLGADO(Arrays.asList(
            new SubPerfil("Ahorrador",
                    2400, 3200, 700, 1000,
                    0.45, 0.10, 100, 220,
                    0.65, 350, 800,
                    0.0, 0.0,
                    0.30, 0.55,
                    400, 800,
                    1500, 4000,
                    3000, 8000, 0.06, 36, 60),
            new SubPerfil("Equilibrado",
                    2200, 2900, 700, 1000,
                    0.55, 0.15, 100, 250,
                    0.85, 200, 500,
                    0.0, 0.0,
                    0.35, 0.60,
                    250, 550,
                    800, 2500,
                    4000, 10000, 0.06, 36, 60)
    ));

    /** Lista inmutable de subperfiles asociados a este perfil. */
    private final List<SubPerfil> subVariantes;

    /**
     * Constructor del enum.
     *
     * @param subVariantes lista de SubPerfiles que cubre este perfil.
     */
    PerfilFinanciero(List<SubPerfil> subVariantes) {
        this.subVariantes = Collections.unmodifiableList(subVariantes);
    }

    /**
     * Devuelve los SubPerfiles asociados al perfil. Sirven para que el
     * generador de movimientos elija uno aleatoriamente y simule datos
     * dentro de los rangos definidos.
     *
     * @return lista inmutable de SubPerfiles del perfil actual.
     */
    public List<SubPerfil> getSubVariantes() {
        return subVariantes;
    }
}
