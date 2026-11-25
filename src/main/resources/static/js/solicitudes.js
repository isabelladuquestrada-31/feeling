/*
 * PROYECTO: Feeling
 * ARCHIVO: solicitudes.js
 * UBICACIÓN: src/main/resources/static/js/solicitudes.js
 * MÓDULO: Comportamiento compartido de las pantallas de solicitudes
 *
 * AUTOR: Yulipaola Domínguez
 * VERSIÓN: 1.2.0
 * FECHA DE DOCUMENTACIÓN: 2026-11-25
 * ESTADO: Finalizado
 * TICKET: SCRUM-83 (RF-10), con ampliaciones de RF-07, RF-12 y RF-13
 *
 * DESCRIPCIÓN:
 * Script base de las pantallas con tarjetas de solicitudes. Nació para las
 * solicitudes recibidas del voluntario (RF-10) y hoy lo cargan también Mis
 * acompañamientos, Buscar voluntarios, Mi disponibilidad y Gestión de
 * usuarios. Cada parte solo actúa si la pantalla tiene los elementos que
 * necesita.
 *
 * FUNCIONALIDADES:
 * - Entrada de las tarjetas una por una cuando llegan a la pantalla.
 * - Onda en el punto donde se toca un botón de acción.
 * - Efecto de presión al tocar una tarjeta en pantallas táctiles.
 * - Contador de pendientes con una pequeña animación.
 * - Formularios de acción (aceptar, rechazar, cancelar y los demás con
 *   data-accion): rechazar y cancelar piden un segundo toque para
 *   confirmar, el botón muestra la carga y la tarjeta puede salir con
 *   una animación antes de enviar.
 * - Mensajes de éxito y error que se pueden cerrar; el de éxito se cierra
 *   solo a los 6 segundos.
 * - Filtros del historial por estado, con la cantidad de cada uno.
 * - Filtros de búsqueda que se envían solos al cambiar una opción.
 * - Avisos para lectores de pantalla.
 *
 * DEPENDENCIAS:
 * - solicitudes.css: clases visible, saliendo, presionada, confirmando,
 *   cambio, ocultando, onda y boton-accion__cargando.
 * - Las plantillas que lo cargan aportan los atributos data-*.
 *
 * ATRIBUTOS DATA-* UTILIZADOS:
 * - data-solicitudes: sección principal; si no existe, el script no hace nada.
 * - data-contador y data-vacio: contador de pendientes y estado vacío.
 * - data-aviso: zona para lectores de pantalla.
 * - data-accion y data-quitar-tarjeta: formularios de acción.
 * - data-alerta y data-cerrar-alerta: mensajes que se pueden cerrar.
 * - data-filtro, data-cantidad, data-estado y data-sin-resultados: filtros del historial.
 * - data-busqueda, data-resultados y data-autoenviar: filtros de búsqueda.
 *
 * OBSERVACIONES:
 * - Al cargar quita la clase sin-js de la sección, para que los estilos
 *   sepan que el JavaScript está activo.
 * - Respeta la preferencia de reducir el movimiento: en ese caso no hay
 *   animaciones ni esperas.
 *
 * HISTORIAL DE VERSIONES:
 * - 1.0.0: entrada de tarjetas, acciones y alertas de solicitudes recibidas (SCRUM-83, RF-10).
 * - 1.1.0: filtros del historial y cancelación (RF-12 y RF-13) y filtros de búsqueda (RF-07).
 * - 1.2.0 (2026-11-25): documentación completa. No cambia la lógica.
 */

// El script se ejecuta cuando el documento terminó de cargarse.
document.addEventListener("DOMContentLoaded", () => {
    // Sección principal de la pantalla. Si no existe, no se hace nada.
    const seccion = document.querySelector("[data-solicitudes]");

    if (!seccion) {
        console.error("No se encontró la sección de solicitudes recibidas.");
        return;
    }

    // Con JavaScript activo se quita sin-js para que las tarjetas empiecen ocultas.
    seccion.classList.remove("sin-js");

    // Preferencia de reducir el movimiento, tarjetas, contador, estado vacío
    // y zona de avisos para lectores de pantalla.
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
    // Cada tarjeta recibe un orden (de 0 a 5) para escalonar su entrada.
    items.forEach((item, indice) => {
        item.style.setProperty("--orden", String(indice % 6));
    });

    // Si el navegador lo permite, cada tarjeta aparece cuando entra en la pantalla.
    // Si no, o si se pidió menos movimiento, se muestran todas de una vez.
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
    // Crea un círculo que crece desde el punto donde se tocó el botón.
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

    // Cada botón de acción crea la onda al presionarlo.
    seccion.querySelectorAll(".boton-accion").forEach(boton => {
        boton.addEventListener("pointerdown", evento => crearOnda(boton, evento));
    });

    // 3. Efecto de presión al tocar una tarjeta en pantallas táctiles.
    seccion.querySelectorAll(".tarjeta-solicitud, .tarjeta-proximo, .tarjeta-historial, .tarjeta-voluntario").forEach(tarjeta => {
        // Solo con el dedo (no con el mouse) se aplica el efecto de presión.
        tarjeta.addEventListener("pointerdown", evento => {
            if (evento.pointerType === "touch") {
                tarjeta.classList.add("presionada");
            }
        });

        // Al soltar, cancelar o salir de la tarjeta se quita el efecto.
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
    // Cantidad de tarjetas que quedan, para el contador y el estado vacío.
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

    // Se configura cada formulario de acción.
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

        // Al enviar se cancela el envío normal para manejarlo aquí.
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

            // Tarjeta del formulario y todos sus botones.
            const item = formulario.closest(".solicitudes__item");
            const botones = item ? item.querySelectorAll(".boton-accion") : [boton];

            // Evita que se envíe dos veces.
            botones.forEach(b => {
                b.disabled = true;
            });

            // El botón muestra el círculo de carga y el texto de la acción.
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
    // Se configura cada mensaje.
    seccion.querySelectorAll("[data-alerta]").forEach(alerta => {
        const botonCerrar = alerta.querySelector("[data-cerrar-alerta]");

        // Cierra el mensaje con una animación y luego lo quita de la página.
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
    // Botones de filtro, tarjetas que se pueden filtrar y mensaje sin resultados.
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

    // Muestra solo las tarjetas del estado elegido, marca el filtro activo
    // y anuncia cuántas quedaron.
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

    // Cada botón de filtro aplica su estado al hacer clic.
    botonesFiltro.forEach(boton => {
        boton.addEventListener("click", () => aplicarFiltro(boton.dataset.filtro));
    });

    // 8. Filtros de búsqueda que se aplican solos al cambiar una opción.
    seccion.querySelectorAll("form[data-busqueda]").forEach(formulario => {
        // Zona de resultados que se atenúa mientras se busca.
        const resultados = seccion.querySelector("[data-resultados]");

        // Atenúa los resultados, avisa y envía el formulario.
        function buscar() {
            if (resultados) {
                resultados.classList.add("buscando");
                resultados.setAttribute("aria-busy", "true");
            }

            anunciar("Buscando voluntarios.");
            formulario.submit();
        }

        // Los campos con data-autoenviar envían la búsqueda al cambiar.
        formulario.querySelectorAll("[data-autoenviar]").forEach(campo => {
            campo.addEventListener("change", buscar);
        });
    });
});
