/*
 * PROYECTO: Feeling
 * ARCHIVO: solicitudes-recibidas.js
 * UBICACIÓN: src/main/resources/static/js/solicitudes-recibidas.js
 * MÓDULO: Solicitudes recibidas (panel del voluntario)
 *
 * AUTOR: Yulipaola Domínguez
 * VERSIÓN: 1.3.0
 * FECHA DE DOCUMENTACIÓN: 2026-11-25
 * ESTADO: Finalizado
 * TICKET: SCRUM-83 (RF-10), con ampliación de SCRUM-97 (RF-14)
 *
 * DESCRIPCIÓN:
 * Script de la pantalla solicitudes-recibidas.html. Agrega el comportamiento
 * propio de esta pantalla: abrir y cerrar el detalle de cada solicitud,
 * ajustar el texto del contador de pendientes, manejar el formulario para
 * marcar un acompañamiento como realizado y dar un efecto de presión a las
 * tarjetas en pantallas táctiles.
 *
 * FUNCIONALIDADES:
 * - Botón Ver: muestra u oculta el detalle de la solicitud.
 * - Contador: cambia entre "pendiente" y "pendientes" según el número.
 * - Marcar como realizado (RF-14): abre el formulario de duración y notas,
 *   cuenta los caracteres de las notas y muestra la carga al guardar.
 * - Efecto de presión al tocar una tarjeta en pantallas táctiles.
 * - Avisos para lectores de pantalla en cada cambio.
 *
 * LO QUE NO HACE ESTE ARCHIVO:
 * Aceptar, Rechazar (con su confirmación), la salida de la tarjeta, la
 * actualización del número del contador y las alertas las maneja solicitudes.js.
 *
 * DEPENDENCIAS:
 * - solicitudes-recibidas.html: aporta los elementos con atributos data-*.
 * - solicitudes.js: actualiza el número del contador que este archivo observa.
 *
 * ATRIBUTOS DATA-* UTILIZADOS:
 * - data-recibidas: sección principal; si no existe, el script no hace nada.
 * - data-aviso: zona para lectores de pantalla.
 * - data-ver: botón que abre el detalle de la solicitud.
 * - data-contador y data-contador-texto: número y texto de pendientes.
 * - data-abrir-seguimiento: botón que abre el formulario de seguimiento.
 * - data-notas-seguimiento, data-caracteres-notas, data-cancelar-seguimiento
 *   y data-guardar-seguimiento: elementos dentro del formulario de seguimiento.
 *
 * HISTORIAL DE VERSIONES:
 * - 1.0.0: botón Ver, contador y efecto de presión (SCRUM-83, RF-10).
 * - 1.3.0: formulario de seguimiento (SCRUM-97, RF-14).
 * - 1.3.1 (2026-11-25): documentación completa. No cambia la lógica.
 */

// El script se ejecuta cuando el documento terminó de cargarse.
document.addEventListener("DOMContentLoaded", () => {
    "use strict";

    // Sección principal de la pantalla. Si no existe, no se hace nada.
    const seccion = document.querySelector("[data-recibidas]");
    if (!seccion) {
        return;
    }

    // Zona de avisos para lectores de pantalla.
    const avisoLector = seccion.querySelector("[data-aviso]");

    /**
     * Anuncia un mensaje a los lectores de pantalla.
     *
     * @param {string} mensaje texto a anunciar
     */
    function anunciar(mensaje) {
        if (avisoLector) {
            avisoLector.textContent = mensaje;
        }
    }

    // 1. "Ver" muestra u oculta el tipo de apoyo, la modalidad y la fecha de envío.
    // Cada botón controla el detalle que indica su atributo aria-controls.
    seccion.querySelectorAll("[data-ver]").forEach(boton => {
        const detalle = document.getElementById(boton.getAttribute("aria-controls"));
        if (!detalle) {
            return;
        }

        boton.addEventListener("click", () => {
            // Si el detalle está oculto, el clic lo abre; si no, lo cierra.
            const abrir = detalle.hidden;
            detalle.hidden = !abrir;
            // Se actualizan el estado para tecnologías de apoyo y el texto del botón.
            boton.setAttribute("aria-expanded", String(abrir));
            boton.textContent = abrir ? "Ocultar" : "Ver";
            anunciar(abrir ? "Detalle de la solicitud abierto." : "Detalle de la solicitud cerrado.");
        });
    });

    // 2. "1 pendiente" o "N pendientes" cuando solicitudes.js cambia el contador.
    // Se observa el número del contador y, cuando cambia, se ajusta el texto
    // del lado según sea singular o plural.
    const contador = seccion.querySelector("[data-contador]");
    const textoContador = seccion.querySelector("[data-contador-texto]");
    if (contador && textoContador && "MutationObserver" in window) {
        new MutationObserver(() => {
            textoContador.textContent = contador.textContent.trim() === "1" ? "pendiente" : "pendientes";
        }).observe(contador, { childList: true, characterData: true, subtree: true });
    }

    // 3. "Marcar como realizado" abre y cierra el formulario de seguimiento (RF-14).
    // Se configura cada tarjeta de la sección "Por marcar como realizados".
    seccion.querySelectorAll("[data-abrir-seguimiento]").forEach(boton => {
        // El formulario de la tarjeta se identifica con aria-controls del botón.
        const formulario = document.getElementById(boton.getAttribute("aria-controls"));
        if (!formulario) {
            return;
        }
        // Elementos internos del formulario.
        const notas = formulario.querySelector("[data-notas-seguimiento]");
        const caracteres = formulario.querySelector("[data-caracteres-notas]");
        const cancelar = formulario.querySelector("[data-cancelar-seguimiento]");
        const guardar = formulario.querySelector("[data-guardar-seguimiento]");

        /**
         * Abre o cierra el formulario de seguimiento de esta tarjeta.
         * Al abrir oculta el botón y lleva el foco a la duración elegida;
         * al cerrar devuelve el foco al botón.
         *
         * @param {boolean} abrir true para abrir, false para cerrar
         */
        function abrirFormulario(abrir) {
            formulario.hidden = !abrir;
            boton.hidden = abrir;
            boton.setAttribute("aria-expanded", String(abrir));
            if (abrir) {
                // El foco va a la duración marcada o, si no hay, a la primera.
                const elegida = formulario.querySelector("input[name='duracionMinutos']:checked")
                    || formulario.querySelector("input[name='duracionMinutos']");
                if (elegida) {
                    elegida.focus();
                }
                anunciar("Formulario para marcar el acompañamiento como realizado.");
            } else {
                boton.focus();
            }
        }

        boton.addEventListener("click", () => abrirFormulario(true));
        if (cancelar) {
            cancelar.addEventListener("click", () => abrirFormulario(false));
        }

        // Contador de caracteres de las notas.
        if (notas && caracteres) {
            notas.addEventListener("input", () => {
                caracteres.textContent = String(notas.value.length);
            });
        }

        // Muestra la carga y evita el doble envío.
        // El botón se desactiva y cambia de texto mientras el servidor responde.
        formulario.addEventListener("submit", () => {
            if (guardar) {
                guardar.disabled = true;
                guardar.setAttribute("aria-busy", "true");
                guardar.textContent = "Guardando...";
            }
            anunciar("Guardando el seguimiento.");
        });
    });

    // 4. Efecto de presión al tocar una solicitud (solo pantallas táctiles).
    // La clase presionada se agrega al tocar la tarjeta, salvo que el toque
    // sea sobre un botón, y se quita al soltar, cancelar o salir de la tarjeta.
    seccion.querySelectorAll(".recibida").forEach(tarjeta => {
        tarjeta.addEventListener("pointerdown", evento => {
            if (evento.pointerType === "touch" && !evento.target.closest("button")) {
                tarjeta.classList.add("presionada");
            }
        });

        ["pointerup", "pointercancel", "pointerleave"].forEach(tipo => {
            tarjeta.addEventListener(tipo, () => tarjeta.classList.remove("presionada"));
        });
    });
});