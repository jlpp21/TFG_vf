package com.example.fintechrecommender.service;

import com.example.fintechrecommender.model.Movimiento;
import com.example.fintechrecommender.model.ObjetivoUsuario;
import com.example.fintechrecommender.model.Usuario;
import com.example.fintechrecommender.repository.MovimientoRepository;
import com.example.fintechrecommender.repository.ObjetivoUsuarioRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Servicio que genera las recomendaciones financieras del usuario usando IA.
 *
 * Es el orquestador principal del modulo de recomendaciones:
 * 1. Recolecta el contexto del cliente (saldos por tipo de cuenta, ingreso
 *    y gasto medio mensual, top categorias de gasto, objetivos declarados).
 * 2. Construye un prompt detallado con esa ficha de cliente.
 * 3. Lo manda al LLM via OllamaClient con un system prompt que actua como
 *    asesor financiero profesional (EFPA).
 * 4. Parsea la respuesta JSON y filtra "alucinaciones" (recomendaciones
 *    que contradicen los datos reales del cliente, por ejemplo decir
 *    "liquida tu deuda de tarjeta" cuando el usuario no tiene deuda).
 *
 * Devuelve siempre un JSON con tres campos: diagnostico, alertas y
 * recomendaciones (lista priorizada).
 */
@Service
public class RecomendacionIaService {

    /**
     * Prompt de sistema que define el rol del asesor financiero, las reglas
     * que debe seguir, los instrumentos que puede recomendar y el formato
     * exacto del JSON de salida. Se envia tal cual al LLM en cada peticion.
     */
    private static final String SYSTEM_PROMPT =
            "Eres un asesor financiero certificado (EFPA) en Espana, con conocimiento "
          + "profundo de productos bancarios espanoles, fiscalidad (IRPF, plusvalias, "
          + "deducciones autonomicas), MiFID II y planificacion patrimonial.\n\n"
          + "Tu rol:\n"
          + "- Das consejos PROFESIONALES y especificos, no consejos genericos de revista.\n"
          + "- Hablas en espanol de Espana, con tono cercano y profesional.\n"
          + "- Te diriges al cliente de tu.\n\n"
          + "PROHIBIDO (un asesor profesional NO da estos consejos):\n"
          + "- 'Ahorra mas' / 'gasta menos' / 'haz un presupuesto' / 'revisa tus suscripciones': "
          + "es lo que diria cualquier persona, no un asesor. Solo lo dices si tiene un "
          + "matiz tecnico concreto (ej: 'aplica la regla 50/30/20 con tu nomina actual de X EUR').\n"
          + "- Recomendar 'cuenta corriente' como producto: la cuenta corriente no rinde, no es un producto de inversion.\n"
          + "- Frases vacias tipo 'establece metas' o 'crea un plan': son inutiles sin instrumento concreto.\n\n"
          + "OBLIGATORIO en cada recomendacion:\n"
          + "- Mencionar un INSTRUMENTO CONCRETO: cuenta remunerada, deposito a plazo fijo, "
          + "Letras del Tesoro, fondo indexado al MSCI World / S&P 500 / FTSE All-World, "
          + "ETF de renta fija europea, plan de pensiones (PIAS, PPA), Plan de Empleo, "
          + "Cuenta Vivienda, hipoteca a tipo fijo / variable, amortizacion anticipada, "
          + "robo-advisor (gestion automatizada), traspaso entre fondos sin peaje fiscal, "
          + "DCA (dollar cost averaging) mensual, etc.\n"
          + "- Aportar UN NUMERO concreto cuando sea posible: porcentaje del salario "
          + "(p.ej. 15-20% a renta variable), TER objetivo de un fondo (<0.30%), TAE "
          + "minima aceptable de un deposito (>=2.5% en 2026), umbral de aportacion al "
          + "plan de pensiones (1.500 EUR/ano deducibles + 8.500 EUR si plan de empleo).\n"
          + "- Justificar con un DATO REAL del contexto del cliente "
          + "(p.ej. 'tu capacidad de ahorro de X EUR/mes permite...', 'con tus Y EUR en "
          + "cuenta corriente sin remunerar pierdes Z EUR/ano de poder adquisitivo a "
          + "una inflacion del 3%').\n"
          + "- Considerar la fiscalidad espanola: traspasos entre fondos sin tributar, "
          + "tributacion del ahorro al 19/21/23/27%, deducciones por aportacion a planes "
          + "de pensiones, exenciones por reinversion en vivienda habitual.\n\n"
          + "Reglas duras - debes seguirlas estrictamente:\n"
          + "1. SOLO recomiendas acciones basadas en datos del contexto. NUNCA inventes "
          + "deudas, ingresos, productos o saldos que no aparezcan.\n"
          + "2. Si saldos_eur.credito >= 0 el cliente NO tiene deuda de tarjeta; no menciones "
          + "'liquidar tarjeta'.\n"
          + "3. Si saldos_eur.prestamo >= 0 el cliente NO tiene prestamo; no menciones "
          + "'liquidar prestamo'.\n"
          + "4. Solo hablas de las cuentas que aparecen en cuentas_conectadas.\n"
          + "5. No mencionas marcas comerciales concretas ('fondo indexado al MSCI World' "
          + "es OK, 'Vanguard FTSE All-World' o 'MyInvestor' NO).\n"
          + "6. Antes de invertir, fondo de emergencia >= 3 meses de gastos. Calculalo: "
          + "gastos_mensuales_eur * 3 vs saldos_eur.ahorro + saldos_eur.corriente.\n"
          + "7. Si saldos_eur.credito < 0, liquidar la tarjeta tiene prioridad sobre "
          + "invertir (TAE de tarjeta >18% > rentabilidad esperada de cualquier inversion).\n"
          + "8. Plazo CORTO (<2 anos): cuenta remunerada, deposito a plazo fijo, Letras "
          + "del Tesoro. NO renta variable ni cripto.\n"
          + "9. Plazo MEDIO (2-5 anos): perfil mixto (60% renta fija + 40% renta variable, "
          + "ajustando segun perfil de riesgo). Considera fondos mixtos defensivos.\n"
          + "10. Plazo LARGO (>5 anos): renta variable diversificada (fondo indexado al "
          + "MSCI World / FTSE All-World, ETF S&P 500), DCA mensual, plan de pensiones "
          + "si tributa marginal alto.\n"
          + "11. Para 'comprar_piso' a LARGO plazo: cuenta vivienda solo si plan a 4 "
          + "anos vista; si no, fondos indexados con DCA y traspaso a renta fija a "
          + "medida que se acerca el objetivo.\n"
          + "12. Para 'plan_jubilacion': prioriza planes de pensiones con TER bajo "
          + "(<1%), aprovecha deduccion fiscal anual.\n"
          + "13. CIFRAS CON LAS QUE TRABAJAR:\n"
          + "   - 'BALANCE GLOBAL' es el patrimonio total acumulado del cliente (suma de todos los saldos).\n"
          + "   - 'Saldo en cuenta CORRIENTE/AHORRO/CREDITO/PRESTAMO' es el saldo de cada cuenta.\n"
          + "   - 'Capacidad de ahorro mensual NETA' es un FLUJO mensual (ingreso - gasto), NO un saldo. NUNCA digas 'tienes ahorrados X EUR' citando este numero.\n"
          + "   - 'Ingreso medio mensual' y 'Gasto medio mensual' son medias mensuales sobre los ultimos meses con datos.\n"
          + "   Cita SIEMPRE las cifras tal y como aparecen en la ficha; no las confundas entre si y no las inventes.\n\n"
          + "Formato de salida:\n"
          + "- Devuelves SIEMPRE un unico JSON valido, sin texto antes ni despues.\n"
          + "- Cada recomendacion responde a una situacion concreta del contexto y "
          + "menciona instrumento + numero + justificacion con dato del cliente.\n"
          + "- Si no puedes recomendar con seguridad, 'recomendaciones: []' y explicas "
          + "el motivo en 'diagnostico'.";

    private final ObjectMapper mapper = new ObjectMapper();

    private final MovimientoRepository movRepo;
    private final ObjetivoUsuarioRepository objRepo;
    private final OllamaClient ollama;

    /**
     * Construye el servicio con sus dependencias.
     *
     * @param movRepo repositorio de movimientos del usuario.
     * @param objRepo repositorio de objetivos del usuario.
     * @param ollama  cliente HTTP para hablar con Ollama.
     */
    public RecomendacionIaService(MovimientoRepository movRepo,
                                  ObjetivoUsuarioRepository objRepo,
                                  OllamaClient ollama) {
        this.movRepo = movRepo;
        this.objRepo = objRepo;
        this.ollama = ollama;
    }

    /**
     * Genera el JSON de recomendaciones para el usuario indicado.
     * Recolecta el contexto, llama a Ollama, parsea la respuesta y
     * filtra alucinaciones antes de devolver el resultado.
     *
     * @param usuario usuario para el que se generan las recomendaciones.
     * @return JsonNode con campos "diagnostico", "alertas" y "recomendaciones".
     *         Si el LLM devuelve algo no parseable, devuelve un JSON de error con la respuesta cruda.
     */
    @Transactional(readOnly = true)
    public JsonNode generar(Usuario usuario) {
        ContextoCliente ctx = recolectarContexto(usuario);
        String userPrompt = construirUserPrompt(ctx);

        String respuesta = ollama.chatJson(SYSTEM_PROMPT, userPrompt);
        JsonNode parseado;
        try {
            parseado = mapper.readTree(respuesta);
        } catch (Exception e) {
            ObjectNode err = mapper.createObjectNode();
            err.put("diagnostico", "La IA devolvio una respuesta no parseable");
            err.set("alertas", mapper.createArrayNode().add(respuesta));
            err.set("recomendaciones", mapper.createArrayNode());
            return err;
        }
        return filtrarAlucinaciones(parseado, ctx);
    }

    /**
     * Elimina alertas y recomendaciones que contradicen los datos reales del
     * cliente (ej: "liquidar tarjeta" cuando no tiene cuenta CREDITO en
     * negativo, o "abrir cuenta de ahorro" cuando ya tiene una conectada).
     *
     * @param payload JSON crudo devuelto por el LLM.
     * @param ctx     contexto del cliente con datos reales para contrastar.
     * @return mismo JSON pero con alertas y recomendaciones filtradas.
     */
    private JsonNode filtrarAlucinaciones(JsonNode payload, ContextoCliente ctx) {
        if (!payload.isObject()) return payload;
        ObjectNode obj = (ObjectNode) payload;

        Double credito = ctx.saldos.get("credito");
        Double prestamo = ctx.saldos.get("prestamo");
        boolean hayDeudaCredito = credito != null && credito < 0;
        boolean hayDeudaPrestamo = prestamo != null && prestamo < 0;
        boolean tieneCorriente = ctx.tiposConectados.contains("corriente");
        boolean tieneAhorro = ctx.tiposConectados.contains("ahorro");

        if (obj.has("alertas") && obj.get("alertas").isArray()) {
            ArrayNode alertas = mapper.createArrayNode();
            for (JsonNode a : obj.get("alertas")) {
                String txt = a.asText("").toLowerCase();
                if (!hayDeudaCredito && (txt.contains("tarjeta") || txt.contains("credito"))
                        && (txt.contains("deuda") || txt.contains("liquidar"))) continue;
                if (!hayDeudaPrestamo && txt.contains("prestamo")
                        && (txt.contains("liquidar") || txt.contains("deuda"))) continue;
                alertas.add(a);
            }
            obj.set("alertas", alertas);
        }

        if (obj.has("recomendaciones") && obj.get("recomendaciones").isArray()) {
            ArrayNode recs = mapper.createArrayNode();
            for (JsonNode r : obj.get("recomendaciones")) {
                String titulo = r.path("titulo").asText("").toLowerCase();
                String porque = r.path("porque").asText("").toLowerCase();
                String texto = titulo + " " + porque;

                if (!hayDeudaCredito
                        && (texto.contains("liquidar tarjeta") || texto.contains("liquidar la tarjeta")
                            || texto.contains("liquidar deuda de tarjeta")
                            || texto.contains("pagar tarjeta") || texto.contains("pagar la deuda en tarjeta"))) continue;
                if (!hayDeudaPrestamo
                        && (texto.contains("liquidar prestamo") || texto.contains("liquidar el prestamo"))) continue;
                if (!tieneCorriente && texto.contains("cuenta corriente")) continue;
                if (!tieneAhorro && texto.contains("cuenta de ahorro") && !texto.contains("abrir")) continue;

                recs.add(r);
            }
            obj.set("recomendaciones", recs);
        }
        return obj;
    }

    /**
     * Estructura interna que reune todos los datos del cliente que se
     * envian al LLM como contexto: perfil, ingresos y gastos medios,
     * saldos por tipo de cuenta, top categorias de gasto y objetivos.
     */
    private static class ContextoCliente {
        String perfil;
        double ingresoMedio, gastoMedio, ahorroMedio;
        double balanceGlobal;
        int mesesConDatos;
        Map<String, Double> saldos = new LinkedHashMap<>();
        java.util.Set<String> tiposConectados = new java.util.LinkedHashSet<>();
        Map<String, Double> topCategorias = new LinkedHashMap<>();
        List<ObjetivoUsuario> objetivos;
    }

    /**
     * Construye el ContextoCliente leyendo los movimientos y objetivos del usuario.
     * Calcula saldos por tipo de cuenta, balance global, ingreso/gasto medio
     * mensual y top 5 categorias de gasto sobre los ultimos 12 meses con datos.
     *
     * @param usuario usuario del que se recolecta el contexto.
     * @return objeto ContextoCliente listo para serializar al prompt.
     */
    private ContextoCliente recolectarContexto(Usuario usuario) {
        ContextoCliente c = new ContextoCliente();
        c.perfil = usuario.getPerfilFinanciero() != null ? usuario.getPerfilFinanciero().name() : "no_disponible";
        c.objetivos = objRepo.findByUsuarioOrderByFechaCreacionAsc(usuario);

        List<Movimiento> movs = movRepo.findAllByUsuarioOrderedAsc(usuario);
        LocalDateTime ahora = LocalDateTime.now();
        LocalDateTime corte = ahora.minusMonths(12);
        double ingresos12m = 0, gastos12m = 0;
        BigDecimal balanceTotal = BigDecimal.ZERO;
        Map<String, BigDecimal> saldosBd = new LinkedHashMap<>();
        Map<String, Double> gastoCat = new LinkedHashMap<>();
        LocalDateTime fechaMasAntiguaUltimo12m = null;

        for (Movimiento m : movs) {
            BigDecimal monto = m.getMonto();
            balanceTotal = balanceTotal.add(monto);
            String tipo = m.getBanco() != null && m.getBanco().getTipoCuenta() != null
                    ? m.getBanco().getTipoCuenta().toLowerCase() : "corriente";
            c.tiposConectados.add(tipo);
            saldosBd.merge(tipo, monto, BigDecimal::add);
            if (m.getFecha().isAfter(corte)) {
                if (fechaMasAntiguaUltimo12m == null || m.getFecha().isBefore(fechaMasAntiguaUltimo12m)) {
                    fechaMasAntiguaUltimo12m = m.getFecha();
                }
                double v = monto.doubleValue();
                if (v > 0) ingresos12m += v;
                else {
                    gastos12m += -v;
                    String cat = m.getCategoria() != null ? m.getCategoria() : "Otros";
                    gastoCat.merge(cat, -v, Double::sum);
                }
            }
        }

        // Divide by actual months of data (capped at 12) to avoid skewed averages
        // when the user has less than a year of history.
        int mesesConDatos = 12;
        if (fechaMasAntiguaUltimo12m != null) {
            long meses = java.time.temporal.ChronoUnit.MONTHS.between(
                    fechaMasAntiguaUltimo12m.withDayOfMonth(1),
                    ahora.withDayOfMonth(1)) + 1;
            mesesConDatos = (int) Math.max(1, Math.min(12, meses));
        }
        c.mesesConDatos = mesesConDatos;
        c.ingresoMedio = redondear(ingresos12m / mesesConDatos);
        c.gastoMedio = redondear(gastos12m / mesesConDatos);
        c.ahorroMedio = redondear(c.ingresoMedio - c.gastoMedio);
        c.balanceGlobal = balanceTotal.setScale(2, RoundingMode.HALF_UP).doubleValue();
        for (String t : c.tiposConectados) {
            c.saldos.put(t, saldosBd.getOrDefault(t, BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP).doubleValue());
        }
        gastoCat.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue(Comparator.reverseOrder()))
                .limit(5)
                .forEach(e -> c.topCategorias.put(e.getKey(), redondear(e.getValue())));
        return c;
    }

    /**
     * Construye el prompt de usuario en texto plano con la ficha del cliente
     * que se envia al LLM. Incluye balance global, saldos por tipo, flujos
     * mensuales, deudas, calculo del fondo de emergencia, top categorias,
     * objetivos del cliente y un ejemplo de recomendacion buena vs mala.
     *
     * @param c contexto del cliente recolectado previamente.
     * @return texto del prompt listo para enviar al LLM.
     */
    private String construirUserPrompt(ContextoCliente c) {
        StringBuilder sb = new StringBuilder();
        sb.append("FICHA DEL CLIENTE\n");
        sb.append("=================\n\n");
        sb.append("Perfil declarado: ").append(c.perfil).append("\n\n");

        sb.append("PATRIMONIO ACTUAL (saldos acumulados, no flujos mensuales)\n");
        sb.append("---------------------------------------------------------\n");
        sb.append(String.format("BALANCE GLOBAL: %.2f EUR  <- suma de todos los saldos del cliente\n", c.balanceGlobal));
        if (c.tiposConectados.isEmpty()) {
            sb.append("El cliente no tiene ninguna cuenta conectada.\n");
        } else {
            for (Map.Entry<String, Double> e : c.saldos.entrySet()) {
                sb.append("- Saldo en cuenta ").append(e.getKey().toUpperCase())
                  .append(": ")
                  .append(String.format("%.2f", e.getValue())).append(" EUR\n");
            }
        }
        sb.append("\n");

        sb.append("FLUJOS MENSUALES (medias de los ultimos ").append(c.mesesConDatos)
          .append(" meses con datos)\n");
        sb.append("-------------------------------------------------\n");
        sb.append(String.format("Ingreso medio mensual (entradas): %.2f EUR\n", c.ingresoMedio));
        sb.append(String.format("Gasto medio mensual (salidas): %.2f EUR\n", c.gastoMedio));
        sb.append(String.format("Capacidad de ahorro mensual NETA: %.2f EUR/mes "
              + "(diferencia ingreso - gasto, NO confundir con saldo en cuenta de ahorro)\n\n",
              c.ahorroMedio));
        sb.append("CUENTAS QUE EL CLIENTE NO TIENE (PROHIBIDO mencionarlas en alertas o recomendaciones):\n");
        boolean alguna = false;
        for (String t : new String[]{"corriente", "ahorro", "credito", "prestamo"}) {
            if (!c.tiposConectados.contains(t)) {
                sb.append("- ").append(t.toUpperCase()).append(" (no conectada)\n");
                alguna = true;
            }
        }
        if (!alguna) sb.append("(ninguna; el cliente tiene los 4 tipos de cuenta)\n");
        sb.append("\n");

        sb.append("DEUDAS\n");
        sb.append("------\n");
        Double credito = c.saldos.get("credito");
        Double prestamo = c.saldos.get("prestamo");
        boolean hayDeudaCredito = credito != null && credito < 0;
        boolean hayDeudaPrestamo = prestamo != null && prestamo < 0;
        if (hayDeudaCredito) sb.append(String.format("- DEUDA EN TARJETA DE CREDITO: %.2f EUR. SI puedes recomendar liquidarla.\n", Math.abs(credito)));
        else sb.append("- TARJETA DE CREDITO: 0 EUR de deuda. NO menciones 'liquidar tarjeta' ni 'deuda en tarjeta'.\n");
        if (hayDeudaPrestamo) sb.append(String.format("- PRESTAMO PENDIENTE: %.2f EUR. SI puedes recomendar amortizar.\n", Math.abs(prestamo)));
        else sb.append("- PRESTAMO: 0 EUR pendiente. NO menciones 'liquidar prestamo' ni 'deuda de prestamo'.\n");
        sb.append("\n");

        sb.append("FONDO DE EMERGENCIA\n");
        sb.append("-------------------\n");
        double colchonRequerido = redondear(c.gastoMedio * 3);
        double liquidez = (c.saldos.getOrDefault("corriente", 0.0)) + (c.saldos.getOrDefault("ahorro", 0.0));
        sb.append(String.format("Colchon recomendado (3 meses de gastos): %.2f EUR\n", colchonRequerido));
        sb.append(String.format("Liquidez actual (corriente + ahorro): %.2f EUR\n", liquidez));
        if (liquidez >= colchonRequerido) sb.append("=> El cliente YA tiene fondo de emergencia suficiente.\n");
        else sb.append("=> El cliente NO tiene fondo de emergencia suficiente.\n");
        sb.append("\n");

        sb.append("TOP CATEGORIAS DE GASTO (12 meses)\n");
        sb.append("----------------------------------\n");
        if (c.topCategorias.isEmpty()) sb.append("Sin datos.\n");
        else c.topCategorias.forEach((k, v) -> sb.append(String.format("- %s: %.2f EUR\n", k, v)));
        sb.append("\n");

        sb.append("OBJETIVOS DEL CLIENTE\n");
        sb.append("---------------------\n");
        if (c.objetivos.isEmpty()) sb.append("El cliente no ha definido objetivos.\n");
        else for (ObjetivoUsuario o : c.objetivos) {
            sb.append("- ").append(etiquetaObjetivo(o.getObjetivo()))
              .append(" (codigo: ").append(o.getObjetivo())
              .append(", plazo: ").append(o.getPlazo()).append(")\n");
        }
        sb.append("\n");

        sb.append("INSTRUCCIONES\n");
        sb.append("-------------\n");
        sb.append("Analiza la ficha como un asesor financiero certificado y devuelve SOLO\n");
        sb.append("el JSON de abajo. Maximo 5 recomendaciones, ordenadas por prioridad ascendente\n");
        sb.append("(1 = la mas urgente). Cada recomendacion debe:\n");
        sb.append("- Estar respaldada por un dato CONCRETO de la ficha (cita la cifra).\n");
        sb.append("- Mencionar un INSTRUMENTO especifico (no 'cuenta corriente' ni 'ahorrar mas').\n");
        sb.append("- Incluir un numero accionable (porcentaje, importe, TAE objetivo, TER, etc.).\n\n");
        sb.append("Ejemplo de recomendacion BUENA (sigue este nivel de detalle):\n");
        sb.append("  {\n");
        sb.append("    \"titulo\": \"Mover liquidez a cuenta remunerada\",\n");
        sb.append("    \"objetivo_relacionado\": null,\n");
        sb.append("    \"plazo\": \"CORTO\",\n");
        sb.append("    \"porque\": \"Tienes 14.500 EUR en cuenta corriente sin remunerar; con una cuenta al 2.75% TAE generarias ~399 EUR/ano sin asumir riesgo.\",\n");
        sb.append("    \"primer_paso\": \"Abre una cuenta remunerada con TAE >= 2.5% y traspasa los 11.500 EUR que sobrepasan tu colchon de 3 meses.\",\n");
        sb.append("    \"producto_sugerido\": \"cuenta remunerada al 2.5%-3.0% TAE\",\n");
        sb.append("    \"importe_mensual_sugerido_eur\": null,\n");
        sb.append("    \"prioridad\": 1\n");
        sb.append("  }\n\n");
        sb.append("Ejemplo de recomendacion MALA (NO la generes):\n");
        sb.append("  { \"titulo\": \"Revisar gastos mensuales\", \"porque\": \"Para ahorrar mas\", ... }\n");
        sb.append("  -> Generica, no menciona instrumento, no aporta numero, lo dice cualquiera.\n\n");
        sb.append("{\n");
        sb.append("  \"diagnostico\": \"1-2 frases sobre la salud financiera global\",\n");
        sb.append("  \"alertas\": [\"Cada string es una alerta concreta\"],\n");
        sb.append("  \"recomendaciones\": [\n");
        sb.append("    {\n");
        sb.append("      \"titulo\": \"max 8 palabras\",\n");
        sb.append("      \"objetivo_relacionado\": \"codigo_objetivo del cliente o null\",\n");
        sb.append("      \"plazo\": \"CORTO | MEDIO | LARGO\",\n");
        sb.append("      \"porque\": \"1 frase justificando con un dato de la ficha\",\n");
        sb.append("      \"primer_paso\": \"1 accion concreta esta semana\",\n");
        sb.append("      \"producto_sugerido\": \"tipologia, sin marcas\",\n");
        sb.append("      \"importe_mensual_sugerido_eur\": null,\n");
        sb.append("      \"prioridad\": 1\n");
        sb.append("    }\n");
        sb.append("  ]\n");
        sb.append("}\n");
        return sb.toString();
    }

    /**
     * Redondea un double a 2 decimales para que las cifras del prompt sean legibles.
     *
     * @param n numero a redondear.
     * @return numero redondeado a 2 decimales (HALF_UP).
     */
    private double redondear(double n) {
        return BigDecimal.valueOf(n).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    /**
     * Convierte el codigo tecnico de un objetivo en un texto legible
     * para el LLM (ej: "comprar_piso" -> "Comprar un piso o casa").
     *
     * @param codigo codigo del objetivo.
     * @return etiqueta en espanol o el propio codigo si no esta mapeado.
     */
    private String etiquetaObjetivo(String codigo) {
        switch (codigo) {
            case "comprar_piso":           return "Comprar un piso o casa";
            case "inversion_inmobiliaria": return "Comprar locales o inmuebles para alquilar";
            case "inversion_cripto":       return "Invertir en criptomonedas";
            case "inversion_empresas":     return "Invertir en empresas o startups";
            case "inversion_bolsa":        return "Invertir en bolsa o fondos indexados";
            case "plan_jubilacion":        return "Plan de jubilacion";
            case "fondo_emergencia":       return "Crear un fondo de emergencia";
            case "ahorrar_capital":        return "Ahorrar y hacer crecer mi patrimonio";
            case "dedicarse_inversion":    return "Dedicarme a la inversion / vivir de las rentas";
            case "estudios":               return "Pagar estudios";
            default:                       return codigo;
        }
    }
}
