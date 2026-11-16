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
    seccion.querySelectorAll(".tarjeta-solicitud, .tarjeta-proximo, .tarjeta-historial, .tarjeta-voluntario").forEach(tarjeta => {
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

    // 5. Envío de los formularios de acción (aceptar, rechazar y cancelar).
    let restantes = items.length;

    // Acciones que piden un segundo toque para evitar errores.
    const textosConfirmar = {
        rechazar: "Confirmar rechazo",
        cancelar: "Confirmar cancelación"
    };

    // Texto que se muestra mientras se envía cada acción.
    const textosCargando = {
        aceptar: "Aceptando...",
        rechazar: "Rechazando...",
        cancelar: "Cancelando..."
    };

    seccion.querySelectorAll("form[data-accion]").forEach(formulario => {
        const boton = formulario.querySelector("button");
        const accion = formulario.dataset.accion;
        const textoOriginal = boton.textContent.trim();
        const quitarTarjeta = formulario.hasAttribute("data-quitar-tarjeta");
        let esperandoConfirmacion = false;
        let temporizador;

        // Devuelve el botón a su estado normal.
        function cancelarConfirmacion() {
            esperandoConfirmacion = false;
            boton.classList.remove("confirmando");
            boton.textContent = textoOriginal;
        }

        formulario.addEventListener("submit", evento => {
            evento.preventDefault();

            // Primer toque: pide confirmación.
            if (textosConfirmar[accion] && !esperandoConfirmacion) {
                esperandoConfirmacion = true;
                boton.classList.add("confirmando");
                boton.textContent = textosConfirmar[accion];
                anunciar("Toca otra vez el botón para confirmar.");

                temporizador = setTimeout(cancelarConfirmacion, 3500);
                return;
            }

            clearTimeout(temporizador);

            const item = formulario.closest(".solicitudes__item");
            const botones = item ? item.querySelectorAll(".boton-accion") : [boton];

            // Evita que se envíe dos veces.
            botones.forEach(b => {
                b.disabled = true;
            });

            boton.classList.remove("confirmando");
            boton.innerHTML = '<span class="boton-accion__cargando" aria-hidden="true"></span>' +
                (textosCargando[accion] || "Enviando...");

            if (item) {
                item.setAttribute("aria-busy", "true");
            }

            anunciar(textosCargando[accion] || "Enviando...");

            // Si la tarjeta debe desaparecer, sale con una animación antes de enviar.
            setTimeout(() => {
                if (quitarTarjeta && item) {
                    item.classList.add("saliendo");
                    restantes -= 1;
                    actualizarContador(restantes);

                    if (restantes === 0 && vacio) {
                        vacio.hidden = false;
                    }
                }

                setTimeout(() => formulario.submit(), duracion(quitarTarjeta ? 350 : 150));
            }, duracion(250));
        });

        // Si sale del botón sin confirmar, se cancela la confirmación.
        // (No aplica cuando solo se cambia de ventana o de pestaña).
        boton.addEventListener("blur", () => {
            if (esperandoConfirmacion && document.hasFocus()) {
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

    // 7. Filtros del historial por estado.
    const botonesFiltro = Array.from(seccion.querySelectorAll("[data-filtro]"));
    const itemsFiltrables = Array.from(seccion.querySelectorAll("[data-estado]"));
    const sinResultados = seccion.querySelector("[data-sin-resultados]");

    // Muestra en cada filtro cuántas solicitudes tiene.
    botonesFiltro.forEach(boton => {
        const filtro = boton.dataset.filtro;
        const cantidad = filtro === "TODAS"
            ? itemsFiltrables.length
            : itemsFiltrables.filter(item => item.dataset.estado === filtro).length;
        const etiqueta = boton.querySelector("[data-cantidad]");

        if (etiqueta) {
            etiqueta.textContent = String(cantidad);
        }

        // Oculta los filtros que no tienen solicitudes (menos "Todas").
        if (filtro !== "TODAS" && cantidad === 0) {
            boton.hidden = true;
        }
    });

    function aplicarFiltro(filtro) {
        let visibles = 0;

        itemsFiltrables.forEach(item => {
            const coincide = filtro === "TODAS" || item.dataset.estado === filtro;

            item.hidden = !coincide;

            if (coincide) {
                visibles += 1;
                // Repite la animación de entrada al mostrar la tarjeta.
                item.classList.remove("visible");
                requestAnimationFrame(() => item.classList.add("visible"));
            }
        });

        botonesFiltro.forEach(boton => {
            const activo = boton.dataset.filtro === filtro;
            boton.classList.toggle("filtro--activo", activo);
            boton.setAttribute("aria-pressed", String(activo));
        });

        if (sinResultados) {
            sinResultados.hidden = visibles > 0;
        }

        anunciar(`${visibles} ${visibles === 1 ? "solicitud" : "solicitudes"} en este filtro.`);
    }

    botonesFiltro.forEach(boton => {
        boton.addEventListener("click", () => aplicarFiltro(boton.dataset.filtro));
    });

    // 8. Filtros de búsqueda que se aplican solos al cambiar una opción.
    seccion.querySelectorAll("form[data-busqueda]").forEach(formulario => {
        const resultados = seccion.querySelector("[data-resultados]");

        function buscar() {
            if (resultados) {
                resultados.classList.add("buscando");
                resultados.setAttribute("aria-busy", "true");
            }

            anunciar("Buscando voluntarios.");
            formulario.submit();
        }

        formulario.querySelectorAll("[data-autoenviar]").forEach(campo => {
            campo.addEventListener("change", buscar);
        });
    });
});
