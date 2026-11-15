document.addEventListener("DOMContentLoaded", () => {
    const seccion = document.querySelector("[data-solicitudes]");

    if (!seccion) {
        console.error("No se encontró la sección de solicitudes recibidas.");
        return;
    }

    seccion.classList.remove("sin-js");

    const reducirMovimiento = window.matchMedia("(prefers-reduced-motion: reduce)");
    const items = Array.from(seccion.querySelectorAll(".solicitudes__item"));
    const contador = seccion.querySelector("[data-contador]");
    const vacio = seccion.querySelector("[data-vacio]");
    const avisoLector = seccion.querySelector("[data-aviso]");

    // Devuelve la duración de una animación, o 0 si el usuario prefiere menos movimiento.
    function duracion(milisegundos) {
        return reducirMovimiento.matches ? 0 : milisegundos;
    }

    // Anuncia un mensaje a los lectores de pantalla.
    function anunciar(mensaje) {
        if (avisoLector) {
            avisoLector.textContent = mensaje;
        }
    }

    // 1. Entrada de las tarjetas: aparecen una por una cuando llegan a la pantalla.
    items.forEach((item, indice) => {
        item.style.setProperty("--orden", String(indice % 6));
    });

    if ("IntersectionObserver" in window && !reducirMovimiento.matches) {
        const observador = new IntersectionObserver(entradas => {
            entradas.forEach(entrada => {
                if (entrada.isIntersecting) {
                    entrada.target.classList.add("visible");
                    observador.unobserve(entrada.target);
                }
            });
        }, { threshold: 0.15 });

        items.forEach(item => observador.observe(item));
    } else {
        items.forEach(item => item.classList.add("visible"));
    }

    // 2. Onda en el punto exacto donde se toca o se hace clic en un botón.
    function crearOnda(boton, evento) {
        if (reducirMovimiento.matches) return;

        const rect = boton.getBoundingClientRect();
        const tamano = Math.max(rect.width, rect.height);
        const onda = document.createElement("span");

        onda.className = "onda";
        onda.style.width = `${tamano}px`;
        onda.style.height = `${tamano}px`;
        onda.style.left = `${evento.clientX - rect.left - tamano / 2}px`;
        onda.style.top = `${evento.clientY - rect.top - tamano / 2}px`;

        boton.appendChild(onda);
        onda.addEventListener("animationend", () => onda.remove());
    }

    seccion.querySelectorAll(".boton-accion").forEach(boton => {
        boton.addEventListener("pointerdown", evento => crearOnda(boton, evento));
    });

    // 3. Efecto de presión al tocar una tarjeta en pantallas táctiles.
    seccion.querySelectorAll(".tarjeta-solicitud").forEach(tarjeta => {
        tarjeta.addEventListener("pointerdown", evento => {
            if (evento.pointerType === "touch") {
                tarjeta.classList.add("presionada");
            }
        });

        ["pointerup", "pointercancel", "pointerleave"].forEach(tipo => {
            tarjeta.addEventListener(tipo, () => tarjeta.classList.remove("presionada"));
        });
    });

    // 4. Actualiza el contador con una pequeña animación.
    function actualizarContador(cantidad) {
        if (!contador) return;

        contador.textContent = String(cantidad);
        contador.classList.add("cambio");
        setTimeout(() => contador.classList.remove("cambio"), duracion(300));
    }

    // 5. Envío de Aceptar y Rechazar.
    let restantes = items.length;

    seccion.querySelectorAll("form[data-accion]").forEach(formulario => {
        const boton = formulario.querySelector("button");
        const accion = formulario.dataset.accion;
        const textoOriginal = boton.textContent.trim();
        let esperandoConfirmacion = false;
        let temporizador;

        // Devuelve el botón de rechazar a su estado normal.
        function cancelarConfirmacion() {
            esperandoConfirmacion = false;
            boton.classList.remove("confirmando");
            boton.textContent = textoOriginal;
        }

        formulario.addEventListener("submit", evento => {
            evento.preventDefault();

            // Rechazar pide un segundo toque para evitar errores.
            if (accion === "rechazar" && !esperandoConfirmacion) {
                esperandoConfirmacion = true;
                boton.classList.add("confirmando");
                boton.textContent = "Confirmar rechazo";
                anunciar("Toca otra vez el botón para confirmar el rechazo.");

                temporizador = setTimeout(cancelarConfirmacion, 3500);
                return;
            }

            clearTimeout(temporizador);

            const item = formulario.closest(".solicitudes__item");
            const botones = item.querySelectorAll(".boton-accion");

            // Evita que se envíe dos veces.
            botones.forEach(b => {
                b.disabled = true;
            });

            boton.classList.remove("confirmando");
            boton.innerHTML = '<span class="boton-accion__cargando" aria-hidden="true"></span>' +
                (accion === "aceptar" ? "Aceptando..." : "Rechazando...");
            item.setAttribute("aria-busy", "true");

            anunciar(accion === "aceptar" ? "Aceptando la solicitud." : "Rechazando la solicitud.");

            // La tarjeta sale con una animación y luego se envía el formulario.
            setTimeout(() => {
                item.classList.add("saliendo");
                restantes -= 1;
                actualizarContador(restantes);

                if (restantes === 0 && vacio) {
                    vacio.hidden = false;
                }

                setTimeout(() => formulario.submit(), duracion(350));
            }, duracion(250));
        });

        // Si sale del botón sin confirmar, se cancela el rechazo.
        boton.addEventListener("blur", () => {
            if (esperandoConfirmacion) {
                clearTimeout(temporizador);
                cancelarConfirmacion();
            }
        });
    });

    // 6. Mensajes de éxito o error: se pueden cerrar y se ocultan solos.
    seccion.querySelectorAll("[data-alerta]").forEach(alerta => {
        const botonCerrar = alerta.querySelector("[data-cerrar-alerta]");

        function cerrarAlerta() {
            alerta.classList.add("ocultando");
            setTimeout(() => alerta.remove(), duracion(350));
        }

        if (botonCerrar) {
            botonCerrar.addEventListener("click", cerrarAlerta);
        }

        // El mensaje de éxito se cierra solo; el de error se queda para que se pueda leer.
        if (alerta.classList.contains("solicitudes__alerta--exito")) {
            setTimeout(cerrarAlerta, 6000);
        }
    });
});
