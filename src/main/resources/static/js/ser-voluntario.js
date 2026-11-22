/*
   PROYECTO: Feeling
   ARCHIVO: ser-voluntario.js
   UBICACIÓN: src/main/resources/static/js/ser-voluntario.js
   MÓDULO: Ser voluntario (panel del estudiante)

   AUTOR: Yulipaola Domínguez
   VERSIÓN: 1.1.0
   FECHA DE DOCUMENTACIÓN: 2026-11-22
   ESTADO: Finalizado
   TICKET: SCRUM-75 (RF-05)

   DESCRIPCIÓN:
   Comportamiento de la pantalla "Activarme como voluntario".
   - Cuenta las materias y los tipos de apoyo elegidos.
   - Filtra las materias mientras se escribe (sin importar tildes).
   - Valida que haya al menos una opción en cada paso antes de enviar,
     igual que el backend, para avisar sin recargar la página.
   - Botón "volver" que regresa a la página anterior.
   Los mensajes de error y éxito del servidor, la onda de los botones
   y el cierre de alertas los maneja solicitudes.js.

   ATRIBUTOS DE INTERACCIÓN (definidos en ser-voluntario.html):
   - data-aviso: región que anuncia mensajes a lectores de pantalla.
   - data-volver: botón para volver.
   - data-formulario-voluntario: formulario de activación.
   - data-paso, data-contador y data-error: elementos de cada paso
     (materias y tipos).
   - data-filtro-materias y data-sin-materias: buscador de materias
     y mensaje de "sin resultados".
   - data-enviar y data-enviar-texto: botón de envío y su texto.
   - data-texto (en cada opción): texto sobre el que se filtra.

   CLASES CSS QUE AGREGA O QUITA (definidas en ser-voluntario.css):
   - elegida, recien-elegida, completo, con-error, cambio.
   - boton-accion__cargando (elemento que se crea al enviar).

   VARIABLES PRINCIPALES:
   - reducirMovimiento: preferencia del sistema de reducir animaciones.
   - avisoLector: región de avisos para lectores de pantalla.
   - volver: botón de regreso.
   - formulario: formulario de activación (nulo si ya es voluntario).
   - pasos: datos de los dos pasos (materias y tipos).
   - filtro, sinMaterias: buscador de materias y su mensaje vacío.
   - botonEnviar, textoEnviar: botón de envío y su texto.
   - enviando: indica que el formulario ya se envió (evita doble envío).

   FUNCIONES:
   - anunciar(mensaje): anuncia un texto a lectores de pantalla.
   - normalizar(texto): quita tildes y pasa a minúsculas.
   - actualizarPaso(datos, animar): actualiza contador, check y estilos.

   HISTORIAL DE VERSIONES:
   - 1.0.0: implementación inicial (SCRUM-75, RF-05).
   - 1.1.0 (2026-11-22): documentación completa. No cambia la lógica.

   OBSERVACIONES:
   - El filtro usa la propiedad dataset.texto de cada opción, que el
     HTML arma con el nombre y el código de la materia.
   - normalizar() usa un rango de caracteres combinados (tildes)
     escrito con los propios caracteres. Se puede reemplazar por
     /[\u0300-\u036f]/g, que es equivalente y más fácil de leer.
  */

document.addEventListener("DOMContentLoaded", () => {
    "use strict";

    // Preferencia del sistema: si pide menos movimiento, se omiten las animaciones.
    const reducirMovimiento = window.matchMedia("(prefers-reduced-motion: reduce)");
    // Región oculta que lee el lector de pantalla.
    const avisoLector = document.querySelector("[data-aviso]");

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
     * Quita tildes, pasa a minúsculas y deja un solo espacio entre palabras.
     *
     * @param {string} texto texto a comparar
     * @returns {string} texto normalizado
     */
    function normalizar(texto) {
        return (texto || "")
            .normalize("NFD")
            .replace(/[̀-ͯ]/g, "")
            .toLowerCase()
            .replace(/\s+/g, " ")
            .trim();
    }

    // 1. Botón volver: regresa a la página anterior si venimos de Feeling.
    // Si no hay historial propio, se deja el enlace normal (hacia /voluntarios).
    const volver = document.querySelector("[data-volver]");
    if (volver) {
        volver.addEventListener("click", evento => {
            const vieneDeFeeling = document.referrer.startsWith(window.location.origin);
            if (vieneDeFeeling && window.history.length > 1) {
                evento.preventDefault();
                window.history.back();
            }
        });
    }

    const formulario = document.querySelector("[data-formulario-voluntario]");
    if (!formulario) {
        // El estudiante ya es voluntario: solo se muestra su perfil.
        return;
    }

    // Datos de cada paso: contenedor, contador, mensaje de error, casillas
    // y las palabras del contador en singular y plural.
    const pasos = {
        materias: {
            paso: formulario.querySelector('[data-paso="materias"]'),
            contador: formulario.querySelector('[data-contador="materias"]'),
            error: formulario.querySelector('[data-error="materias"]'),
            casillas: Array.from(formulario.querySelectorAll('input[name="materias"]')),
            singular: "seleccionada",
            plural: "seleccionadas"
        },
        tipos: {
            paso: formulario.querySelector('[data-paso="tipos"]'),
            contador: formulario.querySelector('[data-contador="tipos"]'),
            error: formulario.querySelector('[data-error="tipos"]'),
            casillas: Array.from(formulario.querySelectorAll('input[name="tiposApoyo"]')),
            singular: "seleccionado",
            plural: "seleccionados"
        }
    };

    /**
     * Actualiza el contador, el check del paso y el estilo de las opciones.
     *
     * @param {object} datos paso a actualizar
     * @param {boolean} animar si el contador hace una pequeña animación
     * @returns {number} cuántas opciones hay elegidas
     */
    function actualizarPaso(datos, animar) {
        const elegidas = datos.casillas.filter(casilla => casilla.checked).length;

        // Marca visualmente las opciones elegidas.
        datos.casillas.forEach(casilla => {
            casilla.closest(".opcion").classList.toggle("elegida", casilla.checked);
        });

        // Texto del contador y estado "completo" del paso.
        datos.contador.textContent = `${elegidas} ${elegidas === 1 ? datos.singular : datos.plural}`;
        datos.paso.classList.toggle("completo", elegidas > 0);

        // Si ya hay una opción, desaparece el error del paso.
        if (elegidas > 0) {
            datos.error.hidden = true;
            datos.paso.classList.remove("con-error");
        }

        // Pequeño aumento del contador (se omite con movimiento reducido).
        if (animar && !reducirMovimiento.matches) {
            datos.contador.classList.add("cambio");
            setTimeout(() => datos.contador.classList.remove("cambio"), 250);
        }

        return elegidas;
    }

    // 2. Cada casilla actualiza su paso y hace una pequeña animación al elegirse.
    Object.values(pasos).forEach(datos => {
        datos.casillas.forEach(casilla => {
            casilla.addEventListener("change", () => {
                const opcion = casilla.closest(".opcion");
                if (casilla.checked && !reducirMovimiento.matches) {
                    // Se quita y se vuelve a poner la clase para reiniciar la animación.
                    opcion.classList.remove("recien-elegida");
                    void opcion.offsetWidth;
                    opcion.classList.add("recien-elegida");
                }
                const elegidas = actualizarPaso(datos, true);
                anunciar(`${elegidas} ${elegidas === 1 ? datos.singular : datos.plural}.`);
            });
        });

        // Estado inicial (por ejemplo, cuando el servidor devuelve el formulario con un error).
        actualizarPaso(datos, false);
    });

    // 3. Buscador de materias: filtra las tarjetas mientras se escribe.
    const filtro = formulario.querySelector("[data-filtro-materias]");
    const sinMaterias = formulario.querySelector("[data-sin-materias]");

    if (filtro) {
        filtro.addEventListener("input", () => {
            // Cada palabra escrita debe aparecer en el texto de la materia.
            const terminos = normalizar(filtro.value).split(" ").filter(Boolean);
            let visibles = 0;

            pasos.materias.casillas.forEach(casilla => {
                const opcion = casilla.closest(".opcion");
                const texto = normalizar(opcion.dataset.texto);
                const coincide = terminos.every(termino => texto.includes(termino));
                // Las materias ya elegidas siempre se ven, para no perderlas de vista.
                opcion.hidden = !coincide && !casilla.checked;
                if (!opcion.hidden) {
                    visibles++;
                }
            });

            // Muestra el mensaje de "sin resultados" si no queda ninguna.
            sinMaterias.hidden = visibles > 0;
        });

        // Enter en el buscador no envía el formulario.
        filtro.addEventListener("keydown", evento => {
            if (evento.key === "Enter") {
                evento.preventDefault();
            }
        });
    }

    // 4. Validación antes de enviar: al menos una materia y un tipo de apoyo.
    const botonEnviar = formulario.querySelector("[data-enviar]");
    const textoEnviar = formulario.querySelector("[data-enviar-texto]");
    let enviando = false;

    formulario.addEventListener("submit", evento => {
        // Evita un segundo envío mientras el primero está en curso.
        if (enviando) {
            evento.preventDefault();
            return;
        }

        // Pasos que no tienen ninguna opción elegida.
        const faltantes = Object.values(pasos).filter(datos => actualizarPaso(datos, false) === 0);

        if (faltantes.length > 0) {
            evento.preventDefault();

            // Muestra el error y reinicia la animación de sacudida.
            faltantes.forEach(datos => {
                datos.error.hidden = false;
                datos.paso.classList.remove("con-error");
                void datos.paso.offsetWidth;
                datos.paso.classList.add("con-error");
            });

            // Lleva la vista y el foco al primer paso con error.
            const primero = faltantes[0];
            primero.paso.scrollIntoView({
                behavior: reducirMovimiento.matches ? "auto" : "smooth",
                block: "start"
            });
            const casillaVisible = primero.casillas.find(casilla => !casilla.closest(".opcion").hidden);
            if (casillaVisible) {
                casillaVisible.focus({ preventScroll: true });
            }

            anunciar(faltantes.map(datos => datos.error.textContent.trim()).join(" "));
            return;
        }

        // Todo listo: muestra que se está enviando y evita doble envío.
        enviando = true;
        botonEnviar.disabled = true;
        botonEnviar.setAttribute("aria-busy", "true");
        textoEnviar.textContent = "Activando tu perfil...";
        const cargando = document.createElement("span");
        cargando.className = "boton-accion__cargando";
        cargando.setAttribute("aria-hidden", "true");
        botonEnviar.prepend(cargando);
    });
});