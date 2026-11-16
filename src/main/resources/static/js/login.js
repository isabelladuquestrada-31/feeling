
/**
 * PROYECTO: FEELING
 * MÓDULO: INICIO DE SESIÓN
 * ARCHIVO: login.js
 *
 * VERSIÓN: 1.0.0
 *
 * DESCRIPCIÓN:
 * Gestiona las interacciones del formulario de inicio de sesión.
 *
 * FUNCIONALIDADES:
 * - Validación de campos obligatorios.
 * - Validación de usuario y contraseña.
 * - Mostrar y ocultar la contraseña.
 * - Visualización de mensajes de validación.
 * - Control del estado de carga del botón.
 * - Prevención de envíos repetidos.
 * - Accesibilidad mediante teclado y lectores de pantalla.
 *
 * INTEGRACIÓN:
 * El formulario se envía mediante POST a /login.
 * La autenticación y validación de credenciales son responsabilidad
 * del LoginController y del ServicioAutenticacion de Spring Boot.
 *
 * IMPORTANTE:
 * Este archivo no realiza la autenticación ni almacena contraseñas.
 */

document.addEventListener("DOMContentLoaded", () => {

    "use strict";

    // ----------------------------------------------------------
    // 1. REFERENCIAS A LOS ELEMENTOS DEL FORMULARIO
    // ----------------------------------------------------------

    const form = document.getElementById("loginForm");

    // Comprueba que la página contiene el formulario de inicio.
    if (!form) {
        console.error("Feeling: no se encontró el formulario de inicio de sesión.");
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

    // ----------------------------------------------------------
    // 2. FUNCIONES PARA MOSTRAR Y LIMPIAR ERRORES
    // ----------------------------------------------------------

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

    // ----------------------------------------------------------
    // 3. VALIDACIÓN DEL NOMBRE DE USUARIO
    // ----------------------------------------------------------

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

    // ----------------------------------------------------------
    // 4. VALIDACIÓN DE LA CONTRASEÑA
    // ----------------------------------------------------------

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

    // ----------------------------------------------------------
    // 5. VALIDACIÓN COMPLETA DEL FORMULARIO
    // ----------------------------------------------------------

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

    // ----------------------------------------------------------
    // 6. MOSTRAR Y OCULTAR CONTRASEÑA
    // ----------------------------------------------------------

    /**
     * Permite alternar la visibilidad de la contraseña.
     *
     * El botón correspondiente debe tener el atributo:
     * data-toggle-password="contrasena"
     */
    document.querySelectorAll("[data-toggle-password]").forEach(boton => {

        boton.addEventListener("click", () => {

            const identificador = boton.dataset.togglePassword;
            const campo = document.getElementById(identificador);

            if (!campo) {
                console.error(
                    "Feeling: no se encontró el campo de contraseña:",
                    identificador
                );
                return;
            }

            const mostrar = campo.type === "password";

            campo.type = mostrar ? "text" : "password";

            boton.setAttribute(
                "aria-label",
                mostrar ? "Ocultar contraseña" : "Mostrar contraseña"
            );

            boton.setAttribute(
                "aria-pressed",
                String(mostrar)
            );

            campo.focus();
        });

    });

    // ----------------------------------------------------------
    // 7. VALIDACIÓN AL SALIR DE LOS CAMPOS
    // ----------------------------------------------------------

    if (usuario) {
        usuario.addEventListener("blur", validarUsuario);
    }

    if (contrasena) {
        contrasena.addEventListener("blur", validarContrasena);
    }

    // ----------------------------------------------------------
    // 8. LIMPIEZA DE ERRORES MIENTRAS SE ESCRIBE
    // ----------------------------------------------------------

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

    // ----------------------------------------------------------
    // 9. CONTROL DEL ESTADO DE CARGA
    // ----------------------------------------------------------

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

    // ----------------------------------------------------------
    // 10. CONTROL DEL ENVÍO DEL FORMULARIO
    // ----------------------------------------------------------

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

    // ----------------------------------------------------------
    // 11. CONFIGURACIÓN INICIAL
    // ----------------------------------------------------------

    establecerCarga(false);

    console.info(
        "Feeling: formulario de inicio de sesión inicializado correctamente."
    );

});
