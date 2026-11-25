/*
 * PROYECTO: Feeling
 * ARCHIVO: admin-usuarios.js
 * UBICACIÓN: src/main/resources/static/js/admin-usuarios.js
 * MÓDULO: Gestión de usuarios (panel del administrador)
 *
 * AUTOR: Yulipaola Domínguez
 * VERSIÓN: 1.1.0
 * FECHA DE DOCUMENTACIÓN: 2026-11-25
 * ESTADO: Finalizado
 * TICKET: SCRUM-103 (RF-17)
 *
 * DESCRIPCIÓN:
 * Script de la pantalla admin-usuarios.html. Maneja el formulario para
 * suspender a un voluntario: lo abre y lo cierra, cuenta los caracteres
 * del motivo y revisa los datos antes de enviarlo.
 *
 * FUNCIONALIDADES:
 * - Botón Suspender: abre el formulario de la tarjeta y lleva el foco al motivo.
 * - Botón Cancelar: cierra el formulario y devuelve el foco al botón Suspender.
 * - Contador de caracteres del motivo.
 * - Validación al enviar: el motivo es obligatorio y la fecha de fin debe
 *   ser posterior a hoy.
 * - Estado de carga del botón mientras se envía, para evitar el doble envío.
 * - Avisos para lectores de pantalla.
 *
 * LO QUE NO HACE ESTE ARCHIVO:
 * Las alertas, la entrada de las tarjetas y la confirmación del botón
 * Reactivar las maneja solicitudes.js.
 *
 * DEPENDENCIAS:
 * - admin-usuarios.html: aporta los elementos con atributos data-*.
 * - solicitudes.js: alertas, entrada de tarjetas y botón Reactivar.
 *
 * ATRIBUTOS DATA-* UTILIZADOS:
 * - data-solicitudes: sección principal; si no existe, el script no hace nada.
 * - data-aviso: zona para lectores de pantalla.
 * - data-abrir-suspension: botón Suspender; su aria-controls indica el formulario.
 * - data-motivo-suspension y data-caracteres-motivo: motivo y su contador.
 * - data-fecha-suspension: fecha de fin de la suspensión.
 * - data-error-suspension: mensaje de error del formulario.
 * - data-cancelar-suspension y data-enviar-suspension: botones del formulario.
 *
 * HISTORIAL DE VERSIONES:
 * - 1.0.0: formulario de suspensión (SCRUM-103, RF-17).
 * - 1.1.0 (2026-11-25): documentación completa. No cambia la lógica.
 */


// El script se ejecuta cuando el documento terminó de cargarse.
document.addEventListener("DOMContentLoaded", () => {
    // Sección principal de la pantalla. Si no existe, no se hace nada.
    const seccion = document.querySelector("[data-solicitudes]");
    if (!seccion) {
        return;
    }

    // Zona de avisos para lectores de pantalla.
    const avisoLector = seccion.querySelector("[data-aviso]");

    // Anuncia un mensaje a los lectores de pantalla.
    function anunciar(mensaje) {
        if (avisoLector) {
            avisoLector.textContent = mensaje;
        }
    }

    // Se configura cada botón Suspender con su formulario.
    seccion.querySelectorAll("[data-abrir-suspension]").forEach(boton => {
        // El formulario de la tarjeta se identifica con aria-controls del botón.
        const formulario = document.getElementById(boton.getAttribute("aria-controls"));
        if (!formulario) {
            return;
        }

        // Elementos del formulario.
        const motivo = formulario.querySelector("[data-motivo-suspension]");
        const caracteres = formulario.querySelector("[data-caracteres-motivo]");
        const fecha = formulario.querySelector("[data-fecha-suspension]");
        const error = formulario.querySelector("[data-error-suspension]");
        const cancelar = formulario.querySelector("[data-cancelar-suspension]");
        const enviar = formulario.querySelector("[data-enviar-suspension]");

        // Abre o cierra el formulario y oculta el botón Suspender mientras está abierto.
        // Al abrir lleva el foco al motivo; al cerrar borra el error y vuelve al botón.
        function abrirFormulario(abrir) {
            formulario.hidden = !abrir;
            boton.hidden = abrir;
            boton.setAttribute("aria-expanded", String(abrir));
            if (abrir) {
                motivo.focus();
                anunciar("Formulario para suspender al voluntario.");
            } else {
                mostrarError("");
                boton.focus();
            }
        }

        // Muestra un mensaje de error (o lo borra si está vacío) y marca el campo
        // que tiene el problema para los lectores de pantalla.
        function mostrarError(mensaje, campo) {
            error.textContent = mensaje;
            error.hidden = !mensaje;
            motivo.removeAttribute("aria-invalid");
            fecha.removeAttribute("aria-invalid");
            if (campo) {
                campo.setAttribute("aria-invalid", "true");
                campo.focus();
            }
        }

        // Suspender abre el formulario y Cancelar lo cierra.
        boton.addEventListener("click", () => abrirFormulario(true));
        cancelar.addEventListener("click", () => abrirFormulario(false));

        // Cada vez que se escribe en el motivo se actualiza el contador.
        motivo.addEventListener("input", () => {
            caracteres.textContent = String(motivo.value.length);
        });

        // Antes de enviar se revisan los datos; si algo falta, se cancela el envío.
        formulario.addEventListener("submit", evento => {
            if (!motivo.value.trim()) {
                evento.preventDefault();
                mostrarError("Debe indicar el motivo de la suspensión.", motivo);
                return;
            }
            // La fecha llega como aaaa-mm-dd, así que se puede comparar como texto.
            if (!fecha.value || (fecha.min && fecha.value < fecha.min)) {
                evento.preventDefault();
                mostrarError("La fecha de fin debe ser posterior a hoy.", fecha);
                return;
            }
            // Si todo está bien, el botón muestra que se está enviando y se desactiva
            // para evitar el doble envío.
            mostrarError("");
            enviar.disabled = true;
            enviar.setAttribute("aria-busy", "true");
            enviar.textContent = "Suspendiendo...";
            anunciar("Suspendiendo al voluntario.");
        });
    });
});
