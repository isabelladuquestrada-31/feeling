/* PROYECTO: Feeling
   ARCHIVO: inicio-panel.js
   UBICACIÓN: src/main/resources/static/js/inicio-panel.js
   MÓDULO: Pantallas de inicio por rol (RF-02)

   AUTOR: Yulipaola Domínguez
   VERSIÓN: 1.1.0
   FECHA DE DOCUMENTACIÓN: 2026-11-22
   ESTADO: Finalizado
   TICKET: SCRUM-53

   DESCRIPCIÓN:
   Script de las pantallas de inicio por rol (estudiante y
   administrador). Agrega dos comportamientos:
   - Onda y efecto de presión al tocar las tarjetas de acceso.
   - Las cifras del administrador cuentan desde 0 al aparecer.
   La entrada de las tarjetas, la onda de los botones y el cierre de
   alertas los maneja solicitudes.js.

   VARIABLES Y FUNCIONES:
   - panel: elemento raíz de la pantalla (atributo data-panel).
   - reducirMovimiento: consulta de medios prefers-reduced-motion.
   - crearOnda(tarjeta, evento): dibuja la onda en el punto tocado.
   - cifras: elementos con el atributo data-contar.
   - contar(elemento): anima un número desde 0 hasta su valor.
   - observador: IntersectionObserver que inicia el conteo cuando la
     cifra aparece en pantalla.

   ELEMENTOS DEL HTML QUE UTILIZA:
   - [data-panel]: sección raíz; si no existe, el script no hace nada.
   - .acceso: tarjetas de acceso rápido.
   - [data-contar]: cifras del administrador.

   CLASES CSS QUE AGREGA:
   - presionada: efecto de presión en pantallas táctiles.
   - onda: elemento temporal de la onda (estilos y animación en
     solicitudes.css y inicio-panel.css).

   DEPENDENCIAS:
   - inicio-panel.css: efecto de la clase presionada.
   - solicitudes.css: animación de la clase onda.
   - solicitudes.js: se carga junto con este script.

   ACCESIBILIDAD:
   Si el usuario prefiere menos movimiento, no se dibuja la onda ni se
   anima el conteo.

   HISTORIAL DE VERSIONES:
   - 1.0.0 (2026-11-16): implementación inicial (SCRUM-53, RF-02).
   - 1.1.0 (2026-11-22): documentación completa del archivo. No se
     modificó el código.

   OBSERVACIONES:
   - La pantalla del estudiante no tiene elementos con data-contar;
     el conteo solo se activa en la pantalla del administrador.
   - La onda se elimina cuando termina su animación. Si la clase onda
     no tuviera animación en el CSS, el elemento quedaría en la
     tarjeta, por eso depende de solicitudes.css.
   - El aria-label del conteo se coloca en un span sin rol, y algunos
     lectores de pantalla no lo anuncian. Una opción es agregar
     role="img" al elemento en el HTML del administrador.
   */

document.addEventListener("DOMContentLoaded", () => {
    "use strict";

    /* Elemento raíz de la pantalla. Si la página no es una pantalla de
       inicio por rol, el script termina sin hacer nada. */

    const panel = document.querySelector("[data-panel]");
    if (!panel) {
        return;
    }

    /* Consulta de medios que indica si el usuario pidió reducir el
       movimiento en su sistema. */

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

    /* 1. TARJETAS DE ACCESO

       Al tocar una tarjeta se dibuja la onda y, si el toque es táctil,
       se agrega la clase presionada. La clase se quita al soltar,
       cancelar el toque o salir de la tarjeta. */

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

    /* 2. CIFRAS DEL ADMINISTRADOR

       Los elementos con data-contar cuentan desde 0 hasta su valor. */

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

    /* El conteo empieza cuando la cifra se ve al menos en un 40 %. Si el
       navegador no soporta IntersectionObserver, cuenta de inmediato. */

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