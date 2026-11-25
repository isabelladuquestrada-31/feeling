/*
 * PROYECTO: Feeling
 * ARCHIVO: disponibilidad.js
 * UBICACIÓN: src/main/resources/static/js/disponibilidad.js
 * MÓDULO: Mi disponibilidad (panel del voluntario)
 *
 * AUTOR: Yulipaola Domínguez
 * VERSIÓN: 1.1.0
 * FECHA DE DOCUMENTACIÓN: 2026-11-25
 * ESTADO: Finalizado
 * TICKET: SCRUM-77 (RF-06)
 *
 * DESCRIPCIÓN:
 * Script de la pantalla disponibilidad.html. Revisa cada franja antes de
 * enviarla con las mismas reglas que el servidor, muestra la duración
 * mientras se eligen las horas, abre y cierra la edición de una franja y
 * pide un segundo toque para eliminarla.
 *
 * FUNCIONALIDADES:
 * - Validación al agregar y al editar: día y horas completos, hora de fin
 *   posterior a la de inicio y sin cruces con otra franja del mismo día.
 * - Duración de la franja ("2 h", "1 h 30 min") junto a las horas.
 * - Botón Editar: abre y cierra el formulario de edición de la franja.
 * - Botón Eliminar: el primer toque pide confirmación durante 3,5 segundos
 *   y el segundo elimina la franja con una animación de salida.
 * - Desplazamiento hasta la franja recién agregada o editada.
 * - Avisos para lectores de pantalla.
 *
 * LO QUE NO HACE ESTE ARCHIVO:
 * El cierre de las alertas y la entrada de las tarjetas los maneja
 * solicitudes.js. El servidor vuelve a validar las franjas al guardarlas.
 *
 * DEPENDENCIAS:
 * - disponibilidad.html: aporta los elementos con atributos data-*.
 * - solicitudes.js: alertas y entrada de tarjetas.
 *
 * ATRIBUTOS DATA-* UTILIZADOS:
 * - data-disponibilidad: sección principal; si no existe, el script no hace nada.
 * - data-aviso: zona para lectores de pantalla.
 * - data-formulario-franja: formularios de agregar y editar una franja.
 * - data-hora="inicio" y data-hora="fin": campos de hora.
 * - data-duracion y data-error-franja: duración y mensaje de error.
 * - data-dia, data-id, data-inicio y data-fin: datos de cada franja guardada.
 * - data-editar y data-cancelar-edicion: botones de la edición.
 * - data-eliminar: formulario para eliminar una franja.
 *
 * HISTORIAL DE VERSIONES:
 * - 1.0.0: validación, edición y eliminación de franjas (SCRUM-77, RF-06).
 * - 1.1.0 (2026-11-25): documentación completa. No cambia la lógica.
 */


// El script se ejecuta cuando el documento terminó de cargarse.
document.addEventListener("DOMContentLoaded", () => {
    "use strict";

    // Sección principal de la pantalla. Si no existe, no se hace nada.
    const seccion = document.querySelector("[data-disponibilidad]");
    if (!seccion) {
        return;
    }

    // Preferencia del usuario de reducir el movimiento.
    const reducirMovimiento = window.matchMedia("(prefers-reduced-motion: reduce)");
    // Zona de avisos para lectores de pantalla.
    const avisoLector = seccion.querySelector("[data-aviso]");
    // Nombre de cada día para los mensajes de error.
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
        // El día puede venir de los botones de días (agregar) o de una lista (editar).
        const diaMarcado = formulario.querySelector('input[name="dia"]:checked');
        const diaSelect = formulario.querySelector('select[name="dia"]');
        const dia = diaMarcado ? diaMarcado.value : (diaSelect ? diaSelect.value : "");
        const inicio = minutos(formulario.querySelector('[data-hora="inicio"]').value);
        const fin = minutos(formulario.querySelector('[data-hora="fin"]').value);

        // Regla 1: el día y las dos horas son obligatorios.
        if (!dia || inicio === null || fin === null) {
            return "Indica el día, la hora de inicio y la hora de fin.";
        }
        // Regla 2: la hora de fin debe ser posterior a la de inicio.
        if (fin <= inicio) {
            return "La hora de fin debe ser posterior a la hora de inicio.";
        }
        // Regla 3: no puede cruzarse con otra franja del mismo día. Al editar,
        // la franja propia no se compara consigo misma.
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
        // Los campos de hora se marcan en rojo si el error es de las horas.
        formulario.querySelectorAll("[data-hora]").forEach(campo => {
            campo.classList.toggle("invalida", mensaje !== "" && mensaje.includes("hora"));
        });
        // Se reinicia la animación de sacudida para que se vea en cada error.
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
        // Se separa el total en horas y minutos.
        const total = fin - inicio;
        const horas = Math.floor(total / 60);
        const resto = total % 60;
        duracion.textContent = [horas ? `${horas} h` : "", resto ? `${resto} min` : ""].filter(Boolean).join(" ");
    }

    // 1. Validación de agregar y editar.
    seccion.querySelectorAll("form[data-formulario-franja]").forEach(formulario => {
        // Al cambiar un campo se actualiza la duración y, si ya había un error,
        // se vuelve a revisar.
        formulario.addEventListener("input", () => {
            actualizarDuracion(formulario);
            if (!formulario.querySelector("[data-error-franja]").hidden) {
                mostrarError(formulario, problemaDe(formulario));
            }
        });

        // Antes de enviar se revisa la franja; si hay un problema, se cancela el envío.
        formulario.addEventListener("submit", evento => {
            const problema = problemaDe(formulario);
            if (problema) {
                evento.preventDefault();
                mostrarError(formulario, problema);
                return;
            }
            // Si todo está bien, el botón muestra que se está guardando y se desactiva.
            const boton = formulario.querySelector('button[type="submit"]');
            if (boton) {
                boton.disabled = true;
                boton.innerHTML = '<span class="boton-accion__cargando" aria-hidden="true"></span>Guardando...';
            }
            anunciar("Guardando la franja.");
        });

        // Se calcula la duración al cargar, por si el formulario ya trae horas.
        actualizarDuracion(formulario);
    });

    // 2. Abrir y cerrar la edición de una franja.
    seccion.querySelectorAll("[data-editar]").forEach(boton => {
        // El formulario de edición se identifica con aria-controls del botón.
        const edicion = document.getElementById(boton.getAttribute("aria-controls"));
        if (!edicion) {
            return;
        }
        // El botón Editar abre o cierra la edición y lleva el foco al primer campo.
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
        // Cancelar cierra la edición, borra el error y devuelve el foco al botón.
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

        // El primer toque solo pide confirmación; si no se confirma en 3,5 segundos,
        // el botón vuelve a su estado normal.
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
            // El segundo toque elimina: la fila sale con una animación y luego se envía.
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
