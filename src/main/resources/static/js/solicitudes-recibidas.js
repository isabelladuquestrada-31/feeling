/* ==========================================================
   Solicitudes recibidas (RF-10, SCRUM-83)
   - Botón "Ver": abre y cierra el detalle de la solicitud.
   - Efecto de presión al tocar una solicitud en pantallas táctiles.
   - "Marcar como realizado" (RF-14, SCRUM-97): abre el formulario de
     duración y notas, cuenta los caracteres y muestra la carga al guardar.
   Aceptar, Rechazar (con confirmación), la salida de la tarjeta,
   el contador y las alertas los maneja solicitudes.js.
   ========================================================== */

document.addEventListener("DOMContentLoaded", () => {
    "use strict";

    const seccion = document.querySelector("[data-recibidas]");
    if (!seccion) {
        return;
    }

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
    seccion.querySelectorAll("[data-ver]").forEach(boton => {
        const detalle = document.getElementById(boton.getAttribute("aria-controls"));
        if (!detalle) {
            return;
        }

        boton.addEventListener("click", () => {
            const abrir = detalle.hidden;
            detalle.hidden = !abrir;
            boton.setAttribute("aria-expanded", String(abrir));
            boton.textContent = abrir ? "Ocultar" : "Ver";
            anunciar(abrir ? "Detalle de la solicitud abierto." : "Detalle de la solicitud cerrado.");
        });
    });

    // 2. "1 pendiente" o "N pendientes" cuando solicitudes.js cambia el contador.
    const contador = seccion.querySelector("[data-contador]");
    const textoContador = seccion.querySelector("[data-contador-texto]");
    if (contador && textoContador && "MutationObserver" in window) {
        new MutationObserver(() => {
            textoContador.textContent = contador.textContent.trim() === "1" ? "pendiente" : "pendientes";
        }).observe(contador, { childList: true, characterData: true, subtree: true });
    }

    // 3. "Marcar como realizado" abre y cierra el formulario de seguimiento (RF-14).
    seccion.querySelectorAll("[data-abrir-seguimiento]").forEach(boton => {
        const formulario = document.getElementById(boton.getAttribute("aria-controls"));
        if (!formulario) {
            return;
        }
        const notas = formulario.querySelector("[data-notas-seguimiento]");
        const caracteres = formulario.querySelector("[data-caracteres-notas]");
        const cancelar = formulario.querySelector("[data-cancelar-seguimiento]");
        const guardar = formulario.querySelector("[data-guardar-seguimiento]");

        function abrirFormulario(abrir) {
            formulario.hidden = !abrir;
            boton.hidden = abrir;
            boton.setAttribute("aria-expanded", String(abrir));
            if (abrir) {
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

        if (notas && caracteres) {
            notas.addEventListener("input", () => {
                caracteres.textContent = String(notas.value.length);
            });
        }

        // Muestra la carga y evita el doble envío.
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
