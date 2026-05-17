// ======================
// Pagina: Recomendaciones (IA)
// ======================
// Pantalla que dispara la generacion de recomendaciones financieras con IA.
// Al hacer click en el boton, llama a POST /api/recomendaciones/ia, espera
// el JSON con diagnostico, alertas y recomendaciones, y los pinta en pantalla.
// La llamada puede tardar (depende de Ollama), por lo que muestra un spinner
// mientras dura.

/**
 * Mapa de codigos de objetivo a etiquetas legibles.
 * Se usa para mostrar el nombre del objetivo en cada tarjeta de recomendacion.
 */
const OBJETIVO_ETIQUETAS = {
    comprar_piso:           'Comprar un piso o casa',
    inversion_inmobiliaria: 'Comprar locales o inmuebles para alquilar',
    inversion_cripto:       'Invertir en criptomonedas',
    inversion_empresas:     'Invertir en empresas o startups',
    inversion_bolsa:        'Invertir en bolsa o fondos indexados',
    plan_jubilacion:        'Plan de jubilación',
    fondo_emergencia:       'Crear un fondo de emergencia',
    ahorrar_capital:        'Ahorrar y hacer crecer mi patrimonio',
    dedicarse_inversion:    'Dedicarme a la inversión / vivir de las rentas',
    estudios:               'Pagar estudios'
};

const btn = document.getElementById('btn-cargar-recomendaciones');
const hint = document.getElementById('recs-hint');
const loading = document.getElementById('recs-loading');
const resultado = document.getElementById('recs-resultado');
const diagnosticoTexto = document.getElementById('diagnostico-texto');
const alertasWrap = document.getElementById('alertas-wrap');
const listaAlertas = document.getElementById('lista-alertas');
const listaRecs = document.getElementById('lista-recomendaciones');

/**
 * Devuelve el HTML del badge de plazo segun el valor recibido.
 *
 * @param {string} plazo CORTO, MEDIO o LARGO (case-insensitive).
 * @returns {string} HTML del badge o cadena vacia si el plazo no es valido.
 */
function plazoBadge(plazo) {
    const p = (plazo || '').toUpperCase();
    if (p === 'CORTO') return '<span class="badge badge-plazo-corto">Corto plazo</span>';
    if (p === 'MEDIO') return '<span class="badge badge-plazo-medio">Medio plazo</span>';
    if (p === 'LARGO') return '<span class="badge badge-plazo-largo">Largo plazo</span>';
    return '';
}

/**
 * Devuelve el HTML del badge del objetivo asociado a la recomendacion.
 *
 * @param {string|null} codigo codigo del objetivo o null si la recomendacion es transversal.
 * @returns {string} HTML del badge.
 */
function objetivoBadge(codigo) {
    if (!codigo) return '<span class="badge badge-objetivo-transversal">Recomendación transversal</span>';
    const etq = OBJETIVO_ETIQUETAS[codigo] || codigo;
    return '<span class="badge badge-objetivo">' + etq + '</span>';
}

/**
 * Pinta en pantalla el JSON de recomendaciones devuelto por el backend:
 * diagnostico, alertas (si las hay) y la lista de recomendaciones ordenadas por prioridad.
 *
 * @param {Object} payload JSON con { diagnostico, alertas[], recomendaciones[] }.
 */
function pintarRecomendaciones(payload) {
    diagnosticoTexto.textContent = payload.diagnostico || 'La IA no ha devuelto diagnóstico.';

    const alertas = Array.isArray(payload.alertas) ? payload.alertas : [];
    listaAlertas.innerHTML = '';
    if (alertas.length) {
        alertasWrap.classList.remove('oculto');
        alertas.forEach(a => {
            const li = document.createElement('li');
            li.textContent = a;
            listaAlertas.appendChild(li);
        });
    } else {
        alertasWrap.classList.add('oculto');
    }

    const recs = Array.isArray(payload.recomendaciones) ? [...payload.recomendaciones] : [];
    recs.sort((a, b) => (a.prioridad || 99) - (b.prioridad || 99));

    listaRecs.innerHTML = '';
    if (!recs.length) {
        listaRecs.innerHTML = '<p class="ayuda">La IA no ha generado recomendaciones para tu situación actual.</p>';
        return;
    }

    recs.forEach(r => {
        const card = document.createElement('div');
        card.className = 'rec-card';
        const importe = (r.importe_mensual_sugerido_eur != null && !isNaN(Number(r.importe_mensual_sugerido_eur)))
            ? '<div class="rec-importe">Sugerencia: <strong>' + Number(r.importe_mensual_sugerido_eur).toLocaleString('es-ES', { minimumFractionDigits: 2, maximumFractionDigits: 2 }) + ' €/mes</strong></div>'
            : '';
        card.innerHTML = `
            <div class="rec-cabecera">
                <span class="rec-prioridad">${r.prioridad || '?'}</span>
                <h4 class="rec-titulo">${r.titulo || 'Sin título'}</h4>
            </div>
            <div class="rec-badges">
                ${objetivoBadge(r.objetivo_relacionado)}
                ${plazoBadge(r.plazo)}
            </div>
            <p class="rec-porque"><strong>Por qué:</strong> ${r.porque || '-'}</p>
            <p class="rec-paso"><strong>Primer paso:</strong> ${r.primer_paso || '-'}</p>
            <p class="rec-producto"><strong>Producto sugerido:</strong> ${r.producto_sugerido || '-'}</p>
            ${importe}
        `;
        listaRecs.appendChild(card);
    });
}

/**
 * Handler del boton "generar recomendaciones".
 * Muestra el spinner, llama al backend, pinta el resultado o un mensaje
 * de error si la llamada falla (por ejemplo si Ollama no esta corriendo).
 */
btn.onclick = async function() {
    btn.disabled = true;
    hint.textContent = '';
    resultado.classList.add('oculto');
    loading.classList.remove('oculto');

    try {
        const res = await fetchAutenticado('/api/recomendaciones/ia', { method: 'POST' });
        if (!res.ok) {
            const err = await res.json().catch(() => ({}));
            throw new Error(err.error || ('Error ' + res.status));
        }
        const payload = await res.json();
        pintarRecomendaciones(payload);
        resultado.classList.remove('oculto');
    } catch (e) {
        hint.innerHTML = '<span class="recs-error">No se pudo generar: ' + (e.message || 'error desconocido') +
            '. Asegúrate de que el contenedor de Ollama está corriendo y que el modelo está descargado.</span>';
    } finally {
        loading.classList.add('oculto');
        btn.disabled = false;
    }
};
