/* ==========================================================
   Solicitudes recibidas (RF-10, SCRUM-83)
   - Botón "Ver": abre y cierra el detalle de la solicitud.
   - Efecto de presión al tocar una solicitud en pantallas táctiles.
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

    // 3. Efecto de presión al tocar una solicitud (solo pantallas táctiles).
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
