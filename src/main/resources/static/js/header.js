/*
    PROYECTO: Feeling
    ARCHIVO: header.js
    UBICACIÓN: src/main/resources/static/js/header.js
    MÓDULO: Plantilla base, encabezado

    AUTOR: Yulipaola Domínguez
    VERSIÓN: 1.1.0
    FECHA DE DOCUMENTACIÓN: 2026-11-24
    ESTADO: Finalizado
    TICKET: SCRUM-43

    DESCRIPCIÓN:
    Script que controla el menú desplegable del perfil de usuario en el
    encabezado de las páginas internas. Abre y cierra el menú al hacer
    clic en el botón de perfil, lo cierra al hacer clic fuera de él o al
    presionar la tecla Escape, y mantiene sincronizado el atributo ARIA
    aria-expanded para los lectores de pantalla.

    FUNCIONALIDADES:
    - Abrir y cerrar el menú al hacer clic en el botón de perfil.
    - Cerrar el menú al hacer clic en cualquier zona fuera del perfil.
    - Cerrar el menú con la tecla Escape y devolver el foco al botón.
    - Actualizar aria-expanded según el estado del menú.

    TECNOLOGÍAS:
    - JavaScript (ES6): funciones, constantes y funciones flecha.
    - DOM API: querySelector, addEventListener, closest.
    - ARIA: accesibilidad del menú desplegable.

    DEPENDENCIAS:
    - templates/fragments/header.html: define los elementos
      #profile-menu-button, #profile-menu y .header-profile.
    - /css/interno.css: estilos del encabezado y del menú.

    ELEMENTOS DEL DOM UTILIZADOS:
    - #profile-menu-button: botón que abre y cierra el menú.
    - #profile-menu: menú desplegable (usa el atributo hidden).
    - .header-profile: contenedor del botón y del menú; sirve para
      detectar si un clic ocurrió dentro o fuera del perfil.

    HISTORIAL DE VERSIONES:
    - 1.0.0: implementación inicial del menú de perfil (SCRUM-43).
    - 1.1.0 (2026-11-24): documentación completa. No se modificó el
      código.

    OBSERVACIONES:
    - Si el botón o el menú no existen en la página, el script termina
      sin generar errores.
    - El menú se muestra y se oculta con el atributo hidden; no se
      manipulan clases CSS.
    - El script debe cargarse en las páginas que incluyen el fragmento
      header.html.
*/

/* Espera a que el HTML termine de cargarse para poder encontrar los
   elementos del encabezado antes de usarlos. */
document.addEventListener("DOMContentLoaded", () => {

    /* Botón de perfil: al hacer clic abre o cierra el menú. */
    const profileButton = document.querySelector(
        "#profile-menu-button"
    );

    /* Menú desplegable: contiene la opción de cerrar sesión. */
    const profileMenu = document.querySelector(
        "#profile-menu"
    );

    /* Protección: si la página no tiene el encabezado (o falta alguno de
       los dos elementos), se detiene el script para evitar errores. */
    if (!profileButton || !profileMenu) {
        return;
    }

    /* ABRIR O CERRAR EL MENÚ
       Invierte el estado actual: si está abierto lo cierra y si está
       cerrado lo abre. Además actualiza aria-expanded para informar el
       nuevo estado a los lectores de pantalla. */
    function toggleProfileMenu() {

        /* El menú está abierto cuando NO tiene el atributo hidden. */
        const isOpen = !profileMenu.hidden;

        /* Si estaba abierto, hidden pasa a true (se oculta); si estaba
           cerrado, pasa a false (se muestra). */
        profileMenu.hidden = isOpen;

        /* aria-expanded recibe el estado contrario al anterior, como
           texto ("true" o "false") porque los atributos son cadenas. */
        profileButton.setAttribute(
            "aria-expanded",
            String(!isOpen)
        );
    }

    /* CERRAR EL MENÚ
       Oculta el menú sin importar su estado previo y deja
       aria-expanded en "false". Se usa al hacer clic fuera o al
       presionar Escape. */
    function closeProfileMenu() {

        /* Oculta el menú desplegable. */
        profileMenu.hidden = true;

        /* Informa a los lectores de pantalla que el menú está cerrado. */
        profileButton.setAttribute(
            "aria-expanded",
            "false"
        );
    }

    /* EVENTO: CLIC EN EL BOTÓN DE PERFIL
       Abre el menú si está cerrado o lo cierra si está abierto. */
    profileButton.addEventListener(
        "click",
        toggleProfileMenu
    );

    /* EVENTO: CLIC FUERA DEL MENÚ
       Se escucha en todo el documento. closest(".header-profile") busca
       si el elemento pulsado, o alguno de sus padres, es el contenedor
       del perfil. Si no lo es, el clic fue afuera y se cierra el menú. */
    document.addEventListener("click", (event) => {

        if (!event.target.closest(".header-profile")) {
            closeProfileMenu();
        }
    });

    /* EVENTO: TECLA ESCAPE
       Con el menú abierto, Escape lo cierra. El foco vuelve al botón de
       perfil para que quien navega con teclado no pierda su posición. */
    document.addEventListener("keydown", (event) => {

        if (
            event.key === "Escape" &&
            !profileMenu.hidden
        ) {
            closeProfileMenu();
            profileButton.focus();
        }
    });

});
