// ======================
// Pagina: Objetivos
// ======================
// Pinta el formulario de seleccion de objetivos financieros del usuario.
// Cada fila es un objetivo (comprar piso, plan de jubilacion, etc.) con
// 4 radios para el plazo: NINGUNO / CORTO / MEDIO / LARGO. Al guardar,
// envia al backend la lista de objetivos seleccionados (los que no son
// NINGUNO) via PUT /api/objetivos.

/**
 * Catalogo estatico de objetivos disponibles.
 * Cada elemento tiene un codigo (lo que va al backend) y una etiqueta legible.
 */
const OBJETIVOS_CATALOGO = [
    { codigo: 'comprar_piso',           etiqueta: 'Comprar un piso o casa' },
    { codigo: 'inversion_inmobiliaria', etiqueta: 'Comprar locales o inmuebles para alquilar' },
    { codigo: 'inversion_cripto',       etiqueta: 'Invertir en criptomonedas' },
    { codigo: 'inversion_empresas',     etiqueta: 'Invertir en empresas o startups' },
    { codigo: 'inversion_bolsa',        etiqueta: 'Invertir en bolsa o fondos indexados' },
    { codigo: 'plan_jubilacion',        etiqueta: 'Plan de jubilación' },
    { codigo: 'fondo_emergencia',       etiqueta: 'Crear un fondo de emergencia' },
    { codigo: 'ahorrar_capital',        etiqueta: 'Ahorrar y hacer crecer mi patrimonio' },
    { codigo: 'dedicarse_inversion',    etiqueta: 'Dedicarme a la inversión / vivir de las rentas' },
    { codigo: 'estudios',               etiqueta: 'Pagar estudios (propios o de los hijos)' }
];

const formObjetivos = document.getElementById('form-objetivos');
const filasObjetivos = document.getElementById('objetivos-filas');

/**
 * Pinta el formulario de objetivos marcando los plazos ya seleccionados por el usuario.
 *
 * @param {Array<{objetivo: string, plazo: string}>} seleccionados lista de objetivos guardados.
 */
function pintarObjetivos(seleccionados) {
    const mapa = {};
    (seleccionados || []).forEach(o => { mapa[o.objetivo] = o.plazo; });

    filasObjetivos.innerHTML = '';
    OBJETIVOS_CATALOGO.forEach(o => {
        const fila = document.createElement('div');
        fila.className = 'objetivos-fila';
        const valor = mapa[o.codigo] || 'NINGUNO';
        fila.innerHTML = `
            <span class="objetivos-col-meta">${o.etiqueta}</span>
            <label class="objetivos-radio"><input type="radio" name="${o.codigo}" value="NINGUNO" ${valor === 'NINGUNO' ? 'checked' : ''}></label>
            <label class="objetivos-radio"><input type="radio" name="${o.codigo}" value="CORTO"   ${valor === 'CORTO'   ? 'checked' : ''}></label>
            <label class="objetivos-radio"><input type="radio" name="${o.codigo}" value="MEDIO"   ${valor === 'MEDIO'   ? 'checked' : ''}></label>
            <label class="objetivos-radio"><input type="radio" name="${o.codigo}" value="LARGO"   ${valor === 'LARGO'   ? 'checked' : ''}></label>
        `;
        filasObjetivos.appendChild(fila);
    });
}

/**
 * Carga los objetivos del usuario desde el backend y pinta el formulario.
 * Si la peticion falla, pinta el formulario vacio.
 *
 * @returns {Promise<void>}
 */
async function cargarObjetivos() {
    try {
        const res = await fetchAutenticado('/api/objetivos');
        if (!res.ok) throw new Error();
        const datos = await res.json();
        pintarObjetivos(datos);
    } catch (e) {
        pintarObjetivos([]);
    }
}

/**
 * Handler del submit del formulario de objetivos.
 * Recoge los radios marcados (descartando los que estan en NINGUNO) y
 * envia la lista al backend via PUT.
 *
 * @param {Event} e evento de submit.
 */
formObjetivos.onsubmit = async function(e) {
    e.preventDefault();
    const entradas = [];
    OBJETIVOS_CATALOGO.forEach(o => {
        const sel = formObjetivos.querySelector(`input[name="${o.codigo}"]:checked`);
        if (sel && sel.value !== 'NINGUNO') {
            entradas.push({ objetivo: o.codigo, plazo: sel.value });
        }
    });
    const btn = formObjetivos.querySelector('button[type="submit"]');
    btn.disabled = true;
    try {
        const res = await fetchAutenticado('/api/objetivos', {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(entradas)
        });
        if (!res.ok) {
            const err = await res.json().catch(() => ({}));
            showNotification(err.error || 'No se pudieron guardar los objetivos', false);
            return;
        }
        showNotification('Objetivos guardados (' + entradas.length + ')');
    } finally {
        btn.disabled = false;
    }
};

cargarObjetivos();
