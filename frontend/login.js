/**
 * Pagina de login (login.html).
 *
 * Comprueba si ya hay sesion (en cuyo caso redirige al dashboard) y
 * gestiona el envio del formulario llamando a iniciarSesion(). Si la
 * autenticacion va bien, redirige a transacciones.html.
 */

// Si ya hay sesion, vamos directo al dashboard
if (obtenerSesion()) {
    window.location.href = 'transacciones.html';
}

const formLogin = document.getElementById('form-login');

/**
 * Handler del submit del formulario de login.
 * Lee el correo y la contrasena del DOM, deshabilita el boton mientras
 * dura la peticion y, si va bien, redirige a transacciones.html.
 *
 * @param {Event} e evento de submit.
 */
formLogin.onsubmit = async function(e) {
    e.preventDefault();

    const correo = document.getElementById('login-correo').value.trim();
    const password = document.getElementById('login-password').value;

    const btn = formLogin.querySelector('button[type="submit"]');
    btn.disabled = true;
    btn.textContent = 'Entrando...';

    const resultado = await iniciarSesion(correo, password);

    btn.disabled = false;
    btn.textContent = 'Entrar';

    if (!resultado.ok) {
        mostrarMensajeAuth(resultado.mensaje, true);
        return;
    }

    mostrarMensajeAuth('Bienvenido ' + resultado.sesion.nombre + '!', false);
    setTimeout(() => {
        window.location.href = 'transacciones.html';
    }, 600);
};
