// ======================
// Pagina: Transacciones
// ======================
// Pantalla principal del dashboard. Muestra la lista paginada de movimientos
// con filtros por descripcion, fecha y banco; ademas pinta tarjetas de resumen
// con el balance total, el numero de cuentas conectadas y un desglose por
// tipo de cuenta (corriente, ahorro, credito, prestamo).

let paginaActual = 0;
let totalPaginas = 1;
const TAMANIO_PAGINA = 25;
let bancosConectados = [];

const tablaBody = document.querySelector('#tabla-transacciones tbody');
const sinMovimientosMsg = document.getElementById('sin-movimientos-msg');
const inputDescripcion = document.getElementById('buscar-descripcion');
const inputFecha = document.getElementById('buscar-fecha');
const filtroBanco = document.getElementById('filtro-banco');
const paginacionWrap = document.getElementById('paginacion');
const btnPrev = document.getElementById('btn-pag-prev');
const btnNext = document.getElementById('btn-pag-next');
const pagInfo = document.getElementById('pag-info');
const resumenBalance = document.getElementById('resumen-balance');
const resumenTotal = document.getElementById('resumen-total');
const resumenBancosCount = document.getElementById('resumen-bancos-count');
const desgloseGrid = document.getElementById('desglose-grid');

/** Mapa de codigos de tipo de cuenta a etiquetas legibles para las tarjetas de desglose. */
const DESGLOSE_LABELS = {
    CORRIENTE: 'Cuentas corrientes',
    AHORRO:    'Cuentas de ahorro',
    CREDITO:   'Tarjetas de credito',
    PRESTAMO:  'Prestamos'
};

/** Orden en el que se pintan las tarjetas de desglose. */
const DESGLOSE_ORDEN = ['CORRIENTE', 'AHORRO', 'CREDITO', 'PRESTAMO'];

/**
 * Pinta las tarjetas de desglose por tipo de cuenta a partir del mapa devuelto por el backend.
 *
 * @param {Object<string, number>} desglose mapa { CORRIENTE: 1234.5, AHORRO: 8000, ... }.
 */
function pintarDesglose(desglose) {
    desgloseGrid.innerHTML = '';
    if (!desglose) return;
    DESGLOSE_ORDEN.forEach(tipo => {
        if (!(tipo in desglose)) return;
        const valor = Number(desglose[tipo]) || 0;
        const card = document.createElement('div');
        card.className = 'desglose-card desglose-' + tipo.toLowerCase();
        card.innerHTML = `
            <span class="desglose-label">${DESGLOSE_LABELS[tipo] || tipo}</span>
            <span class="desglose-valor ${valor >= 0 ? 'monto-positivo' : 'monto-negativo'}">
                ${formatearEuros(valor).replace('+', '')}
            </span>
        `;
        desgloseGrid.appendChild(card);
    });
}

/**
 * Carga la lista de bancos conectados del usuario y refresca el desplegable
 * de filtro por banco.
 *
 * @returns {Promise<void>}
 */
async function cargarBancos() {
    try {
        const res = await fetchAutenticado('/api/bancos');
        if (!res.ok) throw new Error();
        bancosConectados = await res.json();
    } catch (e) {
        bancosConectados = [];
    }
    pintarFiltroBancos();
}

/**
 * Pinta las opciones del select de filtro por banco a partir de la lista cargada.
 * Conserva la opcion seleccionada anteriormente si sigue existiendo.
 */
function pintarFiltroBancos() {
    const seleccionado = filtroBanco.value;
    filtroBanco.innerHTML = '<option value="">Todos los bancos</option>';
    bancosConectados.forEach(b => {
        const opt = document.createElement('option');
        opt.value = b.id;
        opt.textContent = b.nombre + ' · ' + (b.tipoCuentaLabel || formatearTipoCuenta(b.tipoCuenta));
        filtroBanco.appendChild(opt);
    });
    filtroBanco.value = seleccionado;
}

/**
 * Pide al backend la pagina de movimientos segun los filtros activos y
 * pinta la tabla, el resumen y la paginacion.
 *
 * @returns {Promise<void>}
 */
async function renderTransacciones() {
    const params = new URLSearchParams();
    params.append('page', paginaActual);
    params.append('size', TAMANIO_PAGINA);
    const desc = inputDescripcion.value.trim();
    if (desc) params.append('descripcion', desc);
    if (inputFecha.value) params.append('fecha', inputFecha.value);
    if (filtroBanco.value) params.append('bancoId', filtroBanco.value);

    let data;
    try {
        const res = await fetchAutenticado('/api/movimientos?' + params.toString());
        if (!res.ok) throw new Error('No se pudieron cargar los movimientos');
        data = await res.json();
    } catch (e) {
        showNotification(e.message, false);
        return;
    }

    resumenBalance.textContent = formatearEuros(data.balanceTotal).replace('+', '');
    resumenBalance.className = 'resumen-valor ' + (Number(data.balanceTotal) >= 0 ? 'monto-positivo' : 'monto-negativo');
    resumenTotal.textContent = data.total.toLocaleString('es-ES');
    pintarDesglose(data.desgloseTipoCuenta);
    resumenBancosCount.textContent =
        bancosConectados.length + (bancosConectados.length === 1 ? ' cuenta conectada' : ' cuentas conectadas');

    totalPaginas = Math.max(data.totalPaginas, 1);
    if (paginaActual >= totalPaginas) paginaActual = totalPaginas - 1;
    pagInfo.textContent = 'Página ' + (paginaActual + 1) + ' de ' + totalPaginas;
    btnPrev.disabled = paginaActual <= 0;
    btnNext.disabled = paginaActual >= totalPaginas - 1;

    tablaBody.innerHTML = '';
    if (data.items.length === 0) {
        sinMovimientosMsg.classList.remove('oculto');
        paginacionWrap.classList.add('oculto');
        return;
    }
    sinMovimientosMsg.classList.add('oculto');
    paginacionWrap.classList.remove('oculto');

    data.items.forEach(t => {
        const tr = document.createElement('tr');
        const claseMonto = t.monto >= 0 ? 'monto-positivo' : 'monto-negativo';
        const claseBalance = t.balanceDespues >= 0 ? 'monto-positivo' : 'monto-negativo';
        const tipo = t.bancoTipoCuenta || 'CORRIENTE';
        tr.innerHTML = `
            <td>${new Date(t.fecha).toLocaleDateString('es-ES')}</td>
            <td>
                <span class="banco-pill" style="--banco-color: ${t.bancoColor || '#999'}">
                    ${t.bancoNombre || t.bancoCodigo}
                </span>
            </td>
            <td>
                <div class="cuenta-celda cuenta-${tipo.toLowerCase()}">
                    <span class="cuenta-numero">•••• ${t.bancoNumeroFinal || '----'}</span>
                    <span class="cuenta-label">${formatearTipoCuenta(tipo)}</span>
                </div>
            </td>
            <td>${t.descripcion}</td>
            <td><span class="categoria-tag">${t.categoria}</span></td>
            <td class="celda-monto ${claseMonto}">${formatearEuros(t.monto)}</td>
            <td class="celda-monto ${claseBalance}">${formatearEuros(t.balanceDespues).replace('+', '')}</td>
        `;
        tablaBody.appendChild(tr);
    });
}

/**
 * Resetea la pagina a 0 y vuelve a cargar la lista. Se llama cuando cambia algun filtro.
 */
function alCambiarFiltro() {
    paginaActual = 0;
    renderTransacciones();
}
inputDescripcion.oninput = alCambiarFiltro;
inputFecha.onchange = alCambiarFiltro;
filtroBanco.onchange = alCambiarFiltro;

/**
 * Handler del boton "limpiar filtros": vacia los inputs y vuelve a cargar.
 */
document.getElementById('limpiar-filtros').onclick = function() {
    inputDescripcion.value = '';
    inputFecha.value = '';
    filtroBanco.value = '';
    alCambiarFiltro();
};

/** Handler del boton de pagina anterior. */
btnPrev.onclick = function() {
    if (paginaActual > 0) { paginaActual--; renderTransacciones(); }
};

/** Handler del boton de pagina siguiente. */
btnNext.onclick = function() {
    if (paginaActual < totalPaginas - 1) { paginaActual++; renderTransacciones(); }
};

// Init
(async function() {
    await cargarBancos();
    await renderTransacciones();
})();
