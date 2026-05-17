// ======================
// Pagina: Mis bancos
// ======================
// Pantalla que muestra el catalogo de bancos disponibles y la lista de
// bancos ya conectados por el usuario. Cuando el usuario clica en un
// banco del catalogo arranca un wizard de 4 pasos que simula el flujo
// PSD2 (credenciales -> SCA -> seleccion de cuenta -> sincronizacion).
// Al final llama al backend para crear la conexion. Tambien permite
// desconectar bancos ya guardados.

/**
 * Catalogo de bancos servido por el backend (GET /api/catalogo/bancos).
 * Se carga al inicio y se mapea a la estructura interna usada por el wizard.
 */
let BANCOS_DISPONIBLES = [];

/**
 * Pide al backend el catalogo de bancos disponibles y lo guarda en memoria.
 * Si la peticion falla, deja la lista vacia y muestra una notificacion de error.
 *
 * @returns {Promise<void>}
 */
async function cargarCatalogoBancos() {
    try {
        const res = await fetchAutenticado('/api/catalogo/bancos');
        if (!res.ok) throw new Error();
        const lista = await res.json();
        BANCOS_DISPONIBLES = lista.map(b => ({
            id: b.codigo,
            nombre: b.nombre,
            dominio: b.dominio,
            color: b.color,
            logoUrl: b.logoUrl
        }));
    } catch (e) {
        BANCOS_DISPONIBLES = [];
        showNotification('Error cargando el catalogo de bancos', false);
    }
}

/**
 * Devuelve una lista de URLs candidatas para el logo del banco, en orden de preferencia.
 * Si la primera falla al cargar, handleLogoError prueba la siguiente.
 *
 * @param {Object|string} banco objeto banco o solo el dominio.
 * @returns {string[]} lista de URLs ordenadas por preferencia.
 */
function fuentesLogo(banco) {
    const b = (typeof banco === 'string') ? { dominio: banco } : banco;
    const fuentes = [];
    if (b.logoUrl) fuentes.push(b.logoUrl);
    fuentes.push('https://icons.duckduckgo.com/ip3/' + b.dominio + '.ico');
    fuentes.push('https://www.google.com/s2/favicons?domain=' + b.dominio + '&sz=128');
    return fuentes;
}

/**
 * Handler global de error de carga de imagen de logo.
 * Va probando las fuentes alternativas guardadas en data-fuentes y, si todas
 * fallan, muestra un fallback con la inicial del banco. Se asigna a window
 * porque se invoca desde el atributo onerror del HTML generado dinamicamente.
 *
 * @param {HTMLImageElement} img elemento img que ha fallado.
 */
window.handleLogoError = function(img) {
    const fuentes = JSON.parse(img.dataset.fuentes || '[]');
    const idx = parseInt(img.dataset.idx || '0', 10);
    if (idx + 1 < fuentes.length) {
        img.dataset.idx = idx + 1;
        img.src = fuentes[idx + 1];
    } else {
        const wrap = img.parentElement;
        img.style.display = 'none';
        wrap.classList.add('logo-fallback');
        wrap.textContent = img.dataset.inicial;
    }
};

/** Lista de bancos que el usuario ya tiene conectados. */
let bancosConectados = [];

/**
 * Carga la lista de bancos conectados del usuario desde el backend.
 *
 * @returns {Promise<void>}
 */
async function cargarBancosConectados() {
    try {
        const res = await fetchAutenticado('/api/bancos');
        if (!res.ok) throw new Error();
        bancosConectados = await res.json();
    } catch (e) {
        bancosConectados = [];
        showNotification('Error cargando bancos', false);
    }
}

// ====== Render ======
const gridBancos = document.getElementById('grid-bancos');
const listaBancosConectados = document.getElementById('lista-bancos-conectados');
const sinBancosMsg = document.getElementById('sin-bancos-msg');

/**
 * Pinta la cuadricula de bancos disponibles. Cada tarjeta es clicable
 * (abre el wizard) salvo que el banco tenga ya conectados los 4 tipos
 * de cuenta (CORRIENTE, AHORRO, CREDITO, PRESTAMO).
 */
function renderBancosDisponibles() {
    gridBancos.innerHTML = '';
    const TIPOS_TOTALES = 4;

    BANCOS_DISPONIBLES.forEach(banco => {
        const tiposConectados = bancosConectados
            .filter(b => b.bancoCodigo === banco.id)
            .map(b => b.tipoCuenta);
        const todoConectado = tiposConectados.length >= TIPOS_TOTALES;
        const fuentes = fuentesLogo(banco);
        const card = document.createElement('div');
        card.className = 'banco-card' + (todoConectado ? ' banco-card-deshabilitado' : '');
        card.innerHTML = `
            <div class="banco-logo-wrap" style="--banco-color: ${banco.color}">
                <img src="${fuentes[0]}" alt="${banco.nombre}" class="banco-logo"
                     data-fuentes='${JSON.stringify(fuentes)}' data-idx="0"
                     data-inicial="${banco.nombre.charAt(0)}" onerror="handleLogoError(this)">
            </div>
            <p class="banco-nombre">${banco.nombre}</p>
            ${tiposConectados.length > 0 ? '<span class="badge-conectado">' + tiposConectados.length + '/' + TIPOS_TOTALES + ' conectado' + (tiposConectados.length > 1 ? 's' : '') + '</span>' : ''}
        `;
        if (!todoConectado) {
            card.addEventListener('click', () => pedirConectarBanco(banco, tiposConectados));
        }
        gridBancos.appendChild(card);
    });
}

/**
 * Pinta la lista de bancos ya conectados con su nombre, tipo, fecha y boton de desconectar.
 * Si la lista esta vacia, muestra un mensaje de "no hay bancos".
 */
function renderBancosConectados() {
    listaBancosConectados.innerHTML = '';
    if (bancosConectados.length === 0) {
        sinBancosMsg.classList.remove('oculto');
        return;
    }
    sinBancosMsg.classList.add('oculto');

    bancosConectados.forEach(banco => {
        const fuentes = fuentesLogo(banco);
        const item = document.createElement('div');
        item.className = 'banco-conectado';
        item.innerHTML = `
            <div class="banco-conectado-info">
                <div class="banco-mini-logo" style="--banco-color: ${banco.color}">
                    <img src="${fuentes[0]}" alt="${banco.nombre}"
                         data-fuentes='${JSON.stringify(fuentes)}' data-idx="0"
                         data-inicial="${banco.nombre.charAt(0)}" onerror="handleLogoError(this)">
                </div>
                <div>
                    <p class="banco-conectado-nombre">${banco.nombre} <span class="tipo-cuenta-tag">${formatearTipoCuenta(banco.tipoCuenta)}</span></p>
                    <p class="banco-conectado-fecha">•••• ${banco.numeroFinal || '----'} · Conectado el ${new Date(banco.fechaConexion).toLocaleDateString('es-ES')}</p>
                </div>
            </div>
            <button class="btn-desconectar" data-id="${banco.id}">Desconectar</button>
        `;
        listaBancosConectados.appendChild(item);
    });

    document.querySelectorAll('.btn-desconectar').forEach(btn => {
        btn.addEventListener('click', () => pedirDesconectarBanco(parseInt(btn.dataset.id, 10)));
    });
}

// ====== Wizard ======
const modalWizard = document.getElementById('modal-wizard');
const wizardBancoNombre = document.getElementById('wizard-banco-nombre');
const wizardBancoLogo = document.getElementById('wizard-banco-logo');
const btnCerrarWizard = document.getElementById('btn-cerrar-wizard');
const btnAtras = document.getElementById('wizard-atras');
const btnSiguiente = document.getElementById('wizard-siguiente');
const wizardCuentas = document.getElementById('wizard-cuentas');

/** Banco que se esta conectando ahora mismo en el wizard. */
let bancoSeleccionado = null;
/** Paso actual del wizard (1..4). */
let pasoActual = 1;
const TOTAL_PASOS = 4;

/**
 * Inicia el wizard de conexion de un banco: pinta el logo y nombre, carga las
 * cuentas mock de previsualizacion, va al paso 1 y muestra el modal.
 *
 * @param {Object}   banco              banco del catalogo a conectar.
 * @param {string[]} tiposYaConectados  tipos de cuenta que el usuario ya tiene de ese banco.
 * @returns {Promise<void>}
 */
async function pedirConectarBanco(banco, tiposYaConectados) {
    bancoSeleccionado = banco;
    pasoActual = 1;
    wizardBancoNombre.textContent = banco.nombre;
    pintarLogoBanco(banco);
    await pintarCuentasMock(banco, tiposYaConectados || []);
    irAPaso(1);
    document.getElementById('wizard-usuario').value = '';
    document.getElementById('wizard-password').value = '';
    document.getElementById('wizard-codigo').value = '';
    modalWizard.classList.remove('oculto');
}

/**
 * Pinta el logo grande del banco en la cabecera del wizard.
 *
 * @param {Object} banco objeto banco con dominio, color, logoUrl, nombre.
 */
function pintarLogoBanco(banco) {
    const fuentes = fuentesLogo(banco);
    wizardBancoLogo.style.setProperty('--banco-color', banco.color);
    wizardBancoLogo.classList.remove('logo-fallback');
    wizardBancoLogo.innerHTML =
        '<img src="' + fuentes[0] + '" alt="' + banco.nombre + '" ' +
        'data-fuentes=\'' + JSON.stringify(fuentes) + '\' data-idx="0" ' +
        'data-inicial="' + banco.nombre.charAt(0) + '" onerror="handleLogoError(this)">';
}

/**
 * Pide al backend (GET /api/sandbox/cuentas/:bancoCodigo) las cuentas mock
 * para previsualizar y las pinta como radios. Marca por defecto la primera
 * cuenta no conectada y desactiva las que ya estan conectadas.
 *
 * @param {Object}   banco              banco del que se piden las cuentas.
 * @param {string[]} tiposYaConectados  tipos ya conectados (no se podran reconectar).
 * @returns {Promise<void>}
 */
async function pintarCuentasMock(banco, tiposYaConectados) {
    let tipos = [];
    try {
        const res = await fetchAutenticado('/api/sandbox/cuentas/' + banco.id);
        if (!res.ok) throw new Error();
        const data = await res.json();
        tipos = data.map(c => ({
            value: c.tipoCuenta,
            tipo: c.tipoLabel,
            saldo: Number(c.saldo),
            num: c.numero,
            desc: c.descripcion
        }));
    } catch (e) {
        wizardCuentas.innerHTML = '<p class="ayuda">No se pudieron cargar las cuentas disponibles.</p>';
        return;
    }

    wizardCuentas.innerHTML = '';
    let primerDisponible = -1;
    tipos.forEach((c, i) => {
        if (!tiposYaConectados.includes(c.value) && primerDisponible === -1) primerDisponible = i;
    });

    tipos.forEach((c, i) => {
        const yaConectado = tiposYaConectados.includes(c.value);
        const id = 'cuenta-' + i;
        const item = document.createElement('label');
        item.className = 'wizard-cuenta' + (yaConectado ? ' wizard-cuenta-deshabilitada' : '');
        item.innerHTML = `
            <input type="radio" name="wizard-tipo-cuenta" value="${c.value}" id="${id}"
                   ${i === primerDisponible ? 'checked' : ''} ${yaConectado ? 'disabled' : ''}>
            <div class="wizard-cuenta-info">
                <span class="wizard-cuenta-tipo">${c.tipo} ${yaConectado ? '<span class="badge-conectado">Ya conectado</span>' : ''}</span>
                <span class="wizard-cuenta-num">${c.num}</span>
                <span class="wizard-cuenta-desc">${c.desc}</span>
            </div>
            <span class="wizard-cuenta-saldo ${c.saldo >= 0 ? 'monto-positivo' : 'monto-negativo'}">
                ${c.saldo.toFixed(2)} €
            </span>
        `;
        wizardCuentas.appendChild(item);
    });
}

/**
 * Cambia el paso visible del wizard y actualiza la barra de progreso y los botones.
 * Acepta un valor especial 'sync' que muestra la animacion de sincronizacion final.
 *
 * @param {number|'sync'} n numero de paso (1..4) o 'sync' para la pantalla final.
 */
function irAPaso(n) {
    pasoActual = n;
    document.querySelectorAll('.wizard-paso').forEach(p => p.classList.add('oculto'));

    if (n === 'sync') {
        document.getElementById('wizard-paso-sync').classList.remove('oculto');
        btnAtras.classList.add('oculto');
        btnSiguiente.classList.add('oculto');
        return;
    }
    document.getElementById('wizard-paso-' + n).classList.remove('oculto');

    document.querySelectorAll('.step').forEach(s => {
        const num = parseInt(s.dataset.step, 10);
        s.classList.remove('step-activo', 'step-completado');
        if (num < n) s.classList.add('step-completado');
        else if (num === n) s.classList.add('step-activo');
    });

    btnAtras.classList.remove('oculto');
    btnSiguiente.classList.remove('oculto');
    btnAtras.disabled = (n === 1);
    btnSiguiente.textContent = (n === TOTAL_PASOS) ? 'Sincronizar' : 'Continuar';
}

/**
 * Valida el paso actual antes de avanzar al siguiente.
 * Para los pasos 2 y 3 hace una llamada al sandbox para validar credenciales y SCA.
 *
 * @param {number} n numero del paso a validar.
 * @returns {Promise<boolean>} true si el paso es valido y se puede avanzar.
 */
async function validarPaso(n) {
    if (n === 2) {
        const usuario = document.getElementById('wizard-usuario').value.trim();
        const password = document.getElementById('wizard-password').value;
        const res = await fetchAutenticado('/api/sandbox/credenciales', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ usuario, password })
        });
        if (!res.ok) {
            const err = await res.json().catch(() => ({}));
            showNotification(err.error || 'Credenciales invalidas', false);
            return false;
        }
    }
    if (n === 3) {
        const codigo = document.getElementById('wizard-codigo').value.trim();
        const res = await fetchAutenticado('/api/sandbox/sca', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ codigo })
        });
        if (!res.ok) {
            const err = await res.json().catch(() => ({}));
            showNotification(err.error || 'Codigo incorrecto', false);
            return false;
        }
    }
    if (n === 4) {
        if (!wizardCuentas.querySelector('input[type="radio"]:checked')) {
            showNotification('Selecciona el tipo de cuenta', false);
            return false;
        }
    }
    return true;
}

/**
 * Devuelve el tipo de cuenta seleccionado en el paso 4 del wizard.
 *
 * @returns {string} valor del radio marcado o "CORRIENTE" como fallback.
 */
function tipoCuentaSeleccionado() {
    const radio = wizardCuentas.querySelector('input[type="radio"]:checked');
    return radio ? radio.value : 'CORRIENTE';
}

/**
 * Cierra el modal del wizard y limpia el banco seleccionado.
 */
function cerrarWizard() {
    bancoSeleccionado = null;
    modalWizard.classList.add('oculto');
}

btnCerrarWizard.addEventListener('click', cerrarWizard);
modalWizard.addEventListener('click', e => { if (e.target === modalWizard) cerrarWizard(); });
btnAtras.addEventListener('click', () => { if (pasoActual > 1) irAPaso(pasoActual - 1); });

/**
 * Handler del boton "Continuar / Sincronizar".
 * Valida el paso actual y, si es correcto, avanza. En el ultimo paso muestra
 * la pantalla de sincronizacion y llama al backend para crear la conexion.
 */
btnSiguiente.addEventListener('click', async function() {
    btnSiguiente.disabled = true;
    try {
        const ok = await validarPaso(pasoActual);
        if (!ok) return;
        if (pasoActual < TOTAL_PASOS) { irAPaso(pasoActual + 1); return; }
        irAPaso('sync');
        await conectarBanco(bancoSeleccionado);
        cerrarWizard();
    } finally {
        btnSiguiente.disabled = false;
    }
});

// ====== Conectar / desconectar contra backend ======

/**
 * Llama a POST /api/bancos para crear la conexion del banco con el tipo de cuenta seleccionado.
 *
 * @param {Object} banco banco del catalogo a conectar.
 * @returns {Promise<void>}
 */
async function conectarBanco(banco) {
    const tipoCuenta = tipoCuentaSeleccionado();

    try {
        const res = await fetchAutenticado('/api/bancos', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                bancoCodigo: banco.id,
                nombre: banco.nombre,
                dominio: banco.dominio,
                color: banco.color,
                logoUrl: banco.logoUrl || null,
                tipoCuenta: tipoCuenta
            })
        });
        if (!res.ok) {
            const err = await res.json().catch(() => ({}));
            throw new Error(err.error || 'No se pudo conectar el banco');
        }
        showNotification('Cuenta de ' + banco.nombre + ' conectada correctamente');
        await refrescar();
    } catch (e) {
        showNotification(e.message || 'Error al conectar el banco', false);
    }
}

/**
 * Llama a DELETE /api/bancos/:id para desconectar un banco del usuario.
 *
 * @param {number} idDb id de la conexion en base de datos.
 * @returns {Promise<void>}
 */
async function desconectarBanco(idDb) {
    try {
        const res = await fetchAutenticado('/api/bancos/' + idDb, { method: 'DELETE' });
        if (!res.ok && res.status !== 204) throw new Error('No se pudo desconectar el banco');
        showNotification('Cuenta desconectada');
        await refrescar();
    } catch (e) {
        showNotification(e.message, false);
    }
}

// ====== Modal desconectar ======
const modalDesconectar = document.getElementById('modal-desconectar');
const modalDesconectarTexto = document.getElementById('modal-desconectar-texto');
const btnCancelarDesconectar = document.getElementById('btn-cancelar-desconectar');
const btnConfirmarDesconectar = document.getElementById('btn-confirmar-desconectar');
let bancoIdADesconectar = null;

/**
 * Abre el modal de confirmacion para desconectar un banco concreto.
 * Guarda el id en una variable de modulo para usarlo cuando se confirme.
 *
 * @param {number} idDb id de la conexion bancaria a desconectar.
 */
function pedirDesconectarBanco(idDb) {
    const banco = bancosConectados.find(b => b.id === idDb);
    if (!banco) return;
    bancoIdADesconectar = idDb;
    modalDesconectarTexto.textContent =
        '¿Seguro que deseas desconectar tu cuenta de ' + banco.nombre + '? Se eliminarán todos sus movimientos.';
    modalDesconectar.classList.remove('oculto');
}

/**
 * Cierra el modal de desconectar sin hacer nada.
 */
function cerrarModalDesconectar() {
    bancoIdADesconectar = null;
    modalDesconectar.classList.add('oculto');
}

btnCancelarDesconectar.addEventListener('click', cerrarModalDesconectar);
btnConfirmarDesconectar.addEventListener('click', async function() {
    if (bancoIdADesconectar) await desconectarBanco(bancoIdADesconectar);
    cerrarModalDesconectar();
});
modalDesconectar.addEventListener('click', e => { if (e.target === modalDesconectar) cerrarModalDesconectar(); });

/**
 * Refresca la lista de bancos conectados y vuelve a pintar ambas secciones
 * (catalogo de disponibles y lista de conectados).
 *
 * @returns {Promise<void>}
 */
async function refrescar() {
    await cargarBancosConectados();
    renderBancosConectados();
    renderBancosDisponibles();
}

/**
 * Inicializacion al cargar la pagina: descarga el catalogo y la lista de conectados.
 */
(async function init() {
    await cargarCatalogoBancos();
    await refrescar();
})();
