/* ==========================================================
   Solicitar apoyo (RF-08, SCRUM-81)
   - Marca cada paso como listo y llena el resumen mientras se elige.
   - Cuenta los caracteres del tema.
   - Valida que todo esté completo antes de enviar, igual que el backend,
     para avisar sin recargar la página.
   - Botón "volver" que regresa a la página anterior.
   La onda de los botones y el cierre de alertas los maneja solicitudes.js.
   ========================================================== */

document.addEventListener("DOMContentLoaded", () => {
    "use strict";

    const seccion = document.querySelector("[data-solicitar]");
    const formulario = seccion ? seccion.querySelector("[data-formulario-solicitud]") : null;
    if (!formulario) {
        return;
    }

    const reducirMovimiento = window.matchMedia("(prefers-reduced-motion: reduce)");
    const avisoLector = seccion.querySelector("[data-aviso]");
    const campoTema = formulario.querySelector("[data-campo-tema]");
    const contadorTema = seccion.querySelector("[data-contador-tema]");
    const caracteres = seccion.querySelector("[data-caracteres]");
    const progreso = seccion.querySelector("[data-progreso]");
    const pasosListos = seccion.querySelector("[data-pasos-listos]");
    const botonEnviar = formulario.querySelector("[data-enviar]");
    const textoEnviar = formulario.querySelector("[data-enviar-texto]");

    const PASOS = ["materia", "tema", "tipo", "horario", "modalidad"];
    const TEXTOS_VACIOS = {
        materia: "Sin elegir",
        tema: "Sin escribir",
        tipo: "Sin elegir",
        horario: "Sin elegir",
        modalidad: "Sin elegir"
    };

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

    /**
     * Devuelve el texto que el estudiante eligió o escribió en un paso, o "" si está vacío.
     *
     * @param {string} paso nombre del paso
     * @returns {string} texto del valor elegido
     */
    function valorDe(paso) {
        if (paso === "tema") {
            return campoTema ? campoTema.value.trim() : "";
        }
        const elegido = formulario.querySelector(`input[name="${paso}"]:checked`);
        return elegido ? (elegido.dataset.texto || elegido.value) : "";
    }

    /**
     * Actualiza un paso: lo marca como listo, quita el error y llena el resumen.
     *
     * @param {string} paso nombre del paso
     */
    function actualizarPaso(paso) {
        const contenedor = formulario.querySelector(`[data-paso="${paso}"]`);
        const valor = valorDe(paso);
        const resumen = seccion.querySelector(`[data-resumen="${paso}"]`);

        if (contenedor) {
            contenedor.classList.toggle("completo", valor !== "");
            if (valor !== "") {
                quitarError(paso);
            }
        }

        if (resumen) {
            const nuevoTexto = valor || TEXTOS_VACIOS[paso];
            resumen.classList.toggle("pendiente", valor === "");
            if (resumen.textContent !== nuevoTexto) {
                resumen.textContent = nuevoTexto;
                resumen.classList.remove("actualizado");
                void resumen.offsetWidth;
                resumen.classList.add("actualizado");
            }
        }

        // Marca la opción elegida para navegadores sin :has().
        formulario.querySelectorAll(`input[name="${paso}"]`).forEach(entrada => {
            entrada.closest("label").classList.toggle("elegida", entrada.checked);
        });
    }

    /**
     * Actualiza la barra de progreso del resumen.
     */
    function actualizarProgreso() {
        const listos = PASOS.filter(paso => valorDe(paso) !== "").length;
        if (progreso) {
            progreso.style.width = `${(listos / PASOS.length) * 100}%`;
        }
        if (pasosListos) {
            pasosListos.textContent = String(listos);
        }
    }

    /**
     * Muestra el error de un paso.
     *
     * @param {string} paso nombre del paso
     */
    function mostrarError(paso) {
        const contenedor = formulario.querySelector(`[data-paso="${paso}"]`);
        const error = formulario.querySelector(`[data-error="${paso}"]`);
        if (contenedor) {
            contenedor.classList.remove("con-error");
            void contenedor.offsetWidth;
            contenedor.classList.add("con-error");
        }
        if (error) {
            error.hidden = false;
        }
    }

    /**
     * Quita el error de un paso.
     *
     * @param {string} paso nombre del paso
     */
    function quitarError(paso) {
        const contenedor = formulario.querySelector(`[data-paso="${paso}"]`);
        const error = formulario.querySelector(`[data-error="${paso}"]`);
        if (contenedor) {
            contenedor.classList.remove("con-error");
        }
        if (error) {
            error.hidden = true;
        }
    }

    /**
     * Actualiza el contador de caracteres del tema.
     */
    function actualizarContadorTema() {
        if (!campoTema || !caracteres) {
            return;
        }
        const largo = campoTema.value.length;
        const maximo = parseInt(campoTema.getAttribute("maxlength"), 10) || 150;
        caracteres.textContent = String(largo);
        if (contadorTema) {
            contadorTema.classList.toggle("cerca", largo >= maximo - 20);
        }
    }

    // 1. Al elegir una opción: animación, paso listo y resumen.
    formulario.querySelectorAll('input[type="radio"]').forEach(entrada => {
        entrada.addEventListener("change", () => {
            const etiqueta = entrada.closest("label");
            if (etiqueta && !reducirMovimiento.matches) {
                etiqueta.classList.remove("pop");
                void etiqueta.offsetWidth;
                etiqueta.classList.add("pop");
            }
            actualizarPaso(entrada.name);
            actualizarProgreso();
            anunciar(`Elegiste: ${entrada.dataset.texto || entrada.value}.`);
        });
    });

    // 2. Tema: contador, paso listo y resumen mientras se escribe.
    if (campoTema) {
        campoTema.addEventListener("input", () => {
            actualizarContadorTema();
            actualizarPaso("tema");
            actualizarProgreso();
        });

        // Enter en el tema no envía el formulario antes de tiempo.
        campoTema.addEventListener("keydown", evento => {
            if (evento.key === "Enter") {
                evento.preventDefault();
            }
        });
    }

    // 3. Antes de enviar: revisa que todo esté completo.
    formulario.addEventListener("submit", evento => {
        const faltantes = PASOS.filter(paso => valorDe(paso) === "");

        if (faltantes.length > 0) {
            evento.preventDefault();
            faltantes.forEach(mostrarError);

            const primero = formulario.querySelector(`[data-paso="${faltantes[0]}"]`);
            if (primero) {
                primero.scrollIntoView({ behavior: reducirMovimiento.matches ? "auto" : "smooth", block: "center" });
                const foco = primero.querySelector("input:not([type=hidden])");
                if (foco) {
                    setTimeout(() => foco.focus({ preventScroll: true }), 300);
                }
            }
            anunciar(faltantes.length === 1
                ? "Falta completar un paso."
                : `Faltan completar ${faltantes.length} pasos.`);
            return;
        }

        // Todo listo: muestra que se está enviando y evita el doble envío.
        if (botonEnviar) {
            botonEnviar.disabled = true;
            botonEnviar.setAttribute("aria-busy", "true");
        }
        if (textoEnviar) {
            textoEnviar.innerHTML = '<span class="boton-accion__cargando" aria-hidden="true"></span>Enviando solicitud...';
        }
        anunciar("Enviando tu solicitud.");
    });

    // 4. Volver a la página anterior si viene de Feeling; si no, a la búsqueda.
    const volver = seccion.querySelector("[data-volver]");
    if (volver) {
        volver.addEventListener("click", evento => {
            if (document.referrer && new URL(document.referrer).origin === window.location.origin
                    && window.history.length > 1) {
                evento.preventDefault();
                window.history.back();
            }
        });
    }

    // 5. Estado inicial (por si el servidor devolvió el formulario con datos).
    PASOS.forEach(actualizarPaso);
    actualizarProgreso();
    actualizarContadorTema();
});
