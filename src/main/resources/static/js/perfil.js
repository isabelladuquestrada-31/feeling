/*
 * PROYECTO: Feeling
 * ARCHIVO: perfil.js
 * UBICACIÓN: src/main/resources/static/js/perfil.js
 * MÓDULO: Mi perfil (gestión del perfil y accesibilidad)
 *
 * AUTOR: Yulipaola Domínguez
 * VERSIÓN: 1.2.0
 * FECHA DE DOCUMENTACIÓN: 2026-11-25
 * ESTADO: Finalizado
 * TICKET: SCRUM-91 (RF-03), con ampliación de SCRUM-93 (RF-04)
 *
 * DESCRIPCIÓN:
 * Script de la pantalla perfil.html. Actualiza la vista previa del perfil,
 * controla el número de WhatsApp, cuenta los caracteres y valida el
 * formulario antes de enviarlo. También guarda las preferencias de
 * accesibilidad al mover sus interruptores.
 *
 * FUNCIONALIDADES:
 * - Actualizar la vista previa mientras se modifican los datos.
 * - Mostrar la inicial del nombre utilizado en el perfil.
 * - Contar los caracteres del nombre preferido.
 * - Permitir únicamente números en el campo de WhatsApp.
 * - Validar el nombre preferido y el número de contacto.
 * - Mostrar los errores junto a sus respectivos campos.
 * - Evitar el envío repetido del formulario.
 * - Preferencias de accesibilidad (RF-04): al mover un interruptor la
 *   página cambia de inmediato y luego se guarda la preferencia en la cuenta.
 * - Facilitar la navegación mediante el teclado y los lectores de pantalla.
 *
 * LO QUE NO HACE ESTE ARCHIVO:
 * Las alertas generales las maneja solicitudes.js. El servidor vuelve a
 * validar los datos como comprobación adicional.
 *
 * DEPENDENCIAS:
 * - perfil.html: aporta los elementos con atributos data-*.
 * - solicitudes.js: alertas y comportamientos compartidos.
 * - accesibilidad.css: estilos de las clases letra-grande y alto-contraste.
 *
 * ATRIBUTOS DATA-* UTILIZADOS:
 * - data-formulario-perfil: formulario principal; si no existe, esa parte no se ejecuta.
 * - data-contador-nombre y data-guardar: contador del nombre y botón Guardar.
 * - data-previa-nombre, data-previa-pronombres y data-previa-inicial: vista previa.
 * - data-formulario-accesibilidad y data-preferencia: preferencias de accesibilidad.
 * - data-aviso: zona para lectores de pantalla.
 *
 * HISTORIAL DE VERSIONES:
 * - 1.0.0: vista previa, validación y control del WhatsApp (SCRUM-91, RF-03).
 * - 1.1.0: guardado de las preferencias de accesibilidad (SCRUM-93, RF-04).
 * - 1.2.0 (2026-11-25): documentación completa. No cambia la lógica.
 */


// Espera a que el documento esté cargado para acceder a los elementos de la vista.
document.addEventListener("DOMContentLoaded", () => {

    // Obtiene el formulario principal del perfil mediante su atributo de identificación.
    const formulario = document.querySelector("[data-formulario-perfil]");

    // Si la vista no contiene este formulario, no continúa con la ejecución del código.
    if (!formulario) {
        return;
    }

    // Consulta si el usuario tiene activada la preferencia de movimiento reducido en su dispositivo.
    const reducirMovimiento = window.matchMedia("(prefers-reduced-motion: reduce)");

    // Elemento utilizado para comunicar mensajes dinámicos a los lectores de pantalla.
    const avisoLector = document.querySelector("[data-aviso]");

    // Referencias a los campos del formulario y a los elementos asociados con su funcionamiento.
    const campoNombre = formulario.querySelector("#nombrePreferido");
    const campoPronombres = formulario.querySelector("#pronombres");
    const campoWhatsapp = formulario.querySelector("#whatsapp");
    const contadorNombre = formulario.querySelector("[data-contador-nombre]");
    const botonGuardar = formulario.querySelector("[data-guardar]");

    // Elementos de la tarjeta lateral que muestran la vista previa del perfil.
    const previaNombre = document.querySelector("[data-previa-nombre]");
    const previaPronombres = document.querySelector("[data-previa-pronombres]");
    const previaInicial = document.querySelector("[data-previa-inicial]");

    // Nombre de usuario: se muestra en la vista previa si no hay nombre preferido.
    const nombreUsuario = formulario.querySelector(".perfil__tarjeta-cabecera strong");
    const nombrePorDefecto = nombreUsuario ? nombreUsuario.textContent.trim() : "";

    // Cantidad máxima de caracteres permitidos para el nombre preferido.
    const MAXIMO_NOMBRE = 50;

    /*
     * COMUNICACIÓN CON LECTORES DE PANTALLA
     * Actualiza el contenido de la región accesible para anunciar mensajes
     * relacionados con la validación y el envío del formulario.
     */
    function anunciar(mensaje) {
        if (avisoLector) {
            avisoLector.textContent = mensaje;
        }
    }

    /*
     * 1. VISTA PREVIA EN VIVO
     * Actualiza la tarjeta lateral a medida que el estudiante modifica
     * su nombre preferido o selecciona otros pronombres.
     */
    function actualizarVistaPrevia() {
        // Si el nombre preferido está vacío, utiliza el nombre de usuario como alternativa.
        const nombre = campoNombre.value.trim() || nombrePorDefecto;
        const pronombres = campoPronombres.value;

        // Actualiza el nombre que aparece en la tarjeta de vista previa.
        if (previaNombre) {
            previaNombre.textContent = nombre;
        }

        // Muestra los pronombres entre paréntesis, excepto cuando no se selecciona una opción
        // o se elige "Prefiero no decir".
        if (previaPronombres) {
            previaPronombres.textContent = pronombres && pronombres !== "Prefiero no decir"
                ? `(${pronombres})`
                : "";
        }

        // Actualiza la inicial del avatar cuando cambia el nombre mostrado.
        if (previaInicial) {
            const inicial = nombre.charAt(0).toUpperCase();
            if (previaInicial.textContent !== inicial) {
                previaInicial.textContent = inicial;

                // Aplica una animación breve únicamente si el usuario no ha solicitado
                // reducir el movimiento en su dispositivo.
                if (!reducirMovimiento.matches) {
                    previaInicial.classList.add("cambio");
                    setTimeout(() => previaInicial.classList.remove("cambio"), 250);
                }
            }
        }
    }

    /*
     * 2. CONTADOR DEL NOMBRE PREFERIDO
     * Mantiene actualizado el número de caracteres escritos y cambia
     * la clase visual cuando el texto se acerca al límite permitido.
     */
    function actualizarContador() {
        if (!contadorNombre) return;

        const largo = campoNombre.value.length;
        contadorNombre.textContent = `${largo}/${MAXIMO_NOMBRE}`;
        contadorNombre.classList.toggle("cerca", largo >= MAXIMO_NOMBRE - 5);
    }

    /*
     * 3. MOSTRAR Y QUITAR ERRORES DE UN CAMPO
     * Estas funciones centralizan la presentación de los errores para
     * evitar repetir las mismas instrucciones en cada validación.
     */

    // Agrega los estilos de error, actualiza los atributos de accesibilidad
    // y presenta el mensaje correspondiente al campo indicado.
    function mostrarError(nombreCampo, mensaje) {
        const contenedor = formulario.querySelector(`[data-campo="${nombreCampo}"]`);
        const control = contenedor.querySelector(".campo__control");
        const error = contenedor.querySelector(".campo__error");

        contenedor.classList.add("campo--error");
        control.setAttribute("aria-invalid", "true");
        error.textContent = mensaje;
        error.hidden = false;

        // Aplica un efecto visual al campo marcado, respetando la preferencia
        // del usuario sobre el movimiento de la interfaz.
        if (!reducirMovimiento.matches) {
            contenedor.classList.remove("recien-marcado");
            void contenedor.offsetWidth;
            contenedor.classList.add("recien-marcado");
        }
    }

    // Retira el estado de error del campo cuando deja de presentar una validación pendiente.
    function quitarError(nombreCampo) {
        const contenedor = formulario.querySelector(`[data-campo="${nombreCampo}"]`);
        const control = contenedor.querySelector(".campo__control");
        const error = contenedor.querySelector(".campo__error");

        contenedor.classList.remove("campo--error", "recien-marcado");
        control.setAttribute("aria-invalid", "false");
        error.textContent = "";
        error.hidden = true;
    }

    /*
     * 4. VALIDACIONES DEL FORMULARIO
     * Comprueba los datos principales antes del envío.
     * Las reglas siguen las validaciones realizadas por PerfilService.validar
     * en el servidor, que vuelve a comprobar la información recibida.
     */

    // Verifica que el WhatsApp no esté vacío y que tenga 10 dígitos
    // comenzando por el número 3, según el formato de celular utilizado.
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

    // Comprueba que el nombre preferido no supere los 50 caracteres.
    function validarNombre() {
        return campoNombre.value.trim().length > MAXIMO_NOMBRE
            ? "El nombre preferido no puede tener más de 50 caracteres."
            : null;
    }

    /*
     * 5. EVENTOS DE LOS CAMPOS
     * Escucha los cambios realizados por el estudiante para actualizar
     * la información de la pantalla y retirar errores cuando corresponda.
     */

    // Al escribir en el nombre preferido, actualiza el contador y la vista previa.
    // Si el nombre cumple con la longitud permitida, elimina su mensaje de error.
    campoNombre.addEventListener("input", () => {
        actualizarContador();
        actualizarVistaPrevia();
        if (!validarNombre()) quitarError("nombrePreferido");
    });

    // Actualiza los pronombres mostrados en la vista previa cuando cambia la selección.
    // También retira cualquier error asociado a este campo.
    campoPronombres.addEventListener("change", () => {
        actualizarVistaPrevia();
        quitarError("pronombres");
    });

    // El WhatsApp solo acepta números (también al pegar).
    campoWhatsapp.addEventListener("input", () => {
        // Elimina cualquier carácter que no sea un dígito y limita el resultado a 10 números.
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

    /*
     * 6. ENVÍO DEL FORMULARIO
     * Comprueba los datos antes de enviarlos y evita que el estudiante
     * realice varios envíos mientras se procesa la solicitud.
     */
    let enviando = false;

    formulario.addEventListener("submit", evento => {
        // Si ya se inició un envío, cancela cualquier intento adicional.
        if (enviando) {
            evento.preventDefault();
            return;
        }

        // Ejecuta las validaciones del nombre preferido y del número de WhatsApp.
        const errorNombre = validarNombre();
        const errorWhatsapp = validarWhatsapp();

        // Muestra los errores encontrados junto a los campos correspondientes.
        if (errorNombre) mostrarError("nombrePreferido", errorNombre);
        if (errorWhatsapp) mostrarError("whatsapp", errorWhatsapp);

        // Si algún dato no cumple las condiciones, evita el envío
        // y lleva el foco al primer campo que necesita corrección.
        if (errorNombre || errorWhatsapp) {
            evento.preventDefault();
            const primerError = formulario.querySelector(".campo--error .campo__control");
            if (primerError) primerError.focus();
            anunciar("Hay campos por corregir.");
            return;
        }

        // Marca el formulario como enviado, desactiva el botón y muestra
        // un indicador visual mientras se procesa la actualización.
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

    /*
     * 7. ERRORES DEVUELTOS POR EL SERVIDOR
     * Cuando la página vuelve a cargarse con errores de validación,
     * identifica el primer campo marcado y coloca allí el foco para
     * facilitar su corrección.
     */
    const primerErrorServidor = formulario.querySelector(".campo--error .campo__control");
    if (primerErrorServidor) {
        primerErrorServidor.focus();
    }

    /*
     * 8. ESTADO INICIAL
     * Ejecuta las actualizaciones al cargar la vista para que el contador
     * y la tarjeta de vista previa coincidan con los datos que recibió
     * el formulario desde el servidor.
     */
    actualizarContador();
    actualizarVistaPrevia();
});

/*
   Preferencias de accesibilidad (RF-04)
   - Al mover un interruptor, la página cambia de inmediato (clase en <html>).
   - Luego se envía el formulario para guardar la preferencia en la cuenta.
   - Si el servidor no puede guardarla, la página vuelve a cargar con las
     preferencias anteriores y muestra el aviso.
*/
document.addEventListener("DOMContentLoaded", () => {
    // Formulario de preferencias. Si la vista no lo tiene, no se hace nada.
    const formulario = document.querySelector("[data-formulario-accesibilidad]");

    if (!formulario) {
        return;
    }

    // Zona de avisos, interruptores y marca para no enviar dos veces.
    const avisoLector = document.querySelector("[data-aviso]");
    const interruptores = formulario.querySelectorAll("input[data-preferencia]");
    let enviando = false;

    // Cada interruptor aplica su preferencia y la guarda al cambiar.
    interruptores.forEach(interruptor => {
        interruptor.addEventListener("change", () => {
            if (enviando) return;

            // 1. Cambio inmediato en la página
            const clase = interruptor.dataset.preferencia;
            document.documentElement.classList.toggle(clase, interruptor.checked);

            // Se anuncia el cambio a los lectores de pantalla.
            const nombre = interruptor.closest(".interruptor").querySelector("strong").textContent;
            if (avisoLector) {
                avisoLector.textContent = `${nombre} ${interruptor.checked ? "activado" : "desactivado"}. Guardando.`;
            }

            // 2. Guardar en la cuenta (pequeña pausa para que se vea el interruptor moverse)
            enviando = true;
            formulario.classList.add("guardando");
            setTimeout(() => formulario.submit(), 300);
        });
    });

    // Si el usuario vuelve con el botón "atrás", los interruptores quedan listos otra vez.
    window.addEventListener("pageshow", () => {
        enviando = false;
        formulario.classList.remove("guardando");
    });
});
