/**
 * Pagina de registro (register.html).
 *
 * Comprueba si ya hay sesion (en cuyo caso redirige al dashboard) y
 * gestiona el envio del formulario de alta. Valida que las contrasenas
 * coincidan y que se haya elegido un perfil financiero antes de llamar
 * a registrarUsuario().
 */

// Si ya hay sesion, vamos directo al dashboard
if (obtenerSesion()) {
    window.location.href = 'transacciones.html';
}

const formRegistro = document.getElementById('form-registro');

/**
 * Handler del submit del formulario de registro.
 * Valida los campos en el cliente antes de llamar al backend; si el
 * registro es correcto, redirige al dashboard.
 *
 * @param {Event} e evento de submit.
 */
formRegistro.onsubmit = async function(e) {
    e.preventDefault();

    const usuario = document.getElementById('reg-usuario').value.trim();
    const correo = document.getElementById('reg-correo').value.trim();
    const password = document.getElementById('reg-password').value;
    const password2 = document.getElementById('reg-password2').value;
    const perfil = document.getElementById('reg-perfil').value;

    if (password !== password2) {
        mostrarMensajeAuth('Las contrasenas no coinciden', true);
        return;
    }
    if (!perfil) {
        mostrarMensajeAuth('Selecciona un perfil financiero', true);
        return;
    }

    const btn = formRegistro.querySelector('button[type="submit"]');
    btn.disabled = true;
    btn.textContent = 'Creando cuenta...';

    const resultado = await registrarUsuario(usuario, correo, password, perfil);

    btn.disabled = false;
    btn.textContent = 'Registrarse';

    if (!resultado.ok) {
        mostrarMensajeAuth(resultado.mensaje, true);
        return;
    }

    mostrarMensajeAuth('Registro exitoso! Redirigiendo al dashboard...', false);
    formRegistro.reset();
    setTimeout(() => {
        window.location.href = 'transacciones.html';
    }, 800);
};
