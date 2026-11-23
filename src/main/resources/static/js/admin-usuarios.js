/* ==========================================================
   Gestión de usuarios - Suspender voluntarios (RF-17, SCRUM-103)
   - "Suspender" abre y cierra el formulario de la tarjeta.
   - Cuenta los caracteres del motivo.
   - No envía sin motivo o con una fecha de fin que no sea posterior a hoy.
   Las alertas, la entrada de las tarjetas y el botón "Reactivar"
   los maneja solicitudes.js.
   ========================================================== */

document.addEventListener("DOMContentLoaded", () => {
    const seccion = document.querySelector("[data-solicitudes]");
    if (!seccion) {
        return;
    }

    const avisoLector = seccion.querySelector("[data-aviso]");

    // Anuncia un mensaje a los lectores de pantalla.
    function anunciar(mensaje) {
        if (avisoLector) {
            avisoLector.textContent = mensaje;
        }
    }

    seccion.querySelectorAll("[data-abrir-suspension]").forEach(boton => {
        const formulario = document.getElementById(boton.getAttribute("aria-controls"));
        if (!formulario) {
            return;
        }

        const motivo = formulario.querySelector("[data-motivo-suspension]");
        const caracteres = formulario.querySelector("[data-caracteres-motivo]");
        const fecha = formulario.querySelector("[data-fecha-suspension]");
        const error = formulario.querySelector("[data-error-suspension]");
        const cancelar = formulario.querySelector("[data-cancelar-suspension]");
        const enviar = formulario.querySelector("[data-enviar-suspension]");

        function abrirFormulario(abrir) {
            formulario.hidden = !abrir;
            boton.hidden = abrir;
            boton.setAttribute("aria-expanded", String(abrir));
            if (abrir) {
                motivo.focus();
                anunciar("Formulario para suspender al voluntario.");
            } else {
                mostrarError("");
                boton.focus();
            }
        }

        function mostrarError(mensaje, campo) {
            error.textContent = mensaje;
            error.hidden = !mensaje;
            motivo.removeAttribute("aria-invalid");
            fecha.removeAttribute("aria-invalid");
            if (campo) {
                campo.setAttribute("aria-invalid", "true");
                campo.focus();
            }
        }

        boton.addEventListener("click", () => abrirFormulario(true));
        cancelar.addEventListener("click", () => abrirFormulario(false));

        motivo.addEventListener("input", () => {
            caracteres.textContent = String(motivo.value.length);
        });

        formulario.addEventListener("submit", evento => {
            if (!motivo.value.trim()) {
                evento.preventDefault();
                mostrarError("Debe indicar el motivo de la suspensión.", motivo);
                return;
            }
            // La fecha llega como aaaa-mm-dd, así que se puede comparar como texto.
            if (!fecha.value || (fecha.min && fecha.value < fecha.min)) {
                evento.preventDefault();
                mostrarError("La fecha de fin debe ser posterior a hoy.", fecha);
                return;
            }
            mostrarError("");
            enviar.disabled = true;
            enviar.setAttribute("aria-busy", "true");
            enviar.textContent = "Suspendiendo...";
            anunciar("Suspendiendo al voluntario.");
        });
    });
});
