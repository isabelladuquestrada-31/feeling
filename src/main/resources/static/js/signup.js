/**
 * PROYECTO: Feeling
 * MÓDULO: Registro de estudiantes (RF-01)
 * ARCHIVO: signup.js
 * UBICACIÓN: src/main/resources/static/js/signup.js
 *
 * AUTOR: Yulipaola Domínguez
 * VERSIÓN: 1.2.0
 * FECHA DE DOCUMENTACIÓN: 2026-11-22
 * ESTADO: Finalizado
 * TICKET: SCRUM-48
 *
 * DESCRIPCIÓN:
 * Comportamiento del formulario de registro. Valida los campos en el
 * navegador antes de enviarlos, muestra los mensajes de error junto a
 * cada campo y controla el envío para que no se haga dos veces. La
 * validación definitiva la hace el servidor (RegistroController).
 *
 * FUNCIONALIDADES PRINCIPALES:
 * - Validar nombre completo, contraseña, confirmación, programa
 *   académico, WhatsApp (+57 y 10 dígitos) y aceptación del aviso.
 * - Mostrar y ocultar las contraseñas.
 * - Abrir y cerrar el diálogo del aviso de privacidad.
 * - Mostrar el estado "Creando cuenta..." mientras se envía.
 * - Llevar el foco al mensaje de error que devuelve el servidor.
 * - Quitar el estado de carga si el navegador restaura la página
 *   al usar el botón "Atrás".
 *
 * VARIABLES PRINCIPALES:
 * - fields: campos del formulario (HTMLElement).
 * - errorElements: textos de error de cada campo (HTMLElement).
 * - CONFIG: límites de longitud y formato de los campos (Object).
 * - isSubmitting: indica si el formulario ya se está enviando (boolean).
 *
 * FUNCIONES:
 * - showError / showSuccess / clearValidation: estado visual de un campo.
 * - validateName, validatePassword, validateConfirmPassword,
 *   validateProgram, validateWhatsapp, validatePrivacy: reglas de cada campo.
 * - validateForm(): ejecuta todas las validaciones.
 * - setLoading(loading): activa o desactiva el estado de carga del botón.
 *
 * TECNOLOGÍAS:
 * - JavaScript (ECMAScript) y API del DOM.
 * - Elemento <dialog> de HTML.
 *
 * DEPENDENCIAS:
 * - signup.html: identificadores del formulario (signupForm, signupSubmit,
 *   serverError, etc.) y atributos data-toggle-password,
 *   data-open-dialog y data-close-dialog.
 * - signup.css: clases visible, is-visible, input-error e input-success.
 *
 * HISTORIAL DE VERSIONES:
 * - 1.0.0: validaciones del formulario.
 * - 1.1.0: validación del programa académico como lista cerrada.
 * - 1.2.0: foco en el error del servidor, reinicio del botón al volver
 *   con "Atrás", se quita la validación de correo (el formulario no
 *   lo pide) y documentación.
 */

document.addEventListener("DOMContentLoaded", () => {

    "use strict";

    // 1. Elementos del formulario

    const form = document.getElementById("signupForm");

    if (!form) {
        return;
    }

    const fields = {
        nombre: document.getElementById("nombreUsuario"),
        contrasena: document.getElementById("contrasena"),
        confirmarContrasena: document.getElementById("confirmarContrasena"),
        programa: document.getElementById("programaAcademico"),
        whatsapp: document.getElementById("numeroWhatsApp"),
        privacidad: document.getElementById("aceptaTratamientoDatos")
    };

    const errorElements = {
        nombre: document.getElementById("errorNombreUsuario"),
        contrasena: document.getElementById("errorContrasena"),
        confirmarContrasena: document.getElementById("errorConfirmarContrasena"),
        programa: document.getElementById("errorProgramaAcademico"),
        whatsapp: document.getElementById("errorNumeroWhatsApp"),
        privacidad: document.getElementById("errorTratamientoDatos")
    };

    // Botón de envío, estado de carga y mensaje general.
    const submitButton = document.getElementById("signupSubmit");
    const submitText = document.getElementById("signupSubmitText");
    const submitLoading = document.getElementById("signupLoading");
    const generalMessage = document.getElementById("signupFormMessage");

    const privacyDialog = document.getElementById("privacyDialog");

    let isSubmitting = false;

    const CONFIG = {
        minPasswordLength: 8,
        maxPasswordLength: 64,
        maxNameLength: 50,
        whatsappPrefix: "+57",
        whatsappDigits: 10
    };

    // 2. Funciones generales

    function getValue(field) {
        return field ? field.value.trim() : "";
    }

    function normalizeSpaces(value) {
        return value.replace(/\s+/g, " ").trim();
    }

    function showError(field, errorElement, message) {

        if (field) {
            field.classList.add("input-error");
            field.classList.remove("input-success");
            field.setAttribute("aria-invalid", "true");
        }

        if (errorElement) {
            errorElement.textContent = message;
            errorElement.classList.add("visible");
        }

        return false;
    }

    function showSuccess(field, errorElement) {

        if (field) {
            field.classList.remove("input-error");
            field.classList.add("input-success");
            field.setAttribute("aria-invalid", "false");
        }

        if (errorElement) {
            errorElement.textContent = "";
            errorElement.classList.remove("visible");
        }

        return true;
    }

    function clearValidation(field, errorElement) {

        if (field) {
            field.classList.remove("input-error", "input-success");
            field.removeAttribute("aria-invalid");
        }

        if (errorElement) {
            errorElement.textContent = "";
            errorElement.classList.remove("visible");
        }
    }

    // 3. Mensaje general del formulario

    function showGeneralMessage(message) {

        if (!generalMessage) {
            return;
        }

        generalMessage.textContent = message;

        generalMessage.classList.add("is-visible");
    }

    function clearGeneralMessage() {

        if (!generalMessage) {
            return;
        }

        generalMessage.textContent = "";
        generalMessage.classList.remove("is-visible");
    }

    // 4. Validación del nombre completo

    function isValidFullName(value) {

        const namePattern =
            /^[\p{L}]+(?:[ '\u2019-][\p{L}]+)*(?:\s+[\p{L}]+(?:[ '\u2019-][\p{L}]+)*)+$/u;

        return namePattern.test(value);
    }

    function validateName() {

        const field = fields.nombre;
        const error = errorElements.nombre;

        if (!field) {
            return false;
        }

        const value = normalizeSpaces(getValue(field));

        if (!value) {
            return showError(
                field,
                error,
                "Ingresa tu nombre y apellido."
            );
        }

        if (value.length > CONFIG.maxNameLength) {
            return showError(
                field,
                error,
                "El nombre completo no puede superar los 50 caracteres."
            );
        }

        if (!isValidFullName(value)) {
            return showError(
                field,
                error,
                "Ingresa tu nombre y apellido utilizando únicamente letras."
            );
        }

        field.value = value;

        return showSuccess(field, error);
    }

    // 5. Validación de la contraseña

    function validatePassword() {

        const field = fields.contrasena;
        const error = errorElements.contrasena;

        if (!field) {
            return false;
        }

        const password = field.value;

        if (!password) {
            return showError(
                field,
                error,
                "Crea una contraseña para tu cuenta."
            );
        }

        if (password.length < CONFIG.minPasswordLength) {
            return showError(
                field,
                error,
                "La contraseña debe tener mínimo 8 caracteres."
            );
        }

        if (password.length > CONFIG.maxPasswordLength) {
            return showError(
                field,
                error,
                "La contraseña no puede superar los 64 caracteres."
            );
        }

        if (!/[A-Za-zÁÉÍÓÚáéíóúÑñ]/.test(password) || !/[0-9]/.test(password)) {
            return showError(
                field,
                error,
                "Incluye al menos una letra y un número."
            );
        }

        showSuccess(field, error);

        // Si ya existe una confirmación, también se comprueba.
        if (
            fields.confirmarContrasena &&
            fields.confirmarContrasena.value.length > 0
        ) {
            validateConfirmPassword();
        }

        return true;
    }

    // 6. Validación de confirmación de contraseña

    function validateConfirmPassword() {

        const field = fields.confirmarContrasena;
        const error = errorElements.confirmarContrasena;

        if (!field || !fields.contrasena) {
            return false;
        }

        const password = fields.contrasena.value;
        const confirmation = field.value;

        if (!confirmation) {
            return showError(
                field,
                error,
                "Confirma tu contraseña."
            );
        }

        if (password !== confirmation) {
            return showError(
                field,
                error,
                "Las contraseñas no coinciden."
            );
        }

        return showSuccess(field, error);
    }

    // 7. Validación del programa académico

    function validateProgram() {

        const field = fields.programa;
        const error = errorElements.programa;

        if (!field) {
            return false;
        }

        const value = getValue(field);

        // El programa se elige de una lista: solo hay que comprobar que eligió uno.
        if (!value) {
            return showError(
                field,
                error,
                "Selecciona tu programa académico."
            );
        }

        return showSuccess(field, error);
    }

    // 8. Validación del número de WhatsApp

    function validateWhatsapp() {

        const field = fields.whatsapp;
        const error = errorElements.whatsapp;

        if (!field) {
            return false;
        }

        const value = getValue(field);

        if (!value) {
            return showError(
                field,
                error,
                "Ingresa tu número de WhatsApp."
            );
        }

        const normalizedValue = value.replace(/\s/g, "");

        const whatsappPattern = /^\+57\d{10}$/;

        if (!whatsappPattern.test(normalizedValue)) {
            return showError(
                field,
                error,
                "Ingresa +57 seguido de exactamente 10 dígitos."
            );
        }

        field.value = "+57 " + normalizedValue.substring(3);

        return showSuccess(field, error);
    }

    // 9. Validación del tratamiento de datos

    function validatePrivacy() {

        const checkbox = fields.privacidad;
        const error = errorElements.privacidad;

        if (!checkbox) {
            return false;
        }

        if (!checkbox.checked) {

            checkbox.setAttribute("aria-invalid", "true");

            if (error) {
                error.textContent =
                    "Debes aceptar el tratamiento de datos para crear tu cuenta.";

                error.classList.add("visible");
            }

            return false;
        }

        checkbox.setAttribute("aria-invalid", "false");

        if (error) {
            error.textContent = "";
            error.classList.remove("visible");
        }

        return true;
    }

    // 10. Mostrar y ocultar contraseñas

    document.querySelectorAll("[data-toggle-password]").forEach(button => {

        button.addEventListener("click", () => {

            const targetId = button.dataset.togglePassword;
            const input = document.getElementById(targetId);

            if (!input) {
                console.error(
                    "No se encontró el campo de contraseña:",
                    targetId
                );
                return;
            }

            const isPasswordVisible = input.type === "password";

            input.type = isPasswordVisible ? "text" : "password";

            button.setAttribute(
                "aria-label",
                isPasswordVisible
                    ? "Ocultar contraseña"
                    : "Mostrar contraseña"
            );

            button.setAttribute(
                "aria-pressed",
                String(isPasswordVisible)
            );

            button.classList.toggle(
                "is-visible",
                isPasswordVisible
            );

            input.focus();
        });

    });

    // 11. Apertura del diálogo de privacidad

    document.querySelectorAll("[data-open-dialog]").forEach(button => {

        button.addEventListener("click", () => {

            const dialogId = button.dataset.openDialog;
            const dialog = document.getElementById(dialogId);

            if (!dialog) {
                return;
            }

            if (typeof dialog.showModal === "function") {
                dialog.showModal();
            }

        });

    });

    // 12. Cierre del diálogo de privacidad

    document.querySelectorAll("[data-close-dialog]").forEach(button => {

        button.addEventListener("click", () => {

            const dialog = button.closest("dialog");

            if (dialog && dialog.open) {
                dialog.close();
            }

        });

    });

    // Permite cerrar el diálogo al pulsar sobre el fondo.
    if (privacyDialog) {

        privacyDialog.addEventListener("click", event => {

            if (event.target === privacyDialog) {
                privacyDialog.close();
            }

        });

    }

    // 13. Validaciones al salir de los campos

    if (fields.nombre) {
        fields.nombre.addEventListener("blur", validateName);
    }

    if (fields.whatsapp) {
        fields.whatsapp.addEventListener("blur", validateWhatsapp);
    }

    if (fields.programa) {
        fields.programa.addEventListener("blur", validateProgram);
        fields.programa.addEventListener("change", validateProgram);
    }

    if (fields.contrasena) {
        fields.contrasena.addEventListener("blur", validatePassword);
    }

    if (fields.confirmarContrasena) {
        fields.confirmarContrasena.addEventListener(
            "blur",
            validateConfirmPassword
        );
    }

    // 14. Validaciones mientras se escribe

    if (fields.contrasena) {

        fields.contrasena.addEventListener("input", () => {

            clearGeneralMessage();

            if (fields.contrasena.classList.contains("input-error")) {
                validatePassword();
            }

            if (
                fields.confirmarContrasena &&
                fields.confirmarContrasena.value.length > 0
            ) {
                validateConfirmPassword();
            }

        });

    }

    if (fields.confirmarContrasena) {

        fields.confirmarContrasena.addEventListener("input", () => {

            clearGeneralMessage();

            if (fields.confirmarContrasena.value.length > 0) {
                validateConfirmPassword();
            }

        });

    }

    // 15. Limpieza de mensajes generales

    Object.values(fields).forEach(field => {

        if (!field) {
            return;
        }

        const eventName =
            field.type === "checkbox" ? "change" : "input";

        field.addEventListener(eventName, clearGeneralMessage);

    });

    // 16. Validación completa del formulario

    function validateForm() {

        const validations = [
            validateName(),
            validatePassword(),
            validateConfirmPassword(),
            validateProgram(),
            validateWhatsapp(),
            validatePrivacy()
        ];

        return validations.every(result => result === true);
    }

    // 17. Enfocar el primer campo incorrecto

    function focusFirstInvalidField() {

        const firstInvalidField = form.querySelector(
            '[aria-invalid="true"]'
        );

        if (firstInvalidField) {
            firstInvalidField.focus();
            return;
        }

        if (generalMessage) {
            generalMessage.focus();
        }
    }

    // 18. Estado de carga del botón

    function setLoading(loading) {

        if (!submitButton) {
            return;
        }

        submitButton.disabled = loading;

        submitButton.setAttribute(
            "aria-busy",
            String(loading)
        );

        if (submitText) {
            submitText.hidden = loading;
        }

        if (submitLoading) {
            submitLoading.hidden = !loading;
        }
    }

    // 19. Control del envío del formulario

    form.addEventListener("submit", event => {

        // Detiene el envío automático para validar primero.
        event.preventDefault();

        // Evita procesar un segundo envío mientras ya se envía.
        if (isSubmitting) {
            return;
        }

        clearGeneralMessage();

        const isValid = validateForm();

        // Si existe al menos un error, no se envía el formulario.
        if (!isValid) {

            showGeneralMessage(
                "Revisa los campos del formulario. Debes completar correctamente toda la información antes de crear tu cuenta."
            );

            focusFirstInvalidField();

            return;
        }

        // Solo permite el envío cuando todas las validaciones pasan.
        isSubmitting = true;

        setLoading(true);

        // Envía el formulario sin volver a disparar este evento.
        HTMLFormElement.prototype.submit.call(form);

    }, true);

    // 20. Estado inicial

    setLoading(false);

    // Si el servidor rechazó el registro (por ejemplo, nombre de usuario
    // repetido), se muestra el mensaje y se lleva el foco hasta él.
    const serverError = document.getElementById("serverError");

    if (serverError) {
        serverError.scrollIntoView({ block: "center" });
        serverError.focus({ preventScroll: true });
    }

    // Al volver con "Atrás", el navegador puede restaurar la página tal
    // como quedó (con el botón en "Creando cuenta..."). Se reinicia.
    window.addEventListener("pageshow", event => {

        if (event.persisted) {
            isSubmitting = false;
            setLoading(false);
        }

    });

});
