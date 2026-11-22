/**
 * PROYECTO: Feeling
 * MÓDULO: Página de inicio
 * ARCHIVO: inicio.js
 * UBICACIÓN: src/main/resources/static/js/inicio.js
 *
 * AUTOR: Yulipaola Domínguez
 * VERSIÓN: 2.0.0
 * FECHA DE DOCUMENTACIÓN: 2026-11-16
 * ESTADO: Finalizado
 *
 * DESCRIPCIÓN:
 * Archivo encargado de gestionar el comportamiento interactivo del menú
 * de navegación de la página de inicio de Feeling, especialmente en
 * dispositivos móviles.
 *
 * OBJETIVO:
 * Proporcionar una navegación adaptable y accesible que permita abrir,
 * cerrar y utilizar el menú mediante el ratón y el teclado, manteniendo
 * correctamente el estado visual y accesible de sus elementos.
 *
 * FUNCIONALIDADES PRINCIPALES:
 * - Detectar el tamaño de pantalla para adaptar el menú a dispositivos móviles.
 * - Abrir y cerrar el menú mediante el botón de navegación.
 * - Actualizar los atributos de accesibilidad del botón.
 * - Gestionar el foco del teclado al abrir y cerrar el menú.
 * - Permitir la navegación cíclica mediante la tecla Tab.
 * - Cerrar el menú al seleccionar un enlace.
 * - Cerrar el menú mediante la tecla Escape.
 * - Cerrar el menú al hacer clic fuera de su área.
 * - Adaptar el estado del menú cuando cambia el tamaño de pantalla.
 *
 * VARIABLES PRINCIPALES:
 * - menu: elemento de navegación (HTMLElement).
 * - botonMenu: botón que abre y cierra el menú (HTMLElement).
 * - breakpointMovil: consulta de medios que detecta la vista móvil (MediaQueryList).
 * - enlacesMenu: enlaces contenidos en el menú (NodeList).
 * - menuAbierto: estado actual del menú (boolean).
 *
 * FUNCIONES:
 * - actualizarEstadoMenu(abierto): sincroniza el estado visual y accesible.
 * - obtenerElementosEnfocables(): lista los elementos que admiten foco.
 * - abrirMenu(): abre el menú en la vista móvil.
 * - cerrarMenu(restaurarFoco): cierra el menú y gestiona el foco.
 *
 * TECNOLOGÍAS:
 * - JavaScript (ECMAScript).
 * - API del DOM (Document Object Model).
 * - API matchMedia del navegador.
 * - Atributos y propiedades de accesibilidad HTML.
 *
 * DEPENDENCIAS:
 * - index.html: página de inicio donde se carga este script.
 * - Elemento de navegación con el identificador "inicio-navegacion".
 * - Botón de navegación identificado mediante "[data-menu-boton]".
 * - inicio.css: estilos que contemplen el breakpoint de 800 px y la clase "abierto".
 *
 * OBSERVACIONES:
 * El breakpoint utilizado en JavaScript debe coincidir con el definido
 * en las hojas de estilo CSS para garantizar un comportamiento uniforme.
 */

document.addEventListener("DOMContentLoaded", () => {

    /* 1. REFERENCIAS A LOS ELEMENTOS DEL DOCUMENTO

       Se obtienen los elementos HTML necesarios para controlar la navegación:
       - menu: contenedor principal de los enlaces de navegación.
       - botonMenu: botón encargado de abrir y cerrar el menú móvil.
       */

    const menu = document.getElementById("inicio-navegacion");
    const botonMenu = document.querySelector("[data-menu-boton]");

    /* Se verifica que los elementos requeridos existan en el documento.
       Si alguno no está disponible, se registra un mensaje en la consola
       y se detiene la ejecución para evitar errores posteriores. */

    if (!menu || !botonMenu) {
        console.error("No se encontró el menú o el botón de navegación.");
        return;
    }

    /* 2. CONFIGURACIÓN DEL COMPORTAMIENTO RESPONSIVE

       Se establece el punto de interrupción (breakpoint) que determina
       cuándo se debe utilizar el comportamiento del menú para móviles.

       El valor de 800 px debe coincidir con el breakpoint definido en CSS.

       También se recopilan los enlaces del menú para poder gestionar
       el cierre automático cuando el usuario selecciona una opción.
       */

    const breakpointMovil = window.matchMedia("(max-width: 800px)");
    const enlacesMenu = menu.querySelectorAll("a[href]");

    /* Variable que almacena el estado actual del menú.
       - false: menú cerrado.
       - true: menú abierto. */

    let menuAbierto = false;

    /* 3. ACTUALIZACIÓN DEL ESTADO DEL MENÚ

       Actualiza el estado interno, visual y accesible del menú.

       Parámetro:
       abierto: valor booleano que indica si el menú debe permanecer abierto.

       Se modifican:
       - aria-expanded: comunica el estado del menú a tecnologías de asistencia.
       - aria-label: describe la acción disponible en el botón.
       - clase "abierto": permite aplicar los estilos correspondientes desde CSS.
       */

    function actualizarEstadoMenu(abierto) {
        menuAbierto = abierto;

        botonMenu.setAttribute("aria-expanded", String(abierto));
        botonMenu.setAttribute(
            "aria-label",
            abierto ? "Cerrar menú de navegación" : "Abrir menú de navegación"
        );

        menu.classList.toggle("abierto", abierto);
    }

    /* 4. IDENTIFICACIÓN DE ELEMENTOS ENFOCABLES

       Obtiene los elementos del menú que pueden recibir el foco mediante
       la navegación por teclado.

       Se consideran enlaces, botones habilitados, campos de formulario
       y elementos que tengan un tabindex válido.

       Posteriormente, se excluyen los elementos ocultos o que no sean
       visibles en pantalla.

       Retorno:
       Array con los elementos disponibles para recibir el foco.
       */

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

    /* 5. APERTURA DEL MENÚ

       Abre el menú cuando el usuario se encuentra en una resolución móvil.

       Antes de mostrarlo:
       - Comprueba que el breakpoint móvil esté activo.
       - Evita ejecutar nuevamente la acción si el menú ya está abierto.
       - Elimina las restricciones de visibilidad e interacción.
       - Actualiza el estado visual y accesible.

       Finalmente, traslada el foco al primer enlace disponible para
       facilitar la navegación mediante teclado.
       */

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

    /* 6. CIERRE DEL MENÚ

       Cierra el menú y restablece su estado visual y accesible.

       Parámetro:
       restaurarFoco: determina si el foco del teclado debe regresar
       al botón que controla la navegación. Su valor predeterminado es true.

       En dispositivos móviles:
       - Se oculta el menú.
       - Se impide la interacción con sus elementos mediante inert.

       Si se solicita restaurar el foco, este se devuelve al botón de menú.
       */

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

    /* 7. EVENTO DEL BOTÓN DE NAVEGACIÓN

       Se establece el tipo del botón para evitar que actúe como un botón
       de envío de formulario cuando se encuentre dentro de uno.

       El evento click alterna el estado del menú:
       - Si está abierto, lo cierra.
       - Si está cerrado, lo abre.

       Esta interacción solamente se ejecuta cuando está activo
       el breakpoint correspondiente a dispositivos móviles.
       */

    botonMenu.type = "button";

    botonMenu.addEventListener("click", () => {
        if (!breakpointMovil.matches) return;

        if (menuAbierto) {
            cerrarMenu();
        } else {
            abrirMenu();
        }
    });

    /* 8. CIERRE AUTOMÁTICO AL SELECCIONAR UN ENLACE

       Se agrega un evento click a cada enlace de navegación.

       Cuando el usuario selecciona un enlace mientras el menú móvil
       está abierto, este se cierra automáticamente.

       Se utiliza false para evitar devolver el foco al botón, ya que
       el usuario está realizando una acción de navegación.
       */

    enlacesMenu.forEach(enlace => {
        enlace.addEventListener("click", () => {
            if (breakpointMovil.matches && menuAbierto) {
                cerrarMenu(false);
            }
        });
    });

    /* 9. NAVEGACIÓN MEDIANTE TECLADO

       Se controla la tecla Tab mientras el menú móvil permanece abierto.

       El objetivo es mantener el recorrido del foco dentro del menú:
       - Al llegar al último elemento y pulsar Tab, el foco vuelve al primero.
       - Al estar en el primer elemento y pulsar Shift + Tab, el foco pasa
         al último elemento.

       De esta manera, se evita que la navegación por teclado salga
       accidentalmente del menú mientras está abierto.
       */

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

    /* 10. CIERRE MEDIANTE LA TECLA ESCAPE

       Se escucha el evento keydown en todo el documento.

       Si el usuario presiona Escape mientras el menú móvil está abierto:
       - Se evita el comportamiento predeterminado del evento.
       - Se cierra el menú.
       - Se devuelve el foco al botón de navegación.
       */

    document.addEventListener("keydown", event => {
        if (event.key === "Escape" && breakpointMovil.matches && menuAbierto) {
            event.preventDefault();
            cerrarMenu();
        }
    });

    /* 11. CIERRE AL HACER CLIC FUERA DEL MENÚ

       Se detectan los clics realizados en cualquier parte del documento.

       El menú se cierra cuando se cumplen todas estas condiciones:
       - La pantalla se encuentra en el breakpoint móvil.
       - El menú está abierto.
       - El clic no ocurrió dentro del menú.
       - El clic tampoco ocurrió sobre el botón de navegación.

       Se utiliza false para no desplazar el foco al botón después
       de una interacción realizada fuera del menú.
       */

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

    /* 12. ADAPTACIÓN CUANDO CAMBIA EL TAMAÑO DE PANTALLA

       Se escucha el cambio del breakpoint para adaptar el menú cuando
       el usuario cambia entre una resolución móvil y una de escritorio.

       Cuando se activa el breakpoint móvil:
       - Se restablece el estado cerrado.
       - Se oculta el menú.
       - Se impide la interacción con sus elementos.
       - Si el foco estaba dentro del menú, se devuelve al botón.

       Cuando se abandona el breakpoint móvil:
       - El menú vuelve a mostrarse.
       - Se habilita nuevamente la interacción con sus elementos.
       */

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

    /* 13. CONFIGURACIÓN DEL ESTADO INICIAL

       Se determina cómo debe mostrarse el menú al cargar la página,
       dependiendo de la resolución actual del dispositivo.

       En dispositivos móviles:
       - El menú comienza oculto.
       - Sus elementos no permiten interacción.

       En dispositivos de escritorio:
       - El menú permanece visible.
       - Sus elementos están habilitados para la interacción.

       Finalmente, se establece el estado inicial como cerrado para
       mantener sincronizados el botón y el contenedor de navegación.
       */

    if (breakpointMovil.matches) {
        menu.hidden = true;
        menu.inert = true;
    } else {
        menu.hidden = false;
        menu.inert = false;
    }

    actualizarEstadoMenu(false);
});