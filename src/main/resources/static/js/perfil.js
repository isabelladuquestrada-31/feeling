/*
   Mi perfil (RF-03)
   - Vista previa en vivo del nombre preferido y los pronombres.
   - El WhatsApp solo acepta números.
   - Contador de caracteres del nombre preferido.
   - Revisa los campos antes de enviar (el servidor vuelve a revisarlos).
   - Evita que el formulario se envíe dos veces.
   Las alertas y el texto para lectores de pantalla los maneja solicitudes.js.
*/
document.addEventListener("DOMContentLoaded", () => {
    const formulario = document.querySelector("[data-formulario-perfil]");

    if (!formulario) {
        return;
    }

    const reducirMovimiento = window.matchMedia("(prefers-reduced-motion: reduce)");
    const avisoLector = document.querySelector("[data-aviso]");

    const campoNombre = formulario.querySelector("#nombrePreferido");
    const campoPronombres = formulario.querySelector("#pronombres");
    const campoWhatsapp = formulario.querySelector("#whatsapp");
    const contadorNombre = formulario.querySelector("[data-contador-nombre]");
    const botonGuardar = formulario.querySelector("[data-guardar]");

    const previaNombre = document.querySelector("[data-previa-nombre]");
    const previaPronombres = document.querySelector("[data-previa-pronombres]");
    const previaInicial = document.querySelector("[data-previa-inicial]");

    // Nombre de usuario: se muestra en la vista previa si no hay nombre preferido.
    const nombreUsuario = formulario.querySelector(".perfil__tarjeta-cabecera strong");
    const nombrePorDefecto = nombreUsuario ? nombreUsuario.textContent.trim() : "";

    const MAXIMO_NOMBRE = 50;

    function anunciar(mensaje) {
        if (avisoLector) {
            avisoLector.textContent = mensaje;
        }
    }

    // 1. Vista previa en vivo
    function actualizarVistaPrevia() {
        const nombre = campoNombre.value.trim() || nombrePorDefecto;
        const pronombres = campoPronombres.value;

        if (previaNombre) {
            previaNombre.textContent = nombre;
        }

        if (previaPronombres) {
            previaPronombres.textContent = pronombres && pronombres !== "Prefiero no decir"
                ? `(${pronombres})`
                : "";
        }

        if (previaInicial) {
            const inicial = nombre.charAt(0).toUpperCase();
            if (previaInicial.textContent !== inicial) {
                previaInicial.textContent = inicial;
                if (!reducirMovimiento.matches) {
                    previaInicial.classList.add("cambio");
                    setTimeout(() => previaInicial.classList.remove("cambio"), 250);
                }
            }
        }
    }

    // 2. Contador del nombre preferido
    function actualizarContador() {
        if (!contadorNombre) return;

        const largo = campoNombre.value.length;
        contadorNombre.textContent = `${largo}/${MAXIMO_NOMBRE}`;
        contadorNombre.classList.toggle("cerca", largo >= MAXIMO_NOMBRE - 5);
    }

    // 3. Mostrar y quitar errores de un campo
    function mostrarError(nombreCampo, mensaje) {
        const contenedor = formulario.querySelector(`[data-campo="${nombreCampo}"]`);
        const control = contenedor.querySelector(".campo__control");
        const error = contenedor.querySelector(".campo__error");

        contenedor.classList.add("campo--error");
        control.setAttribute("aria-invalid", "true");
        error.textContent = mensaje;
        error.hidden = false;

        if (!reducirMovimiento.matches) {
            contenedor.classList.remove("recien-marcado");
            void contenedor.offsetWidth;
            contenedor.classList.add("recien-marcado");
        }
    }

    function quitarError(nombreCampo) {
        const contenedor = formulario.querySelector(`[data-campo="${nombreCampo}"]`);
        const control = contenedor.querySelector(".campo__control");
        const error = contenedor.querySelector(".campo__error");

        contenedor.classList.remove("campo--error", "recien-marcado");
        control.setAttribute("aria-invalid", "false");
        error.textContent = "";
        error.hidden = true;
    }

    // 4. Las mismas reglas del servidor (PerfilService.validar)
    function validarWhatsapp() {
        const numero = campoWhatsapp.value.trim();

        if (numero === "") {
            return "El número de WhatsApp es obligatorio.";
        }
        if (!/^3\d{9}$/.test(numero)) {
            return "Escribe un celular de 10 dígitos que empiece por 3.";
        }
        return null;
    }

    function validarNombre() {
        return campoNombre.value.trim().length > MAXIMO_NOMBRE
            ? "El nombre preferido no puede tener más de 50 caracteres."
            : null;
    }

    // 5. Eventos de los campos
    campoNombre.addEventListener("input", () => {
        actualizarContador();
        actualizarVistaPrevia();
        if (!validarNombre()) quitarError("nombrePreferido");
    });

    campoPronombres.addEventListener("change", () => {
        actualizarVistaPrevia();
        quitarError("pronombres");
    });

    // El WhatsApp solo acepta números (también al pegar).
    campoWhatsapp.addEventListener("input", () => {
        const soloNumeros = campoWhatsapp.value.replace(/\D/g, "").slice(0, 10);
        if (campoWhatsapp.value !== soloNumeros) {
            campoWhatsapp.value = soloNumeros;
        }
        if (!validarWhatsapp()) quitarError("whatsapp");
    });

    // Al salir del campo se revisa, para avisar antes de enviar.
    campoWhatsapp.addEventListener("blur", () => {
        const error = validarWhatsapp();
        if (error && campoWhatsapp.value !== "") mostrarError("whatsapp", error);
    });

    // 6. Envío del formulario
    let enviando = false;

    formulario.addEventListener("submit", evento => {
        if (enviando) {
            evento.preventDefault();
            return;
        }

        const errorNombre = validarNombre();
        const errorWhatsapp = validarWhatsapp();

        if (errorNombre) mostrarError("nombrePreferido", errorNombre);
        if (errorWhatsapp) mostrarError("whatsapp", errorWhatsapp);

        if (errorNombre || errorWhatsapp) {
            evento.preventDefault();
            const primerError = formulario.querySelector(".campo--error .campo__control");
            if (primerError) primerError.focus();
            anunciar("Hay campos por corregir.");
            return;
        }

        enviando = true;
        botonGuardar.disabled = true;
        botonGuardar.innerHTML = '<span class="boton-accion__cargando" aria-hidden="true"></span>Guardando...';
        anunciar("Guardando tus datos.");
    });

    // Si el usuario vuelve con el botón "atrás", el botón queda listo otra vez.
    window.addEventListener("pageshow", () => {
        enviando = false;
        botonGuardar.disabled = false;
        botonGuardar.innerHTML = '<span data-guardar-texto>Guardar cambios</span>';
    });

    // 7. Si el servidor devolvió errores, lleva el foco al primer campo con error.
    const primerErrorServidor = formulario.querySelector(".campo--error .campo__control");
    if (primerErrorServidor) {
        primerErrorServidor.focus();
    }

    actualizarContador();
    actualizarVistaPrevia();
});
