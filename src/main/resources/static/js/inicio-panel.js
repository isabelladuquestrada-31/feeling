/* ==========================================================
   Pantallas de inicio por rol (SCRUM-53, RF-02)
   - Onda y efecto de presión al tocar las tarjetas de acceso.
   - Las cifras del administrador cuentan desde 0 al aparecer.
   La entrada de las tarjetas, la onda de los botones y el
   cierre de alertas los maneja solicitudes.js.
   ========================================================== */

document.addEventListener("DOMContentLoaded", () => {
    "use strict";

    const panel = document.querySelector("[data-panel]");
    if (!panel) {
        return;
    }

    const reducirMovimiento = window.matchMedia("(prefers-reduced-motion: reduce)");

    /**
     * Dibuja una onda en el punto donde se tocó la tarjeta.
     *
     * @param {HTMLElement} tarjeta tarjeta tocada
     * @param {PointerEvent} evento evento del toque o clic
     */
    function crearOnda(tarjeta, evento) {
        if (reducirMovimiento.matches) {
            return;
        }
        const rect = tarjeta.getBoundingClientRect();
        const tamano = Math.max(rect.width, rect.height);
        const onda = document.createElement("span");

        onda.className = "onda";
        onda.style.width = `${tamano}px`;
        onda.style.height = `${tamano}px`;
        onda.style.left = `${evento.clientX - rect.left - tamano / 2}px`;
        onda.style.top = `${evento.clientY - rect.top - tamano / 2}px`;

        tarjeta.appendChild(onda);
        onda.addEventListener("animationend", () => onda.remove());
    }

    // 1. Tarjetas de acceso: onda al tocarlas y presión en pantallas táctiles.
    panel.querySelectorAll(".acceso").forEach(tarjeta => {
        tarjeta.addEventListener("pointerdown", evento => {
            crearOnda(tarjeta, evento);
            if (evento.pointerType === "touch") {
                tarjeta.classList.add("presionada");
            }
        });

        ["pointerup", "pointercancel", "pointerleave"].forEach(tipo => {
            tarjeta.addEventListener(tipo, () => tarjeta.classList.remove("presionada"));
        });
    });

    // 2. Cifras del administrador: cuentan desde 0 hasta su valor.
    const cifras = Array.from(panel.querySelectorAll("[data-contar]"));

    /**
     * Anima un número desde 0 hasta el valor que trae del servidor.
     *
     * @param {HTMLElement} elemento elemento con el número
     */
    function contar(elemento) {
        const final = parseInt(elemento.textContent, 10);
        if (Number.isNaN(final) || final <= 0 || reducirMovimiento.matches) {
            return;
        }
        // El lector de pantalla siempre lee el valor real, no el conteo.
        elemento.setAttribute("aria-label", String(final));
        const duracion = Math.min(1200, 400 + final * 60);
        const inicio = performance.now();

        function paso(ahora) {
            const avance = Math.min(1, (ahora - inicio) / duracion);
            const suave = 1 - Math.pow(1 - avance, 3);
            elemento.textContent = String(Math.round(final * suave));
            if (avance < 1) {
                requestAnimationFrame(paso);
            }
        }

        elemento.textContent = "0";
        requestAnimationFrame(paso);
    }

    if (cifras.length > 0) {
        if ("IntersectionObserver" in window) {
            const observador = new IntersectionObserver(entradas => {
                entradas.forEach(entrada => {
                    if (entrada.isIntersecting) {
                        contar(entrada.target);
                        observador.unobserve(entrada.target);
                    }
                });
            }, { threshold: 0.4 });
            cifras.forEach(cifra => observador.observe(cifra));
        } else {
            cifras.forEach(contar);
        }
    }
});
