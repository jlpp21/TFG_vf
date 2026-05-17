// ======================
// Modulo de autenticacion contra el backend
// ======================
// El backend expone:
//   POST /api/auth/register  -> crea usuario y devuelve { token, id, nombre, correo }
//   POST /api/auth/login     -> valida credenciales y devuelve lo mismo
//   GET  /api/auth/me        -> devuelve el usuario asociado al token
// El token JWT se guarda en localStorage y se envia en cada peticion protegida.

/** Clave de localStorage donde se guarda el token JWT. */
const TOKEN_KEY = 'fintech_token';

/** Clave de localStorage donde se guardan los datos basicos del usuario. */
const SESION_KEY = 'fintech_sesion';

/**
 * Lee el token JWT guardado en localStorage.
 * @returns {string|null} token JWT o null si no hay sesion.
 */
function obtenerToken() {
    return localStorage.getItem(TOKEN_KEY);
}

/**
 * Lee los datos de la sesion guardados en localStorage.
 * @returns {Object|null} objeto con id, nombre, correo y perfilFinanciero, o null si no hay sesion.
 */
function obtenerSesion() {
    const datos = localStorage.getItem(SESION_KEY);
    return datos ? JSON.parse(datos) : null;
}

/**
 * Guarda el token y los datos del usuario en localStorage.
 * @param {Object} authResponse respuesta del backend con { token, id, nombre, correo, perfilFinanciero }.
 */
function guardarSesion(authResponse) {
    localStorage.setItem(TOKEN_KEY, authResponse.token);
    localStorage.setItem(SESION_KEY, JSON.stringify({
        id: authResponse.id,
        nombre: authResponse.nombre,
        correo: authResponse.correo,
        perfilFinanciero: authResponse.perfilFinanciero
    }));
}

/**
 * Cierra la sesion: borra el token y los datos del usuario y redirige al login.
 */
function cerrarSesion() {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(SESION_KEY);
    window.location.href = 'login.html';
}

/**
 * Llama al backend para registrar al usuario.
 *
 * @param {string} nombre            nombre completo del usuario.
 * @param {string} correo            correo electronico.
 * @param {string} password          contrasena en claro.
 * @param {string} perfilFinanciero  perfil financiero declarado (ej: HOLGADO).
 * @returns {Promise<{ok: boolean, mensaje: string, sesion?: Object}>} resultado del registro.
 */
async function registrarUsuario(nombre, correo, password, perfilFinanciero) {
    try {
        const res = await fetch('/api/auth/register', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ nombre, correo, password, perfilFinanciero })
        });
        const data = await res.json();
        if (!res.ok) {
            return { ok: false, mensaje: data.error || 'No se pudo registrar el usuario' };
        }
        guardarSesion(data);
        return { ok: true, mensaje: 'Usuario registrado correctamente', sesion: obtenerSesion() };
    } catch (e) {
        return { ok: false, mensaje: 'Error de conexion con el servidor' };
    }
}

/**
 * Llama al backend para iniciar sesion.
 *
 * @param {string} correo   correo electronico del usuario.
 * @param {string} password contrasena en claro.
 * @returns {Promise<{ok: boolean, mensaje: string, sesion?: Object}>} resultado del login.
 */
async function iniciarSesion(correo, password) {
    try {
        const res = await fetch('/api/auth/login', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ correo, password })
        });
        const data = await res.json();
        if (!res.ok) {
            return { ok: false, mensaje: data.error || 'Correo o contrasena incorrectos' };
        }
        guardarSesion(data);
        return { ok: true, mensaje: 'Sesion iniciada', sesion: obtenerSesion() };
    } catch (e) {
        return { ok: false, mensaje: 'Error de conexion con el servidor' };
    }
}

/**
 * Middleware: si no hay token, redirige al login.
 * No valida el token contra el backend en este punto; solo comprueba que existe.
 *
 * @returns {Object|null} objeto de sesion almacenado, o null si redirigio al login.
 */
function requerirAutenticacion() {
    const token = obtenerToken();
    if (!token) {
        window.location.href = 'login.html';
        return null;
    }
    return obtenerSesion();
}

/**
 * Helper para llamadas autenticadas. Anade la cabecera Authorization con el JWT
 * y, si el backend responde 401 (token caducado o invalido), fuerza un logout.
 *
 * @param {string} url      URL del endpoint a llamar.
 * @param {Object} [options={}] opciones de fetch (method, body, headers, etc.).
 * @returns {Promise<Response>} respuesta del fetch.
 */
async function fetchAutenticado(url, options = {}) {
    const token = obtenerToken();
    const headers = Object.assign({}, options.headers || {}, {
        'Authorization': 'Bearer ' + token
    });
    const res = await fetch(url, Object.assign({}, options, { headers }));
    if (res.status === 401) {
        cerrarSesion();
    }
    return res;
}

/**
 * Muestra un mensaje (de error o de exito) en las paginas de login y registro.
 *
 * @param {string}  mensaje texto a mostrar.
 * @param {boolean} esError true si es un error (estilo rojo), false si es ok.
 */
function mostrarMensajeAuth(mensaje, esError) {
    const div = document.getElementById('auth-mensaje');
    if (!div) return;
    div.textContent = mensaje;
    div.className = esError ? 'auth-mensaje-error' : 'auth-mensaje-ok';
    div.classList.remove('oculto');
}
