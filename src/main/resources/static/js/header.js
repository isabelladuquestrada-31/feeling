
document.addEventListener("DOMContentLoaded", () => {

    const profileButton = document.querySelector(
        "#profile-menu-button"
    );

    const profileMenu = document.querySelector(
        "#profile-menu"
    );

    if (!profileButton || !profileMenu) {
        return;
    }

    // Abrir o cerrar el menú
    function toggleProfileMenu() {

        const isOpen = !profileMenu.hidden;

        profileMenu.hidden = isOpen;

        profileButton.setAttribute(
            "aria-expanded",
            String(!isOpen)
        );
    }

    // Cerrar el menú
    function closeProfileMenu() {

        profileMenu.hidden = true;

        profileButton.setAttribute(
            "aria-expanded",
            "false"
        );
    }

    // Clic en la imagen de perfil
    profileButton.addEventListener(
        "click",
        toggleProfileMenu
    );

    // Clic fuera del menú
    document.addEventListener("click", (event) => {

        if (!event.target.closest(".header-profile")) {
            closeProfileMenu();
        }
    });

    // Cerrar con Escape
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
