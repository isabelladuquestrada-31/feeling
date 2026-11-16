
/* ==========================================================
   PROYECTO: FEELING
   MÓDULO: REGISTRO DE ESTUDIANTES
   ARCHIVO: signup.js

   Funcionalidades:
   - Validación de campos.
   - Mensajes de error y éxito.
   - Validación de correo electrónico.
   - Validación de contraseña y confirmación.
   - Validación de WhatsApp.
   - Aceptación del tratamiento de datos.
   - Mostrar y ocultar contraseñas.
   - Apertura y cierre del diálogo de privacidad.
   - Control del envío del formulario.
   ========================================================== */

document.addEventListener("DOMContentLoaded", () => {

    "use strict";

    // ==========================================================
    // 1. ELEMENTOS DEL FORMULARIO
    // ==========================================================

    const form = document.getElementById("signupForm");

    if (!form) {
        console.error("Feeling: no se encontró el formulario de registro.");
        return;
    }

    const fields = {
        nombre: document.getElementById("nombreUsuario"),
        correo: document.getElementById("correo"),
        contrasena: document.getElementById("contrasena"),
        confirmarContrasena: document.getElementById("confirmarContrasena"),
        programa: document.getElementById("programaAcademico"),
        whatsapp: document.getElementById("numeroWhatsApp"),
        privacidad: document.getElementById("aceptaTratamientoDatos")
    };

    const errorElements = {
        nombre: document.getElementById("errorNombreUsuario"),
        correo: document.getElementById("errorCorreo"),
        contrasena: document.getElementById("errorContrasena"),
        confirmarContrasena: document.getElementById("errorConfirmarContrasena"),
        programa: document.getElementById("errorProgramaAcademico"),
        whatsapp: document.getElementById("errorNumeroWhatsApp"),
        privacidad: document.getElementById("errorTratamientoDatos")
    };

    // Estos identificadores corresponden a tu HTML.
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
        maxProgramLength: 100,
        maxEmailLength: 254,
        whatsappPrefix: "+57",
        whatsappDigits: 10
    };

    // ==========================================================
    // 2. FUNCIONES GENERALES
    // ==========================================================

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

    // ==========================================================
    // 3. MENSAJE GENERAL DEL FORMULARIO
    // ==========================================================

    function showGeneralMessage(message) {

        if (!generalMessage) {
            return;
        }

        generalMessage.textContent = message;

        // Esta clase coincide con la regla CSS is-visible.
        generalMessage.classList.add("is-visible");
    }

    function clearGeneralMessage() {

        if (!generalMessage) {
            return;
        }

        generalMessage.textContent = "";
        generalMessage.classList.remove("is-visible");
    }

    // ==========================================================
    // 4. VALIDACIÓN DEL NOMBRE COMPLETO
    // ==========================================================

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

    // ==========================================================
    // 5. VALIDACIÓN DEL CORREO ELECTRÓNICO
    // ==========================================================

    function validateEmail() {

        const field = fields.correo;
        const error = errorElements.correo;

        // El formulario de registro no pide correo: si el campo no existe,
        // esta validación no debe bloquear el envío.
        if (!field) {
            return true;
        }

        const email = getValue(field);

        if (!email) {
            return showError(
                field,
                error,
                "Ingresa tu correo electrónico."
            );
        }

        if (email.length > CONFIG.maxEmailLength) {
            return showError(
                field,
                error,
                "El correo electrónico supera la longitud permitida."
            );
        }

        // Acepta correos válidos sin exigir un dominio institucional.
        const emailPattern =
            /^[^\s@]+@[^\s@]+\.[^\s@]{2,}$/;

        if (!emailPattern.test(email)) {
            return showError(
                field,
                error,
                "Ingresa un correo electrónico válido."
            );
        }

        return showSuccess(field, error);
    }

    // ==========================================================
    // 6. VALIDACIÓN DE LA CONTRASEÑA
    // ==========================================================

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

    // ==========================================================
    // 7. VALIDACIÓN DE CONFIRMACIÓN DE CONTRASEÑA
    // ==========================================================

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

    // ==========================================================
    // 8. VALIDACIÓN DEL PROGRAMA ACADÉMICO
    // ==========================================================

    function validateProgram() {

        const field = fields.programa;
        const error = errorElements.programa;

        if (!field) {
            return false;
        }

        const value = getValue(field);

        if (!value) {
            return showError(
                field,
                error,
                "Ingresa tu programa académico."
            );
        }

        if (value.length > CONFIG.maxProgramLength) {
            return showError(
                field,
                error,
                "El programa académico no puede superar los 100 caracteres."
            );
        }

        return showSuccess(field, error);
    }

    // ==========================================================
    // 9. VALIDACIÓN DEL NÚMERO DE WHATSAPP
    // ==========================================================

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

    // ==========================================================
    // 10. VALIDACIÓN DEL TRATAMIENTO DE DATOS
    // ==========================================================

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

    // ==========================================================
    // 11. MOSTRAR Y OCULTAR CONTRASEÑAS
    // ==========================================================

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

    // ==========================================================
    // 12. APERTURA DEL DIÁLOGO DE PRIVACIDAD
    // ==========================================================

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

    // ==========================================================
    // 13. CIERRE DEL DIÁLOGO DE PRIVACIDAD
    // ==========================================================

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

    // ==========================================================
    // 14. VALIDACIONES AL SALIR DE LOS CAMPOS
    // ==========================================================

    if (fields.nombre) {
        fields.nombre.addEventListener("blur", validateName);
    }

    if (fields.correo) {
        fields.correo.addEventListener("blur", validateEmail);
    }

    if (fields.whatsapp) {
        fields.whatsapp.addEventListener("blur", validateWhatsapp);
    }

    if (fields.programa) {
        fields.programa.addEventListener("blur", validateProgram);
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

    // ==========================================================
    // 15. VALIDACIONES MIENTRAS SE ESCRIBE
    // ==========================================================

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

    // ==========================================================
    // 16. LIMPIEZA DE MENSAJES GENERALES
    // ==========================================================

    Object.values(fields).forEach(field => {

        if (!field) {
            return;
        }

        const eventName =
            field.type === "checkbox" ? "change" : "input";

        field.addEventListener(eventName, clearGeneralMessage);

    });

    // ==========================================================
    // 17. VALIDACIÓN COMPLETA DEL FORMULARIO
    // ==========================================================

    function validateForm() {

        const validations = [
            validateName(),
            validateEmail(),
            validatePassword(),
            validateConfirmPassword(),
            validateProgram(),
            validateWhatsapp(),
            validatePrivacy()
        ];

        return validations.every(result => result === true);
    }

    // ==========================================================
    // 18. ENFOCAR EL PRIMER CAMPO INCORRECTO
    // ==========================================================

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

    // ==========================================================
    // 19. ESTADO DE CARGA DEL BOTÓN
    // ==========================================================

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

    // ==========================================================
    // 20. CONTROL DEL ENVÍO DEL FORMULARIO
    // ==========================================================

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

    // ==========================================================
    // 21. ESTADO INICIAL
    // ==========================================================

    setLoading(false);

    console.info(
        "Feeling: validaciones del formulario de registro inicializadas."
    );

});
