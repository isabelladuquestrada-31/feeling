/* ==========================================================
   Solicitar apoyo (RF-08, SCRUM-81)
   - Marca cada paso como listo y llena el resumen mientras se elige.
   - Cuenta los caracteres del tema.
   - Valida que todo esté completo antes de enviar, igual que el backend,
     para avisar sin recargar la página.
   - Ajustes razonables (RF-09, SCRUM-95): muestra el campo para
     describir el ajuste cuando se marca "Otro" y no deja enviar si
     queda vacío.
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

    // 3. Ajustes razonables (RF-09): paso opcional con casillas.
    const casillasAjuste = Array.from(formulario.querySelectorAll("[data-ajuste]"));
    const campoOtro = formulario.querySelector("[data-campo-otro]");
    const detalleOtro = formulario.querySelector("[data-detalle-otro]");
    const caracteresAjuste = seccion.querySelector("[data-caracteres-ajuste]");
    const contadorAjuste = seccion.querySelector("[data-contador-ajuste]");

    /**
     * Indica si la casilla "Otro" está marcada.
     *
     * @returns {boolean} true si eligió "Otro"
     */
    function otroMarcado() {
        const otro = casillasAjuste.find(casilla => casilla.value === "OTRO");
        return Boolean(otro && otro.checked);
    }

    /**
     * Caso de fallo de RF-09: marcó "Otro" y no describió el ajuste.
     *
     * @returns {boolean} true si falta la descripción
     */
    function otroSinDetalle() {
        return otroMarcado() && (!detalleOtro || detalleOtro.value.trim() === "");
    }

    /**
     * Actualiza el contador de caracteres de la descripción del ajuste.
     */
    function actualizarContadorAjuste() {
        if (!detalleOtro || !caracteresAjuste) {
            return;
        }
        const largo = detalleOtro.value.length;
        caracteresAjuste.textContent = String(largo);
        if (contadorAjuste) {
            contadorAjuste.classList.toggle("cerca", largo >= 235);
        }
    }

    /**
     * Marca las casillas elegidas, muestra u oculta la descripción de "Otro"
     * y llena el resumen con los ajustes.
     */
    function actualizarAjustes() {
        casillasAjuste.forEach(casilla => {
            casilla.closest("label").classList.toggle("elegida", casilla.checked);
        });

        if (campoOtro) {
            const mostrar = otroMarcado();
            if (campoOtro.hidden === mostrar) {
                campoOtro.hidden = !mostrar;
                if (mostrar && !reducirMovimiento.matches) {
                    campoOtro.classList.remove("abriendo");
                    void campoOtro.offsetWidth;
                    campoOtro.classList.add("abriendo");
                }
            }
        }

        const elegidos = casillasAjuste
            .filter(casilla => casilla.checked)
            .map(casilla => {
                const detalle = detalleOtro ? detalleOtro.value.trim() : "";
                return casilla.value === "OTRO" && detalle !== ""
                    ? `Otro: ${detalle}`
                    : (casilla.dataset.texto || casilla.value);
            });

        const paso = formulario.querySelector('[data-paso="ajustes"]');
        if (paso) {
            paso.classList.toggle("completo", elegidos.length > 0 && !otroSinDetalle());
        }
        if (!otroSinDetalle()) {
            quitarError("ajustes");
        }

        const resumen = seccion.querySelector('[data-resumen="ajustes"]');
        if (resumen) {
            const texto = elegidos.length > 0 ? elegidos.join(", ") : "Ninguno";
            if (resumen.textContent !== texto) {
                resumen.textContent = texto;
                resumen.classList.remove("actualizado");
                void resumen.offsetWidth;
                resumen.classList.add("actualizado");
            }
        }
    }

    casillasAjuste.forEach(casilla => {
        casilla.addEventListener("change", () => {
            const etiqueta = casilla.closest("label");
            if (etiqueta && !reducirMovimiento.matches) {
                etiqueta.classList.remove("pop");
                void etiqueta.offsetWidth;
                etiqueta.classList.add("pop");
            }
            actualizarAjustes();
            const texto = casilla.dataset.texto || casilla.value;
            anunciar(casilla.checked ? `Marcaste: ${texto}.` : `Quitaste: ${texto}.`);

            // Al marcar "Otro", el cursor pasa al campo para describirlo.
            if (casilla.value === "OTRO" && casilla.checked && detalleOtro) {
                setTimeout(() => detalleOtro.focus(), 150);
            }
        });
    });

    if (detalleOtro) {
        detalleOtro.addEventListener("input", () => {
            actualizarContadorAjuste();
            actualizarAjustes();
        });
    }

    // 4. Antes de enviar: revisa que todo esté completo.
    formulario.addEventListener("submit", evento => {
        const faltantes = PASOS.filter(paso => valorDe(paso) === "");
        if (otroSinDetalle()) {
            faltantes.push("ajustes");
        }

        if (faltantes.length > 0) {
            evento.preventDefault();
            faltantes.forEach(mostrarError);

            const primerPaso = faltantes[0];
            const primero = formulario.querySelector(`[data-paso="${primerPaso}"]`);
            if (primero) {
                primero.scrollIntoView({ behavior: reducirMovimiento.matches ? "auto" : "smooth", block: "center" });
                const foco = primerPaso === "ajustes"
                    ? detalleOtro
                    : primero.querySelector("input:not([type=hidden])");
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

    // 5. Volver a la página anterior si viene de Feeling; si no, a la búsqueda.
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

    // 6. Estado inicial (por si el servidor devolvió el formulario con datos).
    PASOS.forEach(actualizarPaso);
    actualizarProgreso();
    actualizarContadorTema();
    actualizarContadorAjuste();
    actualizarAjustes();
});
