package com.example.fintechrecommender.model.perfil;

/**
 * Configuracion de rangos numericos que define un sub-perfil financiero.
 *
 * Pertenece a un PerfilFinanciero y describe con valores minimos y maximos
 * el comportamiento economico tipico de un usuario de ese sub-perfil:
 * cuanto ingresa, cuanto gasta en vivienda, probabilidad de tener deuda,
 * intensidad de uso de la tarjeta, capital del prestamo, etc. Estos
 * rangos los utiliza MovimientoGeneratorService para generar movimientos
 * sinteticos realistas durante el flujo del sandbox.
 */
public class SubPerfil {

    private final String nombre;
    private final int ingresoMin, ingresoMax;
    private final int viviendaMin, viviendaMax;
    private final double probHipoteca, probDeuda;
    private final int deudaMin, deudaMax;
    private final double multiplicadorOcio;
    private final int ahorroMin, ahorroMax;
    private final double probComisionImpago;
    private final double probPagoMinimoCredito;
    private final double intensidadUsoCreditoMin, intensidadUsoCreditoMax;
    private final double aportacionAhorroMin, aportacionAhorroMax;
    private final double saldoAperturaAhorroMin, saldoAperturaAhorroMax;
    private final double prestamoCapitalMin, prestamoCapitalMax;
    private final double prestamoTasaAnual;
    private final int prestamoPlazoMesesMin, prestamoPlazoMesesMax;

    /**
     * Crea un sub-perfil con todos los rangos numericos.
     *
     * @param nombre                     etiqueta del sub-perfil (ej: "Gastador", "Ahorrador").
     * @param ingresoMin                 ingreso mensual minimo en euros.
     * @param ingresoMax                 ingreso mensual maximo en euros.
     * @param viviendaMin                gasto minimo en vivienda al mes (alquiler/hipoteca).
     * @param viviendaMax                gasto maximo en vivienda al mes.
     * @param probHipoteca               probabilidad (0..1) de que el sub-perfil tenga hipoteca.
     * @param probDeuda                  probabilidad (0..1) de que tenga deuda en tarjeta.
     * @param deudaMin                   deuda minima en tarjeta en euros.
     * @param deudaMax                   deuda maxima en tarjeta en euros.
     * @param multiplicadorOcio          multiplicador para escalar el gasto en ocio.
     * @param ahorroMin                  ahorro mensual minimo en euros.
     * @param ahorroMax                  ahorro mensual maximo en euros.
     * @param probComisionImpago         probabilidad (0..1) de que se cobre comision por impago.
     * @param probPagoMinimoCredito      probabilidad (0..1) de pagar solo el minimo de la tarjeta.
     * @param intensidadUsoCreditoMin    intensidad minima de uso de la tarjeta (0..1+).
     * @param intensidadUsoCreditoMax    intensidad maxima de uso de la tarjeta (0..1+).
     * @param aportacionAhorroMin        aportacion mensual minima a la cuenta de ahorro.
     * @param aportacionAhorroMax        aportacion mensual maxima a la cuenta de ahorro.
     * @param saldoAperturaAhorroMin     saldo inicial minimo de la cuenta de ahorro.
     * @param saldoAperturaAhorroMax     saldo inicial maximo de la cuenta de ahorro.
     * @param prestamoCapitalMin         capital minimo de un prestamo si lo hay.
     * @param prestamoCapitalMax         capital maximo de un prestamo si lo hay.
     * @param prestamoTasaAnual          tasa anual aplicada al prestamo (ej: 0.075 = 7.5%).
     * @param prestamoPlazoMesesMin      plazo minimo del prestamo en meses.
     * @param prestamoPlazoMesesMax      plazo maximo del prestamo en meses.
     */
    public SubPerfil(String nombre,
                     int ingresoMin, int ingresoMax,
                     int viviendaMin, int viviendaMax,
                     double probHipoteca, double probDeuda,
                     int deudaMin, int deudaMax,
                     double multiplicadorOcio,
                     int ahorroMin, int ahorroMax,
                     double probComisionImpago,
                     double probPagoMinimoCredito,
                     double intensidadUsoCreditoMin, double intensidadUsoCreditoMax,
                     double aportacionAhorroMin, double aportacionAhorroMax,
                     double saldoAperturaAhorroMin, double saldoAperturaAhorroMax,
                     double prestamoCapitalMin, double prestamoCapitalMax,
                     double prestamoTasaAnual,
                     int prestamoPlazoMesesMin, int prestamoPlazoMesesMax) {
        this.nombre = nombre;
        this.ingresoMin = ingresoMin; this.ingresoMax = ingresoMax;
        this.viviendaMin = viviendaMin; this.viviendaMax = viviendaMax;
        this.probHipoteca = probHipoteca; this.probDeuda = probDeuda;
        this.deudaMin = deudaMin; this.deudaMax = deudaMax;
        this.multiplicadorOcio = multiplicadorOcio;
        this.ahorroMin = ahorroMin; this.ahorroMax = ahorroMax;
        this.probComisionImpago = probComisionImpago;
        this.probPagoMinimoCredito = probPagoMinimoCredito;
        this.intensidadUsoCreditoMin = intensidadUsoCreditoMin;
        this.intensidadUsoCreditoMax = intensidadUsoCreditoMax;
        this.aportacionAhorroMin = aportacionAhorroMin;
        this.aportacionAhorroMax = aportacionAhorroMax;
        this.saldoAperturaAhorroMin = saldoAperturaAhorroMin;
        this.saldoAperturaAhorroMax = saldoAperturaAhorroMax;
        this.prestamoCapitalMin = prestamoCapitalMin;
        this.prestamoCapitalMax = prestamoCapitalMax;
        this.prestamoTasaAnual = prestamoTasaAnual;
        this.prestamoPlazoMesesMin = prestamoPlazoMesesMin;
        this.prestamoPlazoMesesMax = prestamoPlazoMesesMax;
    }

    /** @return nombre del sub-perfil. */
    public String getNombre() { return nombre; }
    /** @return ingreso mensual minimo. */
    public int getIngresoMin() { return ingresoMin; }
    /** @return ingreso mensual maximo. */
    public int getIngresoMax() { return ingresoMax; }
    /** @return gasto minimo en vivienda. */
    public int getViviendaMin() { return viviendaMin; }
    /** @return gasto maximo en vivienda. */
    public int getViviendaMax() { return viviendaMax; }
    /** @return probabilidad (0..1) de tener hipoteca. */
    public double getProbHipoteca() { return probHipoteca; }
    /** @return probabilidad (0..1) de tener deuda en tarjeta. */
    public double getProbDeuda() { return probDeuda; }
    /** @return deuda minima en tarjeta en euros. */
    public int getDeudaMin() { return deudaMin; }
    /** @return deuda maxima en tarjeta en euros. */
    public int getDeudaMax() { return deudaMax; }
    /** @return multiplicador del gasto en ocio. */
    public double getMultiplicadorOcio() { return multiplicadorOcio; }
    /** @return ahorro mensual minimo. */
    public int getAhorroMin() { return ahorroMin; }
    /** @return ahorro mensual maximo. */
    public int getAhorroMax() { return ahorroMax; }
    /** @return probabilidad de comision por impago. */
    public double getProbComisionImpago() { return probComisionImpago; }
    /** @return probabilidad de pagar solo el minimo de la tarjeta. */
    public double getProbPagoMinimoCredito() { return probPagoMinimoCredito; }
    /** @return intensidad minima de uso de la tarjeta. */
    public double getIntensidadUsoCreditoMin() { return intensidadUsoCreditoMin; }
    /** @return intensidad maxima de uso de la tarjeta. */
    public double getIntensidadUsoCreditoMax() { return intensidadUsoCreditoMax; }
    /** @return aportacion mensual minima a la cuenta de ahorro. */
    public double getAportacionAhorroMin() { return aportacionAhorroMin; }
    /** @return aportacion mensual maxima a la cuenta de ahorro. */
    public double getAportacionAhorroMax() { return aportacionAhorroMax; }
    /** @return saldo inicial minimo de la cuenta de ahorro. */
    public double getSaldoAperturaAhorroMin() { return saldoAperturaAhorroMin; }
    /** @return saldo inicial maximo de la cuenta de ahorro. */
    public double getSaldoAperturaAhorroMax() { return saldoAperturaAhorroMax; }
    /** @return capital minimo del prestamo. */
    public double getPrestamoCapitalMin() { return prestamoCapitalMin; }
    /** @return capital maximo del prestamo. */
    public double getPrestamoCapitalMax() { return prestamoCapitalMax; }
    /** @return tasa anual del prestamo (ej: 0.075 = 7.5%). */
    public double getPrestamoTasaAnual() { return prestamoTasaAnual; }
    /** @return plazo minimo del prestamo en meses. */
    public int getPrestamoPlazoMesesMin() { return prestamoPlazoMesesMin; }
    /** @return plazo maximo del prestamo en meses. */
    public int getPrestamoPlazoMesesMax() { return prestamoPlazoMesesMax; }
}
