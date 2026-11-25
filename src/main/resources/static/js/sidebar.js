/*
    PROYECTO: Feeling
    ARCHIVO: sidebar.js
    UBICACIÓN: src/main/resources/static/js/sidebar.js
    MÓDULO: Plantilla base, menú lateral

    AUTOR: Yulipaola Domínguez
    VERSIÓN: 1.1.0
    FECHA DE DOCUMENTACIÓN: 2026-11-24
    ESTADO: Finalizado
    TICKET: SCRUM-43

    DESCRIPCIÓN:
    Script que controla el menú lateral (sidebar) de las páginas internas.
    En escritorio permite alternar entre un menú expandido y uno compacto.
    En celulares y tabletas (768 px o menos) el menú permanece oculto y se
    abre como panel superpuesto mediante el botón del encabezado, con
    manejo del foco para facilitar la navegación con teclado.

    FUNCIONALIDADES:
    - Expandir y contraer el menú en escritorio.
    - Abrir y cerrar el menú en móviles.
    - Cerrar el menú con la tecla Escape.
    - Cerrar el menú al seleccionar un enlace en móviles.
    - Restaurar el foco al elemento que abrió el menú.
    - Ajustar el comportamiento al cambiar el tamaño de la pantalla.
    - Mantener actualizados aria-expanded, aria-label y title del
      botón de expansión.

    TECNOLOGÍAS:
    - JavaScript (ES6): funciones, constantes y funciones flecha.
    - DOM API: querySelector, classList, setAttribute, addEventListener.
    - matchMedia: detección del punto de quiebre móvil.
    - Propiedad inert: bloquea foco e interacción del menú oculto.
    - ARIA: accesibilidad del menú.

    DEPENDENCIAS:
    - Menú lateral con id "main-sidebar" en la plantilla interna.
    - templates/fragments/header.html: botón con data-sidebar-open.
    - /css/interno.css: estilos de las clases sidebar--expanded,
      sidebar--collapsed y sidebar-is-open.

    ELEMENTOS DEL DOM UTILIZADOS:
    - #main-sidebar: contenedor del menú lateral.
    - [data-sidebar-open]: botones que abren el menú en móviles.
    - [data-sidebar-close]: botones que cierran el menú en móviles.
    - [data-sidebar-toggle]: botón que expande o contrae el menú.
    - .sidebar__link: enlaces de navegación del menú.

    CLASES CSS QUE CONTROLA:
    - sidebar--expanded: menú expandido (escritorio).
    - sidebar--collapsed: menú compacto (escritorio) u oculto (móvil).
    - sidebar-is-open (en body): menú abierto en móviles.

    HISTORIAL DE VERSIONES:
    - 1.0.0: implementación inicial del menú lateral.
    - 1.1.0 (2026-11-24): documentación completa. No se modificó el
      código.

    OBSERVACIONES:
    - Si la página no tiene el elemento #main-sidebar, el script termina
      sin generar errores.
    - El punto de quiebre móvil (768 px) debe coincidir con el usado en
      la hoja de estilos.
    - En móviles el menú usa inert cuando está cerrado, así el teclado
      no puede enfocar enlaces ocultos.
*/

/* Espera a que el HTML termine de cargarse para poder encontrar los
   elementos del menú antes de usarlos. */
document.addEventListener("DOMContentLoaded", () => {

    /* Contenedor principal del menú lateral. */
    const sidebar = document.getElementById("main-sidebar");

    /* Protección: si la página no tiene menú lateral, se detiene el
       script para evitar errores. */
    if (!sidebar) {
        return;
    }

    /* Botones que abren el menú en móviles (por ejemplo, el del
       encabezado). Puede haber más de uno, por eso se usa querySelectorAll. */
    const openButtons = document.querySelectorAll("[data-sidebar-open]");

    /* Botones que cierran el menú en móviles (por ejemplo, la equis o el
       fondo oscuro). */
    const closeButtons = document.querySelectorAll("[data-sidebar-close]");

    /* Botón del propio menú que alterna entre expandido y compacto en
       escritorio. Puede ser null si no existe en la página. */
    const toggleButton = sidebar.querySelector("[data-sidebar-toggle]");

    /* Consulta de medios: indica si el ancho de pantalla es de 768 px o
       menos, es decir, si se considera vista móvil. */
    const mobileQuery = window.matchMedia("(max-width: 768px)");

    /* Elemento que tenía el foco antes de abrir el menú. Se guarda para
       devolverle el foco cuando el menú se cierre. */
    let previousFocus = null;

    /* Devuelve true si la pantalla está en vista móvil en este momento. */
    const isMobile = () => mobileQuery.matches;

    /* ACTUALIZAR EL ESTADO VISUAL DEL MENÚ
       Recibe true para expandir o false para contraer. Cambia las clases
       CSS y actualiza los atributos de accesibilidad del botón. */
    function updateToggleState(expanded) {

        /* classList.toggle con segundo parámetro: agrega la clase si es
           true y la quita si es false. Así siempre queda una sola de las
           dos clases activa. */
        sidebar.classList.toggle("sidebar--expanded", expanded);
        sidebar.classList.toggle("sidebar--collapsed", !expanded);

        /* Solo se actualiza el botón si existe en la página. */
        if (toggleButton) {

            /* Informa a los lectores de pantalla si el menú está expandido. */
            toggleButton.setAttribute("aria-expanded", String(expanded));

            /* Nombre accesible del botón según la acción que realizará. */
            toggleButton.setAttribute(
                "aria-label",
                expanded ? "Contraer menú" : "Expandir menú"
            );

            /* Texto de ayuda que aparece al pasar el cursor. */
            toggleButton.setAttribute(
                "title",
                expanded ? "Contraer menú" : "Expandir menú"
            );
        }
    }

    /* EXPANDIR O CONTRAER EN ESCRITORIO
       Alterna el estado del menú. No hace nada en móviles, donde el menú
       se abre y se cierra con otras funciones. */
    function toggleSidebar() {

        if (isMobile()) {
            return;
        }

        /* Lee el estado actual a partir de la clase del menú. */
        const isExpanded = sidebar.classList.contains("sidebar--expanded");

        /* Aplica el estado contrario al actual. */
        updateToggleState(!isExpanded);
    }

    /* ABRIR MENÚ EN MÓVILES
       Muestra el menú como panel superpuesto y mueve el foco a su primer
       enlace. No hace nada en escritorio. */
    function openSidebar() {

        if (!isMobile()) {
            return;
        }

        /* Guarda el elemento enfocado para restaurarlo al cerrar. */
        previousFocus = document.activeElement;

        /* Quita el bloqueo para que el menú pueda recibir foco y clics. */
        sidebar.inert = false;

        /* Esta clase en body hace visible el menú mediante CSS. */
        document.body.classList.add("sidebar-is-open");

        /* Informa a los lectores de pantalla que el menú está abierto. */
        openButtons.forEach(button => {
            button.setAttribute("aria-expanded", "true");
        });

        /* Primer enlace del menú: recibe el foco para que el teclado
           empiece a navegar dentro del panel. */
        const firstLink = sidebar.querySelector(".sidebar__link");

        if (firstLink) {
            firstLink.focus();
        }
    }

    /* CERRAR MENÚ EN MÓVILES
       Oculta el panel. El parámetro restoreFocus (true por defecto)
       indica si se debe devolver el foco al elemento que abrió el menú.
       Se envía false cuando el cierre ocurre por otra razón, como
       seguir un enlace o cambiar el tamaño de la pantalla. */
    function closeSidebar(restoreFocus = true) {

        /* Quita la clase que mantiene visible el menú. */
        document.body.classList.remove("sidebar-is-open");

        /* Informa a los lectores de pantalla que el menú está cerrado. */
        openButtons.forEach(button => {
            button.setAttribute("aria-expanded", "false");
        });

        /* En móviles el menú cerrado se bloquea con inert para que sus
           enlaces ocultos no reciban foco con el teclado. */
        if (isMobile()) {
            sidebar.inert = true;
        }

        /* Devuelve el foco solo si se pidió, si había un elemento
           guardado y si ese elemento todavía puede recibir foco. */
        if (
            restoreFocus &&
            previousFocus &&
            typeof previousFocus.focus === "function"
        ) {
            previousFocus.focus();
        }

        /* Limpia la referencia para el próximo ciclo de apertura. */
        previousFocus = null;
    }

    /* EVENTO DEL BOTÓN DE EXPANSIÓN
       Conecta el botón del menú con la función que alterna el estado. */
    if (toggleButton) {
        toggleButton.addEventListener("click", toggleSidebar);
    }

    /* EVENTOS PARA ABRIR EN MÓVILES
       Cada botón de apertura ejecuta openSidebar al hacer clic. */
    openButtons.forEach(button => {
        button.addEventListener("click", openSidebar);
    });

    /* EVENTOS PARA CERRAR
       Cada botón de cierre ejecuta closeSidebar. Se usa una función
       flecha para no enviar el objeto del evento como parámetro, ya que
       closeSidebar espera un valor booleano. */
    closeButtons.forEach(button => {
        button.addEventListener("click", () => {
            closeSidebar();
        });
    });

    /* CERRAR CON ESCAPE
       Si el menú móvil está abierto, la tecla Escape lo cierra. */
    document.addEventListener("keydown", event => {

        if (
            event.key === "Escape" &&
            document.body.classList.contains("sidebar-is-open")
        ) {
            closeSidebar();
        }

    });

    /* CERRAR AL SELECCIONAR UNA OPCIÓN EN MÓVILES
       Al pulsar un enlace del menú en móviles, el panel se cierra para
       no tapar la página de destino. Se envía false porque la página
       cambia y no tiene sentido devolver el foco. */
    sidebar.querySelectorAll("a").forEach(link => {

        link.addEventListener("click", () => {

            if (isMobile()) {
                closeSidebar(false);
            }

        });

    });

    /* CAMBIAR DE COMPORTAMIENTO AL CAMBIAR DE TAMAÑO
       Se ejecuta cuando la pantalla cruza el límite de 768 px.
       event.matches es true si ahora está en vista móvil. */
    mobileQuery.addEventListener("change", event => {

        if (event.matches) {

            /* Pasó a vista móvil: cierra cualquier panel abierto, deja
               el menú contraído y lo bloquea hasta que se abra. */
            closeSidebar(false);
            updateToggleState(false);
            sidebar.inert = true;

        } else {

            /* Pasó a escritorio: cierra el modo móvil, desbloquea el
               menú y lo muestra expandido. */
            closeSidebar(false);
            sidebar.inert = false;
            updateToggleState(true);

        }

    });

    /* INICIAR EXPANDIDO EN ESCRITORIO Y COMPACTO EN MÓVILES
       Define el estado inicial según el tamaño de pantalla al cargar. */
    if (isMobile()) {
        updateToggleState(false);
        sidebar.inert = true;
    } else {
        updateToggleState(true);
        sidebar.inert = false;
    }

});