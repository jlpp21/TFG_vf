// ======================
// Logica compartida por las paginas autenticadas
// ======================
// Requiere que auth.js se cargue antes (define obtenerSesion, fetchAutenticado, cerrarSesion).
// Este archivo se incluye en todas las paginas autenticadas (transacciones, bancos,
// objetivos, recomendaciones) y se encarga de pintar la cabecera del usuario en
// el sidebar, marcar la pagina activa, gestionar el modal de logout y exponer
// helpers globales (notificaciones, formato de euros, etc.).

/**
 * IIFE que pinta el nombre y la inicial del usuario en el sidebar.
 * Lee la sesion de localStorage y rellena los elementos #usuario-actual y #usuario-avatar.
 */
(function pintarUsuario() {
    const sesion = obtenerSesion();
    if (!sesion) return;
    const span = document.getElementById('usuario-actual');
    if (span) span.textContent = sesion.nombre;
    const avatar = document.getElementById('usuario-avatar');
    if (avatar) avatar.textContent = sesion.nombre.charAt(0).toUpperCase();
})();

/**
 * IIFE que marca el enlace de la navegacion segun la pagina actual.
 * Compara la URL con un mapa de paginas y anyade la clase "active" al enlace correspondiente.
 */
(function activarNav() {
    const path = (window.location.pathname.split('/').pop() || 'transacciones.html').toLowerCase();
    document.querySelectorAll('.nav-links a').forEach(a => a.classList.remove('active'));
    const mapa = {
        'transacciones.html':  'nav-transacciones',
        'bancos.html':         'nav-bancos',
        'objetivos.html':      'nav-objetivos',
        'recomendaciones.html':'nav-recomendaciones'
    };
    const targetId = mapa[path];
    if (targetId) {
        const el = document.getElementById(targetId);
        if (el) el.classList.add('active');
    }
})();

/**
 * IIFE que instala los event listeners del modal de cerrar sesion.
 * Conecta el boton de logout, el de cancelar, el de confirmar y el click en el fondo.
 */
(function instalarLogout() {
    const btnLogout = document.getElementById('btn-logout');
    const modalLogout = document.getElementById('modal-logout');
    const btnCancel = document.getElementById('btn-cancelar-logout');
    const btnConfirm = document.getElementById('btn-confirmar-logout');

    /** Abre el modal de confirmacion de logout. */
    function abrir() { if (modalLogout) modalLogout.classList.remove('oculto'); }

    /** Cierra el modal sin cerrar la sesion. */
    function cerrar() { if (modalLogout) modalLogout.classList.add('oculto'); }

    if (btnLogout) btnLogout.addEventListener('click', e => { e.preventDefault(); abrir(); });
    if (btnCancel) btnCancel.addEventListener('click', cerrar);
    if (btnConfirm) btnConfirm.addEventListener('click', cerrarSesion);
    if (modalLogout) {
        modalLogout.addEventListener('click', e => { if (e.target === modalLogout) cerrar(); });
    }
})();

// ======================
// Helpers globales
// ======================

/**
 * Muestra una notificacion temporal (toast) en la esquina de la pantalla.
 * Se oculta automaticamente despues de 2.5 segundos.
 *
 * @param {string}  msg              texto de la notificacion.
 * @param {boolean} [success=true]   true para estilo verde de exito, false para rojo de error.
 */
function showNotification(msg, success) {
    if (success === undefined) success = true;
    const noti = document.getElementById('notificacion');
    if (!noti) return;
    noti.textContent = msg;
    noti.className = success ? 'noti-success' : 'noti-error';
    noti.classList.remove('oculto');
    setTimeout(() => noti.classList.add('oculto'), 2500);
}

/**
 * Formatea un numero como cantidad en euros con signo (+ para positivos).
 *
 * @param {number|string} n numero a formatear (acepta strings parseables).
 * @returns {string} cadena tipo "+1.234,56 €" o "-42,00 €".
 */
function formatearEuros(n) {
    const num = Number(n) || 0;
    const signo = num >= 0 ? '+' : '';
    return signo + num.toLocaleString('es-ES', { minimumFractionDigits: 2, maximumFractionDigits: 2 }) + ' €';
}

/**
 * Convierte el codigo del tipo de cuenta a un texto legible en espanol.
 *
 * @param {string} tipo codigo de cuenta (CORRIENTE, AHORRO, CREDITO, PRESTAMO).
 * @returns {string} etiqueta legible (ej: "Cuenta de ahorro").
 */
function formatearTipoCuenta(tipo) {
    const t = (tipo || 'CORRIENTE').toUpperCase();
    if (t === 'AHORRO') return 'Cuenta de ahorro';
    if (t === 'CREDITO') return 'Tarjeta de credito';
    if (t === 'PRESTAMO') return 'Prestamo personal';
    return 'Cuenta corriente';
}
