/* ==========================================================
   Mi disponibilidad (RF-06, SCRUM-77)
   - Valida cada franja antes de enviarla, igual que el backend:
     día y horas completos, fin después del inicio y sin cruces
     con otra franja del mismo día.
   - Muestra la duración de la franja mientras se escriben las horas.
   - Abre y cierra la edición de una franja.
   - Pide un segundo toque para eliminar una franja.
   El cierre de alertas y la entrada de las tarjetas los maneja solicitudes.js.
   ========================================================== */

document.addEventListener("DOMContentLoaded", () => {
    "use strict";

    const seccion = document.querySelector("[data-disponibilidad]");
    if (!seccion) {
        return;
    }

    const reducirMovimiento = window.matchMedia("(prefers-reduced-motion: reduce)");
    const avisoLector = seccion.querySelector("[data-aviso]");
    const NOMBRES = {
        LUNES: "lunes", MARTES: "martes", MIERCOLES: "miércoles", JUEVES: "jueves",
        VIERNES: "viernes", SABADO: "sábado", DOMINGO: "domingo"
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
     * Convierte "HH:mm" en minutos desde la medianoche.
     *
     * @param {string} hora hora en formato HH:mm
     * @returns {number|null} minutos, o null si está vacía
     */
    function minutos(hora) {
        if (!hora) {
            return null;
        }
        const [h, m] = hora.split(":").map(Number);
        return h * 60 + m;
    }

    /**
     * Franjas que ya están en la lista, para revisar cruces.
     *
     * @returns {Array<{id: string, dia: string, inicio: number, fin: number}>}
     */
    function franjasRegistradas() {
        return Array.from(seccion.querySelectorAll(".franja[data-dia]")).map(fila => ({
            id: fila.dataset.id,
            dia: fila.dataset.dia,
            inicio: minutos(fila.dataset.inicio),
            fin: minutos(fila.dataset.fin)
        }));
    }

    /**
     * Revisa la franja de un formulario y devuelve el problema, o "" si está bien.
     *
     * @param {HTMLFormElement} formulario formulario de agregar o editar
     * @returns {string} mensaje de error
     */
    function problemaDe(formulario) {
        const diaMarcado = formulario.querySelector('input[name="dia"]:checked');
        const diaSelect = formulario.querySelector('select[name="dia"]');
        const dia = diaMarcado ? diaMarcado.value : (diaSelect ? diaSelect.value : "");
        const inicio = minutos(formulario.querySelector('[data-hora="inicio"]').value);
        const fin = minutos(formulario.querySelector('[data-hora="fin"]').value);

        if (!dia || inicio === null || fin === null) {
            return "Indica el día, la hora de inicio y la hora de fin.";
        }
        if (fin <= inicio) {
            return "La hora de fin debe ser posterior a la hora de inicio.";
        }
        const fila = formulario.closest(".franja");
        const propia = fila ? fila.dataset.id : null;
        const cruce = franjasRegistradas().some(otra =>
            otra.id !== propia && otra.dia === dia && otra.inicio < fin && inicio < otra.fin);
        if (cruce) {
            return `La franja se cruza con otra que ya registraste el ${NOMBRES[dia] || "mismo día"}.`;
        }
        return "";
    }

    /**
     * Muestra u oculta el error de un formulario.
     *
     * @param {HTMLFormElement} formulario formulario de la franja
     * @param {string} mensaje error a mostrar, o "" para quitarlo
     */
    function mostrarError(formulario, mensaje) {
        const error = formulario.querySelector("[data-error-franja]");
        if (error) {
            error.textContent = mensaje;
            error.hidden = mensaje === "";
        }
        formulario.querySelectorAll("[data-hora]").forEach(campo => {
            campo.classList.toggle("invalida", mensaje !== "" && mensaje.includes("hora"));
        });
        if (mensaje) {
            formulario.classList.remove("con-error");
            void formulario.offsetWidth;
            formulario.classList.add("con-error");
            anunciar(mensaje);
        }
    }

    /**
     * Escribe la duración de la franja ("2 h", "1 h 30 min") junto a las horas.
     *
     * @param {HTMLFormElement} formulario formulario de agregar
     */
    function actualizarDuracion(formulario) {
        const duracion = formulario.querySelector("[data-duracion]");
        if (!duracion) {
            return;
        }
        const inicio = minutos(formulario.querySelector('[data-hora="inicio"]').value);
        const fin = minutos(formulario.querySelector('[data-hora="fin"]').value);
        if (inicio === null || fin === null || fin <= inicio) {
            duracion.textContent = "";
            return;
        }
        const total = fin - inicio;
        const horas = Math.floor(total / 60);
        const resto = total % 60;
        duracion.textContent = [horas ? `${horas} h` : "", resto ? `${resto} min` : ""].filter(Boolean).join(" ");
    }

    // 1. Validación de agregar y editar.
    seccion.querySelectorAll("form[data-formulario-franja]").forEach(formulario => {
        formulario.addEventListener("input", () => {
            actualizarDuracion(formulario);
            if (!formulario.querySelector("[data-error-franja]").hidden) {
                mostrarError(formulario, problemaDe(formulario));
            }
        });

        formulario.addEventListener("submit", evento => {
            const problema = problemaDe(formulario);
            if (problema) {
                evento.preventDefault();
                mostrarError(formulario, problema);
                return;
            }
            const boton = formulario.querySelector('button[type="submit"]');
            if (boton) {
                boton.disabled = true;
                boton.innerHTML = '<span class="boton-accion__cargando" aria-hidden="true"></span>Guardando...';
            }
            anunciar("Guardando la franja.");
        });

        actualizarDuracion(formulario);
    });

    // 2. Abrir y cerrar la edición de una franja.
    seccion.querySelectorAll("[data-editar]").forEach(boton => {
        const edicion = document.getElementById(boton.getAttribute("aria-controls"));
        if (!edicion) {
            return;
        }
        boton.addEventListener("click", () => {
            const abrir = edicion.hidden;
            edicion.hidden = !abrir;
            boton.setAttribute("aria-expanded", String(abrir));
            if (abrir) {
                const primero = edicion.querySelector("select, input");
                if (primero) {
                    primero.focus();
                }
            }
        });
        const cancelar = edicion.querySelector("[data-cancelar-edicion]");
        if (cancelar) {
            cancelar.addEventListener("click", () => {
                edicion.hidden = true;
                boton.setAttribute("aria-expanded", "false");
                mostrarError(edicion, "");
                boton.focus();
            });
        }
    });

    // 3. Eliminar con un segundo toque para confirmar.
    seccion.querySelectorAll("form[data-eliminar]").forEach(formulario => {
        const boton = formulario.querySelector("button");
        let confirmando = false;
        let temporizador;

        formulario.addEventListener("submit", evento => {
            evento.preventDefault();
            if (!confirmando) {
                confirmando = true;
                boton.classList.add("confirmando");
                anunciar("Toca otra vez para eliminar la franja.");
                temporizador = setTimeout(() => {
                    confirmando = false;
                    boton.classList.remove("confirmando");
                }, 3500);
                return;
            }
            clearTimeout(temporizador);
            boton.disabled = true;
            const fila = formulario.closest(".franja");
            if (fila && !reducirMovimiento.matches) {
                fila.classList.add("saliendo");
            }
            anunciar("Eliminando la franja.");
            setTimeout(() => formulario.submit(), reducirMovimiento.matches ? 0 : 350);
        });
    });

    // 4. Si se acaba de agregar o editar una franja, se lleva a la vista.
    const nueva = seccion.querySelector(".franja--nueva");
    if (nueva) {
        nueva.scrollIntoView({ behavior: reducirMovimiento.matches ? "auto" : "smooth", block: "center" });
    }
});
