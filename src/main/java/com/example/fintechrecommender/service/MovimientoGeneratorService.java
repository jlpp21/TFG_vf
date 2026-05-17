package com.example.fintechrecommender.service;

import com.example.fintechrecommender.model.BancoConectado;
import com.example.fintechrecommender.model.Movimiento;
import com.example.fintechrecommender.model.perfil.PerfilFinanciero;
import com.example.fintechrecommender.model.perfil.SubPerfil;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Servicio que genera movimientos ficticios para una cuenta bancaria recien conectada.
 *
 * Soporta los 4 tipos de cuenta del sistema (CORRIENTE, AHORRO, CREDITO, PRESTAMO)
 * y construye un historial de 3 a 5 anyos basado en el PerfilFinanciero del
 * usuario y un SubPerfil aleatorio. Asi cada usuario ve movimientos coherentes
 * con su perfil declarado (un usuario "endeudado cronico" tiene mas comisiones
 * y deuda; un "holgado" tiene mas capacidad de ahorro). Las descripciones usan
 * nombres reales de comercios espanoles para que el resultado sea creible.
 */
@Service
public class MovimientoGeneratorService {

    private final Random rnd = new Random();

    // ====== Comercios reales por categoria ======
    private static final List<String> SUPERMERCADO = Arrays.asList(
            "Mercadona", "Carrefour", "Lidl", "Dia", "Alcampo", "Eroski",
            "Consum", "El Corte Ingles", "Aldi");
    private static final List<String> RESTAURACION = Arrays.asList(
            "Glovo", "Just Eat", "Uber Eats", "100 Montaditos", "VIPS", "Telepizza",
            "Burger King", "McDonalds", "Starbucks", "Goiko Grill", "La Tagliatella",
            "Cafeteria del barrio", "Restaurante La Marisma");
    private static final List<String> TRANSPORTE = Arrays.asList(
            "Metro Madrid", "TMB Barcelona", "EMT Madrid", "Renfe Cercanias",
            "Cabify", "Free Now", "Uber", "BlaBlaCar");
    private static final List<String> COMBUSTIBLE = Arrays.asList(
            "Repsol", "Cepsa", "BP", "Galp", "Shell");
    private static final List<String> COMPRAS = Arrays.asList(
            "Amazon", "AliExpress", "PcComponentes", "MediaMarkt", "FNAC",
            "Decathlon", "IKEA", "Leroy Merlin");
    private static final List<String> ROPA = Arrays.asList(
            "Zara", "H&M", "Mango", "Bershka", "Pull&Bear", "Stradivarius",
            "Massimo Dutti", "Springfield");
    private static final List<String> SALUD = Arrays.asList(
            "Sanitas", "Adeslas", "Mapfre Salud", "Farmacia Central", "Clinica Dental Vitaldent");
    private static final List<String> SUMINISTROS = Arrays.asList(
            "Iberdrola", "Endesa", "Naturgy", "Repsol Energia", "TotalEnergies");
    private static final List<String> AGUA = Arrays.asList(
            "Canal Isabel II", "Aguas de Barcelona", "Emasesa");
    private static final List<String> TELEFONIA = Arrays.asList(
            "Movistar", "Vodafone", "Orange", "Yoigo", "Lowi", "O2", "Pepephone", "MasMovil");
    private static final List<String> GYM = Arrays.asList(
            "Basic-Fit", "McFit", "Altafit", "Anytime Fitness", "VivaGym");
    private static final List<String> AEROLINEAS = Arrays.asList(
            "Iberia", "Vueling", "Ryanair", "Air Europa");
    private static final List<String> SEGUROS = Arrays.asList(
            "Mapfre Seguros", "Mutua Madrilena", "AXA Seguros", "Linea Directa", "Reale");
    private static final List<String> BIZUM_IN = Arrays.asList(
            "Bizum recibido - Maria", "Bizum recibido - Carlos", "Bizum recibido - Ana",
            "Bizum recibido - Jorge", "Bizum recibido - Lucia");
    private static final List<String> BIZUM_OUT = Arrays.asList(
            "Bizum enviado - Maria", "Bizum enviado - Carlos", "Bizum enviado - Ana",
            "Bizum enviado - Jorge", "Bizum enviado - cuenta compartida");
    private static final List<String> DEUDA = Arrays.asList(
            "Cuota prestamo personal", "Tarjeta de credito - liquidacion",
            "Financiacion CaixaBank Consumer");

    // ====== Punto de entrada ======

    /**
     * Punto de entrada principal: genera la lista de movimientos para un banco
     * concreto en funcion del perfil del usuario y el tipo de cuenta.
     *
     * @param banco  banco al que se asociaran los movimientos.
     * @param perfil perfil financiero del usuario; condiciona los rangos numericos.
     * @return lista de movimientos generados (puede tener cientos de entradas).
     * @throws IllegalArgumentException si el perfil es null.
     */
    public List<Movimiento> generar(BancoConectado banco, PerfilFinanciero perfil) {
        if (perfil == null) {
            throw new IllegalArgumentException("Perfil financiero requerido para generar movimientos");
        }
        SubPerfil sub = elegir(perfil.getSubVariantes());
        String tipo = banco.getTipoCuenta() != null ? banco.getTipoCuenta() : "CORRIENTE";
        switch (tipo) {
            case "AHORRO":   return generarComoAhorro(banco, sub);
            case "CREDITO":  return generarComoCredito(banco, perfil, sub);
            case "PRESTAMO": return generarComoPrestamo(banco, perfil, sub);
            default:         return generarComoCorriente(banco, perfil, sub);
        }
    }

    // ====== Helpers ======

    /**
     * Devuelve un numero aleatorio dentro de un rango.
     *
     * @param min minimo (inclusive).
     * @param max maximo (exclusive).
     * @return valor aleatorio en [min, max).
     */
    private double aleatorio(double min, double max) { return rnd.nextDouble() * (max - min) + min; }

    /**
     * Devuelve un entero aleatorio dentro de un rango.
     *
     * @param min minimo (inclusive).
     * @param max maximo (inclusive).
     * @return valor aleatorio en [min, max].
     */
    private int aleatorioInt(int min, int max) { return rnd.nextInt(max - min + 1) + min; }

    /**
     * Elige un elemento aleatorio de la lista.
     *
     * @param lista lista no vacia.
     * @param <T>   tipo de elemento.
     * @return elemento aleatorio.
     */
    private <T> T elegir(List<T> lista) { return lista.get(rnd.nextInt(lista.size())); }

    /**
     * Redondea un double a 2 decimales como BigDecimal (para guardar dinero).
     *
     * @param n numero a redondear.
     * @return BigDecimal con escala 2 y redondeo HALF_UP.
     */
    private BigDecimal redondear(double n) { return BigDecimal.valueOf(n).setScale(2, RoundingMode.HALF_UP); }

    /**
     * Construye una fecha en el anyo, mes y dia indicados con hora aleatoria.
     * Limita el dia a [1, 28] para evitar problemas de febrero.
     *
     * @param anio anyo.
     * @param mes0 mes empezando en 0 (0 = enero, 11 = diciembre).
     * @param dia  dia del mes (se ajusta a 1..28).
     * @return LocalDateTime con la fecha y hora aleatoria.
     */
    private LocalDateTime fechaEn(int anio, int mes0, int dia) {
        int diaCapped = Math.max(1, Math.min(dia, 28));
        return LocalDateTime.of(anio, mes0 + 1, diaCapped, aleatorioInt(0, 23), aleatorioInt(0, 59), 0);
    }

    /**
     * Crea una entidad Movimiento ya configurada lista para anyadir a la lista de salida.
     *
     * @param banco       banco al que pertenece.
     * @param fecha       fecha del movimiento.
     * @param descripcion texto descriptivo.
     * @param categoria   categoria del movimiento.
     * @param monto       importe (positivo ingreso, negativo gasto).
     * @return el Movimiento listo para guardar.
     */
    private Movimiento nuevo(BancoConectado banco, LocalDateTime fecha, String descripcion, String categoria, double monto) {
        Movimiento m = new Movimiento();
        m.setBanco(banco);
        m.setFecha(fecha);
        m.setDescripcion(descripcion);
        m.setCategoria(categoria);
        m.setMonto(redondear(monto));
        return m;
    }

    // ====== Generador CORRIENTE ======

    /**
     * Genera el historial completo de una cuenta corriente.
     * Crea un mes a mes el flujo de nominas, gastos fijos y variables, y
     * eventos anuales (vacaciones, navidad, IRPF, IBI, seguros). Al final
     * asegura que el balance acumulado nunca quede en negativo anyadiendo
     * un deposito inicial si hace falta.
     *
     * @param banco  banco al que se asocian los movimientos.
     * @param perfil perfil financiero del usuario.
     * @param sub    sub-perfil concreto con los rangos numericos.
     * @return lista con todos los movimientos del periodo.
     */
    private List<Movimiento> generarComoCorriente(BancoConectado banco, PerfilFinanciero perfil, SubPerfil sub) {
        int aniosHistoria = aleatorioInt(3, 5);
        double ingresoBase = aleatorio(sub.getIngresoMin(), sub.getIngresoMax());
        double gastoVivienda = aleatorio(sub.getViviendaMin(), sub.getViviendaMax());
        boolean tieneHipoteca = rnd.nextDouble() < sub.getProbHipoteca();
        boolean tieneDeuda = rnd.nextDouble() < sub.getProbDeuda();
        double cuotaDeuda = tieneDeuda ? aleatorio(sub.getDeudaMin(), sub.getDeudaMax()) : 0;
        int cuotasExtraDeuda = perfil == PerfilFinanciero.ENDEUDADO_CRONICO ? aleatorioInt(1, 2) : 0;

        List<Suscripcion> subs = construirSuscripciones();

        List<Movimiento> out = new ArrayList<>();
        LocalDateTime hoy = LocalDateTime.now();
        LocalDateTime cursor = hoy.minusYears(aniosHistoria).withDayOfMonth(1);

        while (!cursor.isAfter(hoy)) {
            int anio = cursor.getYear();
            int mes0 = cursor.getMonthValue() - 1;
            generarMes(out, banco, sub, anio, mes0, ingresoBase, gastoVivienda,
                    tieneHipoteca, cuotaDeuda, cuotasExtraDeuda, subs);
            cursor = cursor.plusMonths(1);
        }

        for (int a = hoy.getYear() - aniosHistoria; a <= hoy.getYear(); a++) {
            generarEventosAnuales(out, banco, sub, a, hoy);
        }

        asegurarBalancePositivo(out, banco);
        return out;
    }

    /**
     * Construye una lista aleatoria de suscripciones mensuales (Netflix, Spotify, etc.)
     * con probabilidades realistas.
     *
     * @return lista de suscripciones que se cargaran cada mes.
     */
    private List<Suscripcion> construirSuscripciones() {
        List<Suscripcion> subs = new ArrayList<>();
        subs.add(new Suscripcion("Netflix",      13.99, aleatorioInt(1, 10)));
        subs.add(new Suscripcion("Spotify",      10.99, aleatorioInt(1, 28)));
        subs.add(new Suscripcion("Amazon Prime",  4.99, aleatorioInt(1, 28)));
        if (rnd.nextDouble() < 0.55) {
            subs.add(new Suscripcion(elegir(Arrays.asList("HBO Max", "Disney+", "Filmin")), aleatorio(7, 11), aleatorioInt(1, 28)));
        }
        if (rnd.nextDouble() < 0.40) {
            subs.add(new Suscripcion("YouTube Premium", 11.99, aleatorioInt(1, 28)));
        }
        if (rnd.nextDouble() < 0.45) {
            subs.add(new Suscripcion("Gym " + elegir(GYM), aleatorio(25, 45), aleatorioInt(1, 5)));
        }
        return subs;
    }

    /**
     * Anyade a la lista los movimientos de un mes concreto: nomina, paga extra,
     * vivienda, suministros, telefonia, suscripciones, gastos variables (super,
     * restauracion, transporte, etc.), deuda y aportacion a ahorro.
     *
     * @param out               lista de salida donde se acumulan los movimientos.
     * @param banco             banco al que se asocian.
     * @param sub               sub-perfil del usuario.
     * @param anio              anyo del mes a generar.
     * @param mes0              mes 0..11.
     * @param ingresoBase       ingreso base mensual.
     * @param gastoVivienda     gasto fijo de vivienda.
     * @param tieneHipoteca     true si paga hipoteca, false si paga alquiler.
     * @param cuotaDeuda        cuota mensual de deuda (0 si no tiene).
     * @param cuotasExtraDeuda  cuotas extra al mes para perfiles muy endeudados.
     * @param subs              suscripciones mensuales del usuario.
     */
    private void generarMes(List<Movimiento> out, BancoConectado banco, SubPerfil sub,
                            int anio, int mes0, double ingresoBase, double gastoVivienda,
                            boolean tieneHipoteca, double cuotaDeuda, int cuotasExtraDeuda,
                            List<Suscripcion> subs) {
        double m = sub.getMultiplicadorOcio();

        // Ingresos
        double salario = ingresoBase * aleatorio(0.97, 1.03);
        out.add(nuevo(banco, fechaEn(anio, mes0, aleatorioInt(25, 28)), "Nomina mensual", "Ingresos", salario));

        if ((mes0 == 5 || mes0 == 11) && rnd.nextDouble() < 0.7) {
            out.add(nuevo(banco, fechaEn(anio, mes0, aleatorioInt(20, 24)), "Paga extra", "Ingresos", salario * aleatorio(0.7, 1.0)));
        }
        if (rnd.nextDouble() < 0.45) {
            out.add(nuevo(banco, fechaEn(anio, mes0, aleatorioInt(5, 25)), elegir(BIZUM_IN), "Ingresos", aleatorio(15, 150)));
        }

        // Gastos fijos
        String conceptoVivienda = tieneHipoteca ? "Cuota hipoteca" : "Alquiler vivienda";
        String categoriaVivienda = tieneHipoteca ? "Hipoteca" : "Vivienda";
        out.add(nuevo(banco, fechaEn(anio, mes0, aleatorioInt(1, 5)), conceptoVivienda, categoriaVivienda, -gastoVivienda));

        out.add(nuevo(banco, fechaEn(anio, mes0, aleatorioInt(5, 12)), elegir(SUMINISTROS), "Suministros", -aleatorio(45, 130)));
        if (rnd.nextDouble() < 0.7) {
            out.add(nuevo(banco, fechaEn(anio, mes0, aleatorioInt(8, 18)), elegir(AGUA), "Suministros", -aleatorio(20, 50)));
        }

        out.add(nuevo(banco, fechaEn(anio, mes0, aleatorioInt(1, 5)), elegir(TELEFONIA) + " Internet+TV", "Telefonia", -aleatorio(38, 65)));
        out.add(nuevo(banco, fechaEn(anio, mes0, aleatorioInt(5, 15)), elegir(TELEFONIA) + " Movil", "Telefonia", -aleatorio(8, 25)));

        for (Suscripcion s : subs) {
            out.add(nuevo(banco, fechaEn(anio, mes0, s.dia), s.nombre, "Suscripciones", -s.monto));
        }

        // Variables
        int visitasSuper = aleatorioInt(3, 6);
        for (int i = 0; i < visitasSuper; i++) {
            out.add(nuevo(banco, fechaEn(anio, mes0, aleatorioInt(1, 28)), elegir(SUPERMERCADO), "Supermercado", -aleatorio(28, 110) * m));
        }
        int visitasResto = aleatorioInt(0, 8);
        for (int i = 0; i < visitasResto; i++) {
            out.add(nuevo(banco, fechaEn(anio, mes0, aleatorioInt(1, 28)), elegir(RESTAURACION), "Restauracion", -aleatorio(8, 45) * m));
        }
        int viajes = aleatorioInt(3, 14);
        for (int i = 0; i < viajes; i++) {
            out.add(nuevo(banco, fechaEn(anio, mes0, aleatorioInt(1, 28)), elegir(TRANSPORTE), "Transporte", -aleatorio(1.5, 12)));
        }
        if (rnd.nextDouble() < 0.5) {
            int repostajes = aleatorioInt(1, 3);
            for (int i = 0; i < repostajes; i++) {
                out.add(nuevo(banco, fechaEn(anio, mes0, aleatorioInt(1, 28)), elegir(COMBUSTIBLE), "Combustible", -aleatorio(45, 80)));
            }
        }
        int compras = aleatorioInt(0, 4);
        for (int i = 0; i < compras; i++) {
            List<String> fuente = rnd.nextDouble() < 0.5 ? COMPRAS : ROPA;
            out.add(nuevo(banco, fechaEn(anio, mes0, aleatorioInt(1, 28)), elegir(fuente), "Compras", -aleatorio(15, 130) * m));
        }
        if (rnd.nextDouble() < 0.4) {
            out.add(nuevo(banco, fechaEn(anio, mes0, aleatorioInt(1, 28)), elegir(SALUD), "Salud", -aleatorio(8, 60)));
        }
        if (cuotaDeuda > 0) {
            out.add(nuevo(banco, fechaEn(anio, mes0, aleatorioInt(1, 10)), elegir(DEUDA), "Deuda", -cuotaDeuda));
            for (int i = 0; i < cuotasExtraDeuda; i++) {
                double extra = aleatorio(sub.getDeudaMin(), sub.getDeudaMax()) * 0.7;
                out.add(nuevo(banco, fechaEn(anio, mes0, aleatorioInt(10, 25)), elegir(DEUDA), "Deuda", -extra));
            }
        }
        if (sub.getProbComisionImpago() > 0 && rnd.nextDouble() < sub.getProbComisionImpago()) {
            out.add(nuevo(banco, fechaEn(anio, mes0, aleatorioInt(15, 28)),
                    "Comision por descubierto", "Comisiones", -aleatorio(15, 45)));
        }
        if (rnd.nextDouble() < 0.5) {
            out.add(nuevo(banco, fechaEn(anio, mes0, aleatorioInt(1, 28)), elegir(BIZUM_OUT), "Transferencias", -aleatorio(10, 80)));
        }
        if (sub.getAhorroMax() > 0 && rnd.nextDouble() < 0.6) {
            double ahorro = aleatorio(sub.getAhorroMin(), sub.getAhorroMax());
            if (ahorro > 0) {
                out.add(nuevo(banco, fechaEn(anio, mes0, 28), "Aportacion fondo de ahorro", "Ahorro", -ahorro));
            }
        }
    }

    /**
     * Anyade eventos anuales tipicos (vacaciones, Black Friday, regalos de
     * navidad, declaracion de IRPF, seguros, IBI) al historial de la cuenta.
     *
     * @param out   lista de salida donde se acumulan los movimientos.
     * @param banco banco al que se asocian.
     * @param sub   sub-perfil del usuario.
     * @param anio  anyo a generar.
     * @param hoy   fecha actual; se usa para no generar eventos futuros.
     */
    private void generarEventosAnuales(List<Movimiento> out, BancoConectado banco, SubPerfil sub,
                                       int anio, LocalDateTime hoy) {
        double m = sub.getMultiplicadorOcio();

        int mesVac = elegir(Arrays.asList(6, 7));
        LocalDateTime fVac = fechaEn(anio, mesVac, aleatorioInt(1, 20));
        if (!fVac.isAfter(hoy)) {
            double presupuesto = aleatorio(700, 2000) * m;
            out.add(nuevo(banco, fVac, elegir(AEROLINEAS) + " - vuelo verano", "Viajes", -presupuesto * 0.4));
            out.add(nuevo(banco, fechaEn(anio, mesVac, fVac.getDayOfMonth() + 1), "Booking.com - hotel verano", "Viajes", -presupuesto * 0.4));
            out.add(nuevo(banco, fechaEn(anio, mesVac, fVac.getDayOfMonth() + 5), "Gastos vacaciones", "Viajes", -presupuesto * 0.2));
        }

        if (rnd.nextDouble() < 0.7) {
            LocalDateTime f = fechaEn(anio, 10, aleatorioInt(20, 28));
            if (!f.isAfter(hoy)) out.add(nuevo(banco, f, "Amazon - Black Friday", "Compras", -aleatorio(80, 350) * m));
        }

        LocalDateTime fRegalos = fechaEn(anio, 11, aleatorioInt(15, 22));
        if (!fRegalos.isAfter(hoy)) {
            out.add(nuevo(banco, fRegalos, "Regalos Navidad", "Compras", -aleatorio(150, 500) * m));
            out.add(nuevo(banco, fechaEn(anio, 11, aleatorioInt(20, 24)), "Cena Navidad - supermercado", "Supermercado", -aleatorio(80, 250)));
        }

        if (rnd.nextDouble() < 0.85) {
            LocalDateTime f = fechaEn(anio, 5, aleatorioInt(15, 28));
            if (!f.isAfter(hoy)) out.add(nuevo(banco, f, "AEAT IRPF", "Impuestos", aleatorio(-450, 700)));
        }
        if (rnd.nextDouble() < 0.55) {
            LocalDateTime f = fechaEn(anio, aleatorioInt(0, 11), aleatorioInt(1, 28));
            if (!f.isAfter(hoy)) out.add(nuevo(banco, f, elegir(SEGUROS), "Seguros", -aleatorio(180, 600)));
        }
        if (rnd.nextDouble() < 0.4) {
            LocalDateTime f = fechaEn(anio, 9, aleatorioInt(1, 28));
            if (!f.isAfter(hoy)) out.add(nuevo(banco, f, "Ayuntamiento - IBI", "Impuestos", -aleatorio(120, 450)));
        }
    }

    /**
     * Si el balance acumulado del historial llega a negativo en algun momento,
     * inserta un deposito inicial al principio para que la cuenta nunca quede
     * en numeros rojos.
     *
     * @param trx   lista de movimientos generados (se modifica en sitio).
     * @param banco banco al que se asocian.
     */
    private void asegurarBalancePositivo(List<Movimiento> trx, BancoConectado banco) {
        trx.sort((a, b) -> a.getFecha().compareTo(b.getFecha()));
        BigDecimal balance = BigDecimal.ZERO;
        BigDecimal minimo = BigDecimal.ZERO;
        for (Movimiento t : trx) {
            balance = balance.add(t.getMonto());
            if (balance.compareTo(minimo) < 0) minimo = balance;
        }
        if (minimo.signum() < 0) {
            double colchon = aleatorio(200, 800);
            double depositoInicial = -minimo.doubleValue() + colchon;
            LocalDateTime primera = trx.get(0).getFecha().minusDays(1);
            trx.add(0, nuevo(banco, primera, "Apertura de cuenta - deposito inicial", "Ingresos", depositoInicial));
        }
    }

    // ====== Generador AHORRO ======

    /**
     * Genera el historial de una cuenta de ahorro: deposito inicial,
     * aportaciones mensuales, liquidacion trimestral de intereses y
     * retiradas ocasionales hacia la cuenta corriente.
     *
     * @param banco banco al que se asocian los movimientos.
     * @param sub   sub-perfil con los rangos de aportacion y saldo inicial.
     * @return lista de movimientos generados.
     */
    private List<Movimiento> generarComoAhorro(BancoConectado banco, SubPerfil sub) {
        int aniosHistoria = aleatorioInt(3, 5);
        double aportacionMensual = aleatorio(sub.getAportacionAhorroMin(), sub.getAportacionAhorroMax());
        double tasaAnual = aleatorio(0.005, 0.03);

        List<Movimiento> out = new ArrayList<>();
        LocalDateTime hoy = LocalDateTime.now();
        LocalDateTime cursor = hoy.minusYears(aniosHistoria).withDayOfMonth(1);

        double saldoApertura = aleatorio(sub.getSaldoAperturaAhorroMin(), sub.getSaldoAperturaAhorroMax());
        out.add(nuevo(banco, cursor.minusDays(1), "Apertura cuenta de ahorro", "Ingresos", saldoApertura));
        double saldo = saldoApertura;

        while (!cursor.isAfter(hoy)) {
            int anio = cursor.getYear();
            int mes0 = cursor.getMonthValue() - 1;
            if (aportacionMensual > 0) {
                double aportacion = aportacionMensual * aleatorio(0.85, 1.15);
                out.add(nuevo(banco, fechaEn(anio, mes0, aleatorioInt(1, 5)),
                        "Transferencia desde cuenta corriente", "Ingresos", aportacion));
                saldo += aportacion;
            }

            if (mes0 == 2 || mes0 == 5 || mes0 == 8 || mes0 == 11) {
                double interes = saldo * (tasaAnual / 4);
                out.add(nuevo(banco, fechaEn(anio, mes0, 28), "Liquidacion intereses", "Intereses", interes));
                saldo += interes;
            }

            if (rnd.nextDouble() < 0.15 && saldo > 500) {
                double retirada = aleatorio(100, Math.min(saldo - 100, 800));
                out.add(nuevo(banco, fechaEn(anio, mes0, aleatorioInt(15, 25)),
                        "Transferencia a cuenta corriente", "Transferencias", -retirada));
                saldo -= retirada;
            }
            cursor = cursor.plusMonths(1);
        }
        return out;
    }

    // ====== Generador CREDITO ======

    /**
     * Genera el historial de una tarjeta de credito: compras durante el mes,
     * liquidacion al final (total o pago minimo con intereses), cuota anual
     * y comisiones por exceso de limite para perfiles muy endeudados.
     *
     * @param banco  banco al que se asocian los movimientos.
     * @param perfil perfil financiero del usuario.
     * @param sub    sub-perfil con la intensidad de uso y probabilidad de pago minimo.
     * @return lista de movimientos generados.
     */
    private List<Movimiento> generarComoCredito(BancoConectado banco, PerfilFinanciero perfil, SubPerfil sub) {
        int aniosHistoria = aleatorioInt(3, 5);
        double intensidadUso = aleatorio(sub.getIntensidadUsoCreditoMin(), sub.getIntensidadUsoCreditoMax());

        List<Movimiento> out = new ArrayList<>();
        LocalDateTime hoy = LocalDateTime.now();
        LocalDateTime cursor = hoy.minusYears(aniosHistoria).withDayOfMonth(1);

        double saldoPendiente = 0;

        while (!cursor.isAfter(hoy)) {
            int anio = cursor.getYear();
            int mes0 = cursor.getMonthValue() - 1;

            int numCompras = (int) Math.floor(aleatorio(5, 20) * intensidadUso);
            double totalMes = 0;
            for (int i = 0; i < numCompras; i++) {
                CategoriaCompra c = elegirCategoriaCompra();
                double monto = aleatorio(c.min, c.max);
                totalMes += monto;
                out.add(nuevo(banco, fechaEn(anio, mes0, aleatorioInt(1, 27)),
                        elegir(c.fuente), c.categoria, -monto));
            }

            double deudaTotal = saldoPendiente + totalMes;
            if (deudaTotal > 0) {
                boolean pagoMinimo = rnd.nextDouble() < sub.getProbPagoMinimoCredito();
                if (pagoMinimo) {
                    double pago = deudaTotal * aleatorio(0.05, 0.15);
                    out.add(nuevo(banco, fechaEn(anio, mes0, 28),
                            "Pago minimo tarjeta - cargo cuenta corriente", "Liquidacion", pago));
                    double resto = deudaTotal - pago;
                    double intereses = resto * aleatorio(0.018, 0.025);
                    out.add(nuevo(banco, fechaEn(anio, mes0, 28),
                            "Intereses tarjeta credito", "Intereses", -intereses));
                    saldoPendiente = resto + intereses;
                } else {
                    out.add(nuevo(banco, fechaEn(anio, mes0, 28),
                            "Liquidacion tarjeta - cargo cuenta corriente", "Liquidacion", deudaTotal));
                    saldoPendiente = 0;
                }
            }

            if (mes0 == 0 && rnd.nextDouble() < 0.7) {
                out.add(nuevo(banco, fechaEn(anio, 0, aleatorioInt(10, 20)),
                        "Cuota anual tarjeta", "Comisiones", -aleatorio(30, 60)));
            }
            if (perfil == PerfilFinanciero.ENDEUDADO_CRONICO && rnd.nextDouble() < 0.25) {
                out.add(nuevo(banco, fechaEn(anio, mes0, aleatorioInt(1, 27)),
                        "Comision por exceso de limite", "Comisiones", -aleatorio(20, 40)));
            }
            cursor = cursor.plusMonths(1);
        }
        return out;
    }

    // ====== Generador PRESTAMO ======

    /**
     * Genera el historial de un prestamo personal: concesion (entrada de
     * capital), cuotas mensuales calculadas por amortizacion francesa con
     * la tasa anual del sub-perfil, y comisiones por impago para perfiles
     * cronicamente endeudados.
     *
     * @param banco  banco al que se asocian los movimientos.
     * @param perfil perfil del usuario; si es ENDEUDADO_CRONICO genera impagos.
     * @param sub    sub-perfil con el rango de capital, plazo y tasa.
     * @return lista de movimientos generados.
     */
    private List<Movimiento> generarComoPrestamo(BancoConectado banco, PerfilFinanciero perfil, SubPerfil sub) {
        List<Movimiento> out = new ArrayList<>();

        double capital = aleatorio(sub.getPrestamoCapitalMin(), sub.getPrestamoCapitalMax());
        int plazoMeses = aleatorioInt(sub.getPrestamoPlazoMesesMin(), sub.getPrestamoPlazoMesesMax());
        double tasaMensual = sub.getPrestamoTasaAnual() / 12.0;
        double cuota = capital * tasaMensual / (1 - Math.pow(1 + tasaMensual, -plazoMeses));

        LocalDateTime hoy = LocalDateTime.now();
        int mesesDesdeConcesion = aleatorioInt(Math.max(6, plazoMeses / 4), Math.min(plazoMeses + 12, plazoMeses + 24));
        int mesesPagados = Math.min(plazoMeses, mesesDesdeConcesion);

        LocalDateTime concesion = hoy.minusMonths(mesesDesdeConcesion);
        out.add(nuevo(banco, concesion, "Concesion prestamo personal", "Prestamo", -capital));

        double saldoPendiente = capital;
        for (int i = 1; i <= mesesPagados && saldoPendiente > 0.01; i++) {
            double interesMes = saldoPendiente * tasaMensual;
            double amortizacion = Math.min(cuota - interesMes, saldoPendiente);
            double cuotaReal = interesMes + amortizacion;

            LocalDateTime fecha = concesion.plusMonths(i);
            if (fecha.isAfter(hoy)) break;

            int dia = Math.max(1, Math.min(concesion.getDayOfMonth(), 28));
            fecha = fecha.withDayOfMonth(dia);

            boolean impago = perfil == PerfilFinanciero.ENDEUDADO_CRONICO && rnd.nextDouble() < 0.10;
            if (impago) {
                out.add(nuevo(banco, fecha, "Comision por impago de cuota", "Comisiones", -aleatorio(20, 45)));
                continue;
            }
            out.add(nuevo(banco, fecha, "Cuota prestamo personal", "Prestamo", cuotaReal));
            saldoPendiente -= amortizacion;
        }
        return out;
    }

    /**
     * Elige una categoria de compra aleatoria de entre las posibles para
     * generar movimientos en una tarjeta de credito.
     *
     * @return categoria con su lista de comercios y rango de importes.
     */
    private CategoriaCompra elegirCategoriaCompra() {
        List<CategoriaCompra> cats = Arrays.asList(
                new CategoriaCompra(COMPRAS,      "Compras",      15, 200),
                new CategoriaCompra(RESTAURACION, "Restauracion", 10,  80),
                new CategoriaCompra(ROPA,         "Ropa",         25, 150),
                new CategoriaCompra(COMBUSTIBLE,  "Combustible",  40,  80),
                new CategoriaCompra(SUPERMERCADO, "Supermercado", 30, 120)
        );
        return elegir(cats);
    }

    // ====== Estructuras auxiliares ======

    /**
     * Estructura interna que representa una suscripcion mensual fija
     * (Netflix, Spotify, Gym, etc.) con su importe y dia de cargo.
     */
    private static class Suscripcion {
        final String nombre; final double monto; final int dia;
        /**
         * @param n nombre del servicio.
         * @param m importe mensual.
         * @param d dia del mes en que se carga.
         */
        Suscripcion(String n, double m, int d) { nombre = n; monto = m; dia = d; }
    }

    /**
     * Estructura interna que agrupa una categoria de compra con su
     * lista de comercios y el rango de importes.
     */
    private static class CategoriaCompra {
        final List<String> fuente; final String categoria; final double min, max;
        /**
         * @param fuente    lista de comercios posibles.
         * @param categoria etiqueta de la categoria.
         * @param min       importe minimo.
         * @param max       importe maximo.
         */
        CategoriaCompra(List<String> fuente, String categoria, double min, double max) {
            this.fuente = fuente; this.categoria = categoria; this.min = min; this.max = max;
        }
    }
}
