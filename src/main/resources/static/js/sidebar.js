/* =========================================
   FEELING - MENÚ LATERAL
   Navegación compacta, expandida y móvil
   ========================================= */

document.addEventListener("DOMContentLoaded", () => {

    const sidebar = document.getElementById("main-sidebar");

    if (!sidebar) {
        return;
    }

    const openButtons = document.querySelectorAll("[data-sidebar-open]");
    const closeButtons = document.querySelectorAll("[data-sidebar-close]");
    const toggleButton = sidebar.querySelector("[data-sidebar-toggle]");

    const mobileQuery = window.matchMedia("(max-width: 768px)");

    let previousFocus = null;

    const isMobile = () => mobileQuery.matches;

    /* ACTUALIZAR EL ESTADO VISUAL DEL MENÚ */
    function updateToggleState(expanded) {

        sidebar.classList.toggle("sidebar--expanded", expanded);
        sidebar.classList.toggle("sidebar--collapsed", !expanded);

        if (toggleButton) {
            toggleButton.setAttribute("aria-expanded", String(expanded));
            toggleButton.setAttribute(
                "aria-label",
                expanded ? "Contraer menú" : "Expandir menú"
            );
            toggleButton.setAttribute(
                "title",
                expanded ? "Contraer menú" : "Expandir menú"
            );
        }
    }

    /* EXPANDIR O CONTRAER EN ESCRITORIO */
    function toggleSidebar() {

        if (isMobile()) {
            return;
        }

        const isExpanded = sidebar.classList.contains("sidebar--expanded");

        updateToggleState(!isExpanded);
    }

    /* ABRIR MENÚ EN MÓVILES */
    function openSidebar() {

        if (!isMobile()) {
            return;
        }

        previousFocus = document.activeElement;

        sidebar.inert = false;

        document.body.classList.add("sidebar-is-open");

        openButtons.forEach(button => {
            button.setAttribute("aria-expanded", "true");
        });

        const firstLink = sidebar.querySelector(".sidebar__link");

        if (firstLink) {
            firstLink.focus();
        }
    }

    /* CERRAR MENÚ EN MÓVILES */
    function closeSidebar(restoreFocus = true) {

        document.body.classList.remove("sidebar-is-open");

        openButtons.forEach(button => {
            button.setAttribute("aria-expanded", "false");
        });

        if (isMobile()) {
            sidebar.inert = true;
        }

        if (
            restoreFocus &&
            previousFocus &&
            typeof previousFocus.focus === "function"
        ) {
            previousFocus.focus();
        }

        previousFocus = null;
    }

    /* EVENTO DEL BOTÓN DE EXPANSIÓN */
    if (toggleButton) {
        toggleButton.addEventListener("click", toggleSidebar);
    }

    /* EVENTOS PARA ABRIR EN MÓVILES */
    openButtons.forEach(button => {
        button.addEventListener("click", openSidebar);
    });

    /* EVENTOS PARA CERRAR */
    closeButtons.forEach(button => {
        button.addEventListener("click", () => {
            closeSidebar();
        });
    });

    /* CERRAR CON ESCAPE */
    document.addEventListener("keydown", event => {

        if (
            event.key === "Escape" &&
            document.body.classList.contains("sidebar-is-open")
        ) {
            closeSidebar();
        }

    });

    /* CERRAR AL SELECCIONAR UNA OPCIÓN EN MÓVILES */
    sidebar.querySelectorAll("a").forEach(link => {

        link.addEventListener("click", () => {

            if (isMobile()) {
                closeSidebar(false);
            }

        });

    });

    /* CAMBIAR DE COMPORTAMIENTO AL CAMBIAR DE TAMAÑO */
    mobileQuery.addEventListener("change", event => {

        if (event.matches) {

            closeSidebar(false);
            updateToggleState(false);
            sidebar.inert = true;

        } else {

            closeSidebar(false);
            sidebar.inert = false;
            updateToggleState(true);

        }

    });

    /* INICIAR EXPANDIDO EN ESCRITORIO Y COMPACTO EN MÓVILES */
    if (isMobile()) {
        updateToggleState(false);
        sidebar.inert = true;
    } else {
        updateToggleState(true);
        sidebar.inert = false;
    }

});