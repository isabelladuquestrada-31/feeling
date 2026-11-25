/*
 * PROYECTO: Feeling
 * ARCHIVO: mis-acompanamientos.js
 * UBICACIÓN: src/main/resources/static/js/mis-acompanamientos.js
 * MÓDULO: Mis acompañamientos (panel del estudiante)
 *
 * AUTOR: Yulipaola Domínguez
 * VERSIÓN: 1.1.0
 * FECHA DE DOCUMENTACIÓN: 2026-11-25
 * ESTADO: Finalizado
 * TICKET: SCRUM-101 (RF-16)
 *
 * DESCRIPCIÓN:
 * Script de la pantalla mis-acompanamientos.html. Maneja el formulario
 * para calificar con estrellas un acompañamiento realizado.
 *
 * FUNCIONALIDADES:
 * - Botón Calificar: abre el formulario de la tarjeta y lleva el foco a
 *   las estrellas.
 * - Botón Cancelar: cierra el formulario y devuelve el foco al botón Calificar.
 * - Texto de la calificación elegida (Muy mala, Mala, Regular, Buena, Excelente).
 * - Contador de caracteres del comentario.
 * - Validación al enviar: no se envía sin elegir estrellas.
 * - Estado de carga del botón mientras se envía, para evitar el doble envío.
 * - Avisos para lectores de pantalla.
 *
 * LO QUE NO HACE ESTE ARCHIVO:
 * Los filtros del historial, la cancelación con confirmación, las alertas
 * y la entrada de las tarjetas los maneja solicitudes.js.
 *
 * DEPENDENCIAS:
 * - mis-acompanamientos.html: aporta los elementos con atributos data-*.
 * - solicitudes.js: filtros, cancelación, alertas y entrada de tarjetas.
 *
 * ATRIBUTOS DATA-* UTILIZADOS:
 * - data-solicitudes: sección principal; si no existe, el script no hace nada.
 * - data-aviso: zona para lectores de pantalla.
 * - data-abrir-calificacion: botón Calificar; su aria-controls indica el formulario.
 * - data-texto-puntuacion y data-error-puntuacion: texto de la calificación y error.
 * - data-comentario-calificacion y data-caracteres-comentario: comentario y su contador.
 * - data-cancelar-calificacion y data-enviar-calificacion: botones del formulario.
 *
 * HISTORIAL DE VERSIONES:
 * - 1.0.0: formulario para calificar (SCRUM-101, RF-16).
 * - 1.1.0 (2026-11-25): documentación completa. No cambia la lógica.
 */


// El script se ejecuta cuando el documento terminó de cargarse.
document.addEventListener("DOMContentLoaded", () => {
    // Sección principal de la pantalla. Si no existe, no se hace nada.
    const seccion = document.querySelector("[data-solicitudes]");
    if (!seccion) {
        return;
    }

    // Zona de avisos para lectores de pantalla.
    const avisoLector = seccion.querySelector("[data-aviso]");
    // Texto que se muestra según el número de estrellas elegido.
    const TEXTOS = {
        1: "Muy mala",
        2: "Mala",
        3: "Regular",
        4: "Buena",
        5: "Excelente"
    };

    // Anuncia un mensaje a los lectores de pantalla.
    function anunciar(mensaje) {
        if (avisoLector) {
            avisoLector.textContent = mensaje;
        }
    }

    // Se configura cada botón Calificar con su formulario.
    seccion.querySelectorAll("[data-abrir-calificacion]").forEach(boton => {
        // El formulario de la tarjeta se identifica con aria-controls del botón.
        const formulario = document.getElementById(boton.getAttribute("aria-controls"));
        if (!formulario) {
            return;
        }

        // Elementos del formulario.
        const estrellas = Array.from(formulario.querySelectorAll("input[name='puntuacion']"));
        const textoPuntuacion = formulario.querySelector("[data-texto-puntuacion]");
        const error = formulario.querySelector("[data-error-puntuacion]");
        const comentario = formulario.querySelector("[data-comentario-calificacion]");
        const caracteres = formulario.querySelector("[data-caracteres-comentario]");
        const cancelar = formulario.querySelector("[data-cancelar-calificacion]");
        const enviar = formulario.querySelector("[data-enviar-calificacion]");
        // Texto inicial, para volver a él si no hay estrellas elegidas.
        const textoInicial = textoPuntuacion ? textoPuntuacion.textContent : "";

        // El aviso propio reemplaza el mensaje del navegador.
        formulario.noValidate = true;

        // Abre o cierra el formulario y oculta el botón Calificar mientras está abierto.
        // Al abrir lleva el foco a la estrella elegida o a la primera.
        function abrirFormulario(abrir) {
            formulario.hidden = !abrir;
            boton.hidden = abrir;
            boton.setAttribute("aria-expanded", String(abrir));
            if (abrir) {
                const elegida = estrellas.find(estrella => estrella.checked) || estrellas[0];
                if (elegida) {
                    elegida.focus();
                }
                anunciar("Formulario para calificar el acompañamiento.");
            } else {
                boton.focus();
            }
        }

        // Muestra u oculta el mensaje de error.
        function mostrarError(mostrar) {
            if (error) {
                error.hidden = !mostrar;
            }
        }

        // Calificar abre el formulario y Cancelar lo cierra.
        boton.addEventListener("click", () => abrirFormulario(true));
        if (cancelar) {
            cancelar.addEventListener("click", () => abrirFormulario(false));
        }

        // Al elegir una estrella se actualiza el texto y se borra el error.
        estrellas.forEach(estrella => {
            estrella.addEventListener("change", () => {
                if (textoPuntuacion) {
                    textoPuntuacion.textContent = TEXTOS[estrella.value] || textoInicial;
                }
                mostrarError(false);
            });
        });

        // Cada vez que se escribe en el comentario se actualiza el contador.
        if (comentario && caracteres) {
            comentario.addEventListener("input", () => {
                caracteres.textContent = String(comentario.value.length);
            });
        }

        // Antes de enviar se revisa que haya estrellas elegidas; si no, se muestra
        // el error y se cancela el envío.
        formulario.addEventListener("submit", evento => {
            if (!estrellas.some(estrella => estrella.checked)) {
                evento.preventDefault();
                mostrarError(true);
                if (estrellas[0]) {
                    estrellas[0].focus();
                }
                return;
            }
            // Si todo está bien, el botón muestra que se está enviando y se desactiva.
            if (enviar) {
                enviar.disabled = true;
                enviar.setAttribute("aria-busy", "true");
                enviar.textContent = "Enviando...";
            }
            anunciar("Enviando la calificación.");
        });
    });
});
