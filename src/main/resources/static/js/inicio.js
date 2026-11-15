
document.addEventListener("DOMContentLoaded", () => {
    const menu = document.getElementById("inicio-navegacion");
    const botonMenu = document.querySelector("[data-menu-boton]");

    if (!menu || !botonMenu) {
        console.error("No se encontró el menú o el botón de navegación.");
        return;
    }

    // El breakpoint debe coincidir con el definido en el CSS.
    const breakpointMovil = window.matchMedia("(max-width: 800px)");
    const enlacesMenu = menu.querySelectorAll("a[href]");

    let menuAbierto = false;

    // Actualiza el estado visual y accesible del menú.
    function actualizarEstadoMenu(abierto) {
        menuAbierto = abierto;

        botonMenu.setAttribute("aria-expanded", String(abierto));
        botonMenu.setAttribute(
            "aria-label",
            abierto ? "Cerrar menú de navegación" : "Abrir menú de navegación"
        );

        menu.classList.toggle("abierto", abierto);
    }

    // Devuelve los elementos visibles que pueden recibir el foco.
    function obtenerElementosEnfocables() {
        const selector = [
            'a[href]',
            'button:not([disabled])',
            'input:not([disabled])',
            'select:not([disabled])',
            'textarea:not([disabled])',
            '[tabindex]:not([tabindex="-1"])'
        ].join(",");

        return Array.from(menu.querySelectorAll(selector)).filter(elemento => {
            return (
                !elemento.hidden &&
                elemento.getAttribute("aria-hidden") !== "true" &&
                elemento.getClientRects().length > 0
            );
        });
    }

    // Abre el menú y lleva el foco al primer enlace.
    function abrirMenu() {
        if (!breakpointMovil.matches || menuAbierto) return;

        menu.hidden = false;
        menu.inert = false;

        actualizarEstadoMenu(true);

        const primerEnlace = menu.querySelector("a[href]");

        if (primerEnlace) {
            primerEnlace.focus();
        }
    }

    // Cierra el menú y, cuando corresponde, devuelve el foco al botón.
    function cerrarMenu(restaurarFoco = true) {
        if (!menuAbierto) return;

        actualizarEstadoMenu(false);

        if (breakpointMovil.matches) {
            menu.hidden = true;
            menu.inert = true;
        }

        if (restaurarFoco) {
            botonMenu.focus();
        }
    }

    // Controla la apertura y el cierre desde el botón.
    botonMenu.type = "button";

    botonMenu.addEventListener("click", () => {
        if (!breakpointMovil.matches) return;

        if (menuAbierto) {
            cerrarMenu();
        } else {
            abrirMenu();
        }
    });

    // Cierra el menú cuando se selecciona un enlace.
    enlacesMenu.forEach(enlace => {
        enlace.addEventListener("click", () => {
            if (breakpointMovil.matches && menuAbierto) {
                cerrarMenu(false);
            }
        });
    });

    // Mantiene la navegación por teclado dentro del menú abierto.
    menu.addEventListener("keydown", event => {
        if (!breakpointMovil.matches || !menuAbierto || event.key !== "Tab") {
            return;
        }

        const elementos = obtenerElementosEnfocables();

        if (elementos.length === 0) return;

        const primero = elementos[0];
        const ultimo = elementos[elementos.length - 1];

        if (event.shiftKey && document.activeElement === primero) {
            event.preventDefault();
            ultimo.focus();
        } else if (!event.shiftKey && document.activeElement === ultimo) {
            event.preventDefault();
            primero.focus();
        }
    });

    // Cierra el menú al pulsar Escape.
    document.addEventListener("keydown", event => {
        if (event.key === "Escape" && breakpointMovil.matches && menuAbierto) {
            event.preventDefault();
            cerrarMenu();
        }
    });

    // Cierra el menú cuando se hace clic fuera de él.
    document.addEventListener("click", event => {
        if (
            breakpointMovil.matches &&
            menuAbierto &&
            !menu.contains(event.target) &&
            !botonMenu.contains(event.target)
        ) {
            cerrarMenu(false);
        }
    });

    // Ajusta la visibilidad del menú cuando cambia el tamaño de pantalla.
    breakpointMovil.addEventListener("change", event => {
        actualizarEstadoMenu(false);

        if (event.matches) {
            menu.hidden = true;
            menu.inert = true;

            if (menu.contains(document.activeElement)) {
                botonMenu.focus();
            }
        } else {
            menu.hidden = false;
            menu.inert = false;
        }
    });

    // Configura el estado inicial según el tamaño de pantalla.
    if (breakpointMovil.matches) {
        menu.hidden = true;
        menu.inert = true;
    } else {
        menu.hidden = false;
        menu.inert = false;
    }

    actualizarEstadoMenu(false);
});
