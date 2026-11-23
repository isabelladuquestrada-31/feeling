/* ==========================================================
   Mis acompañamientos - Calificar (RF-16, SCRUM-101)
   - "Calificar" abre y cierra el formulario de la tarjeta.
   - Muestra el texto de las estrellas elegidas y cuenta los
     caracteres del comentario.
   - No envía sin estrellas y evita el doble envío.
   Los filtros, las alertas y la entrada de las tarjetas los
   maneja solicitudes.js.
   ========================================================== */

document.addEventListener("DOMContentLoaded", () => {
    const seccion = document.querySelector("[data-solicitudes]");
    if (!seccion) {
        return;
    }

    const avisoLector = seccion.querySelector("[data-aviso]");
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

    seccion.querySelectorAll("[data-abrir-calificacion]").forEach(boton => {
        const formulario = document.getElementById(boton.getAttribute("aria-controls"));
        if (!formulario) {
            return;
        }

        const estrellas = Array.from(formulario.querySelectorAll("input[name='puntuacion']"));
        const textoPuntuacion = formulario.querySelector("[data-texto-puntuacion]");
        const error = formulario.querySelector("[data-error-puntuacion]");
        const comentario = formulario.querySelector("[data-comentario-calificacion]");
        const caracteres = formulario.querySelector("[data-caracteres-comentario]");
        const cancelar = formulario.querySelector("[data-cancelar-calificacion]");
        const enviar = formulario.querySelector("[data-enviar-calificacion]");
        const textoInicial = textoPuntuacion ? textoPuntuacion.textContent : "";

        // El aviso propio reemplaza el mensaje del navegador.
        formulario.noValidate = true;

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

        function mostrarError(mostrar) {
            if (error) {
                error.hidden = !mostrar;
            }
        }

        boton.addEventListener("click", () => abrirFormulario(true));
        if (cancelar) {
            cancelar.addEventListener("click", () => abrirFormulario(false));
        }

        estrellas.forEach(estrella => {
            estrella.addEventListener("change", () => {
                if (textoPuntuacion) {
                    textoPuntuacion.textContent = TEXTOS[estrella.value] || textoInicial;
                }
                mostrarError(false);
            });
        });

        if (comentario && caracteres) {
            comentario.addEventListener("input", () => {
                caracteres.textContent = String(comentario.value.length);
            });
        }

        formulario.addEventListener("submit", evento => {
            if (!estrellas.some(estrella => estrella.checked)) {
                evento.preventDefault();
                mostrarError(true);
                if (estrellas[0]) {
                    estrellas[0].focus();
                }
                return;
            }
            if (enviar) {
                enviar.disabled = true;
                enviar.setAttribute("aria-busy", "true");
                enviar.textContent = "Enviando...";
            }
            anunciar("Enviando la calificación.");
        });
    });
});
