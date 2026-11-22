/**
 * PROYECTO: Feeling
 * MÓDULO: Inicio de sesión (RF-02)
 * ARCHIVO: login.js
 * UBICACIÓN: src/main/resources/static/js/login.js
 *
 * AUTOR: Yulipaola Domínguez
 * VERSIÓN: 1.2.0
 * FECHA DE DOCUMENTACIÓN: 2026-11-22
 * ESTADO: Finalizado
 * TICKET: SCRUM-52
 *
 * DESCRIPCIÓN:
 * Comportamiento del formulario de inicio de sesión. Revisa que el
 * nombre de usuario y la contraseña no estén vacíos, muestra los
 * mensajes de error y evita que el formulario se envíe dos veces.
 * La autenticación la hace el servidor (LoginController).
 *
 * FUNCIONALIDADES PRINCIPALES:
 * - Validar que los dos campos tengan contenido.
 * - Mostrar el error debajo de cada campo y un mensaje general.
 * - Mostrar "Ingresando..." en el botón mientras se envía.
 * - Quitar el estado de carga si el navegador restaura la página
 *   al usar el botón "Atrás".
 *
 * VARIABLES PRINCIPALES:
 * - usuario, contrasena: campos del formulario (HTMLElement).
 * - errorUsuario, errorContrasena: textos de error (HTMLElement).
 * - enviando: indica si el formulario ya se está enviando (boolean).
 *
 * FUNCIONES:
 * - mostrarError / limpiarError: estado de error de un campo.
 * - validarUsuario / validarContrasena / validarFormulario.
 * - establecerCarga(cargando): estado de carga del botón.
 *
 * DEPENDENCIAS:
 * - login.html: identificadores loginForm, nombreUsuario, contrasena,
 *   errorNombreUsuario, errorContrasena, loginFormMessage, loginSubmit,
 *   loginSubmitText y loginLoading.
 * - login.css: clases is-invalid e is-visible.
 *
 * HISTORIAL DE VERSIONES:
 * - 1.0.0: validación de campos y control del envío.
 * - 1.2.0: reinicio del botón al volver con "Atrás", se quita el código
 *   de mostrar contraseña (la vista no tiene ese botón) y documentación.
 */

document.addEventListener("DOMContentLoaded", () => {

    "use strict";

    // 1. Referencias a los elementos del formulario

    const form = document.getElementById("loginForm");

    // Comprueba que la página contiene el formulario de inicio.
    if (!form) {
        return;
    }

    const usuario = document.getElementById("nombreUsuario");
    const contrasena = document.getElementById("contrasena");

    const errorUsuario = document.getElementById("errorNombreUsuario");
    const errorContrasena = document.getElementById("errorContrasena");

    const mensajeGeneral = document.getElementById("loginFormMessage");

    const botonEnviar = document.getElementById("loginSubmit");
    const textoBoton = document.getElementById("loginSubmitText");
    const indicadorCarga = document.getElementById("loginLoading");

    let enviando = false;

    // 2. Funciones para mostrar y limpiar errores

    /**
     * Muestra un mensaje de error en un campo.
     *
     * @param {HTMLElement} campo - Campo que presenta el error.
     * @param {HTMLElement} elementoError - Elemento donde se muestra el mensaje.
     * @param {string} mensaje - Descripción del error.
     */
    function mostrarError(campo, elementoError, mensaje) {

        if (campo) {
            campo.classList.add("is-invalid");
            campo.classList.remove("is-valid");
            campo.setAttribute("aria-invalid", "true");
        }

        if (elementoError) {
            elementoError.textContent = mensaje;
        }
    }

    /**
     * Limpia el error de un campo cuando su contenido es correcto.
     *
     * @param {HTMLElement} campo - Campo que se está validando.
     * @param {HTMLElement} elementoError - Elemento del mensaje de error.
     */
    function limpiarError(campo, elementoError) {

        if (campo) {
            campo.classList.remove("is-invalid");
            campo.removeAttribute("aria-invalid");
        }

        if (elementoError) {
            elementoError.textContent = "";
        }
    }

    /**
     * Limpia el mensaje general del formulario.
     */
    function limpiarMensajeGeneral() {

        if (mensajeGeneral) {
            mensajeGeneral.textContent = "";
            mensajeGeneral.classList.remove("is-visible");
        }
    }

    /**
     * Muestra un mensaje general de validación.
     *
     * @param {string} mensaje - Mensaje que se mostrará.
     */
    function mostrarMensajeGeneral(mensaje) {

        if (!mensajeGeneral) {
            return;
        }

        mensajeGeneral.textContent = mensaje;
        mensajeGeneral.classList.add("is-visible");
    }

    // 3. Validación del nombre de usuario

    /**
     * Comprueba que el usuario haya ingresado su nombre de usuario.
     *
     * @returns {boolean} Indica si el campo es válido.
     */
    function validarUsuario() {

        if (!usuario) {
            return false;
        }

        const valor = usuario.value.trim();

        if (!valor) {
            mostrarError(
                usuario,
                errorUsuario,
                "Ingresa tu nombre de usuario."
            );

            return false;
        }

        limpiarError(usuario, errorUsuario);

        return true;
    }

    // 4. Validación de la contraseña

    /**
     * Comprueba que el usuario haya ingresado su contraseña.
     *
     * No se modifica ni se recortan los espacios de la contraseña,
     * porque estos pueden formar parte de la credencial.
     *
     * @returns {boolean} Indica si el campo es válido.
     */
    function validarContrasena() {

        if (!contrasena) {
            return false;
        }

        if (!contrasena.value) {
            mostrarError(
                contrasena,
                errorContrasena,
                "Ingresa tu contraseña."
            );

            return false;
        }

        limpiarError(contrasena, errorContrasena);

        return true;
    }

    // 5. Validación completa del formulario

    /**
     * Ejecuta las validaciones de todos los campos.
     *
     * @returns {boolean} True cuando todos los campos son válidos.
     */
    function validarFormulario() {

        const usuarioValido = validarUsuario();
        const contrasenaValida = validarContrasena();

        return usuarioValido && contrasenaValida;
    }

    // 6. Validación al salir de los campos

    if (usuario) {
        usuario.addEventListener("blur", validarUsuario);
    }

    if (contrasena) {
        contrasena.addEventListener("blur", validarContrasena);
    }

    // 7. Limpieza de errores mientras se escribe

    if (usuario) {

        usuario.addEventListener("input", () => {

            limpiarMensajeGeneral();
            limpiarError(usuario, errorUsuario);

        });

    }

    if (contrasena) {

        contrasena.addEventListener("input", () => {

            limpiarMensajeGeneral();
            limpiarError(contrasena, errorContrasena);

        });

    }

    // 8. Control del estado de carga

    /**
     * Actualiza el botón mientras se procesa el envío.
     *
     * @param {boolean} cargando - Indica si el formulario se está enviando.
     */
    function establecerCarga(cargando) {

        if (!botonEnviar) {
            return;
        }

        botonEnviar.disabled = cargando;

        botonEnviar.setAttribute(
            "aria-busy",
            String(cargando)
        );

        if (textoBoton) {
            textoBoton.hidden = cargando;
        }

        if (indicadorCarga) {
            indicadorCarga.hidden = !cargando;
        }
    }

    // 9. Control del envío del formulario

    form.addEventListener("submit", evento => {

        // Evita que el formulario se envíe antes de validarlo.
        evento.preventDefault();

        // Evita múltiples envíos mientras se procesa el anterior.
        if (enviando) {
            return;
        }

        limpiarMensajeGeneral();

        // Comprueba que los campos obligatorios estén completos.
        if (!validarFormulario()) {

            mostrarMensajeGeneral(
                "Completa los campos indicados para continuar."
            );

            const primerCampoIncorrecto = form.querySelector(
                '[aria-invalid="true"]'
            );

            if (primerCampoIncorrecto) {
                primerCampoIncorrecto.focus();
            }

            return;
        }

        // Activa el estado de envío.
        enviando = true;
        establecerCarga(true);

        // Envía el formulario al LoginController de Spring Boot.
        // Se conserva el método POST y la acción definida en Thymeleaf.
        HTMLFormElement.prototype.submit.call(form);

    });

    // 10. Configuración inicial

    establecerCarga(false);

    // Al volver con "Atrás", el navegador puede restaurar la página con
    // el botón en "Ingresando...". Se reinicia.
    window.addEventListener("pageshow", evento => {

        if (evento.persisted) {
            enviando = false;
            establecerCarga(false);
        }

    });

});
