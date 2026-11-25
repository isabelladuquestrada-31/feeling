
/*
   PROYECTO: Feeling
   ARCHIVO: solicitar-apoyo.js
   UBICACIÓN: src/main/resources/static/js/solicitar-apoyo.js
   MÓDULO: Solicitar apoyo (panel de voluntarios)

   AUTOR: Yulipaola Domínguez
   VERSIÓN: 1.1.0
   FECHA DE DOCUMENTACIÓN: 2026-11-25
   ESTADO: Finalizado
   TICKET: SCRUM-81 (RF-08)
   REQUISITO RELACIONADO: SCRUM-95 (RF-09)

   DESCRIPCIÓN:
   Controla el comportamiento del formulario que utiliza un estudiante
   para solicitar el acompañamiento de un voluntario.

   - Actualiza el estado de cada paso mientras el estudiante completa
     la información.
   - Muestra en el resumen los datos que se van seleccionando o escribiendo.
   - Actualiza la barra de progreso según los cinco pasos obligatorios.
   - Cuenta los caracteres del tema y muestra una indicación cuando
     se acerca al límite permitido.
   - Revisa que los pasos obligatorios estén completos antes de enviar
     el formulario, para mostrar los errores sin recargar la página.
   - Permite seleccionar ajustes razonables y, cuando se elige "Otro",
     muestra un campo para escribir la descripción.
   - No permite enviar el formulario si se seleccionó "Otro" y no se
     escribió el detalle correspondiente.
   - Evita el doble envío mientras se procesa la solicitud.
   - Permite regresar a la página anterior cuando la navegación viene
     de otra página del mismo sitio.

   Los mensajes de error y éxito que devuelve el servidor, la animación
   general de los botones y el cierre de las alertas los maneja
   solicitudes.js.

   ATRIBUTOS DE INTERACCIÓN (definidos en solicitar-apoyo.html):
   - data-solicitar: identifica la sección principal de esta pantalla.
   - data-formulario-solicitud: identifica el formulario de solicitud.
   - data-aviso: región que anuncia mensajes a lectores de pantalla.
   - data-campo-tema: campo donde se escribe el tema de la solicitud.
   - data-contador-tema y data-caracteres: elementos del contador del tema.
   - data-progreso: barra que representa el avance de los pasos.
   - data-pasos-listos: muestra cuántos pasos obligatorios están completos.
   - data-paso: identifica cada uno de los pasos del formulario.
   - data-error: identifica el mensaje de error de cada paso.
   - data-resumen: identifica el dato correspondiente en el resumen.
   - data-texto: texto que se muestra para cada opción seleccionable.
   - data-ajuste: identifica las casillas de ajustes razonables.
   - data-campo-otro: contenedor del campo para describir el ajuste "Otro".
   - data-detalle-otro: campo donde se escribe la descripción del ajuste.
   - data-caracteres-ajuste: muestra los caracteres escritos en ese campo.
   - data-contador-ajuste: identifica el contador de la descripción.
   - data-enviar: identifica el botón que envía la solicitud.
   - data-enviar-texto: identifica el texto que cambia mientras se envía.
   - data-volver: identifica el enlace o botón para regresar.

   CLASES CSS QUE AGREGA O QUITA (definidas en los estilos de la vista):
   - completo: indica que un paso tiene la información necesaria.
   - pendiente: identifica un dato que todavía no se ha completado
     en el resumen.
   - actualizado: permite mostrar el cambio de un dato en el resumen.
   - elegida: resalta la opción que seleccionó el estudiante.
   - con-error: identifica visualmente un paso que tiene información faltante.
   - cerca: indica que el contador se está acercando al límite de caracteres.
   - pop: activa la animación de selección de una opción.
   - abriendo: activa la animación al mostrar el campo de "Otro".
   - boton-accion__cargando: elemento que se muestra dentro del botón
     mientras se envía la solicitud.

   VARIABLES PRINCIPALES:
   - reducirMovimiento: consulta si el usuario tiene activada la preferencia
     del sistema para reducir las animaciones.
   - avisoLector: región utilizada para comunicar mensajes a lectores
     de pantalla.
   - campoTema: campo donde se escribe el tema de la solicitud.
   - contadorTema, caracteres: elementos que muestran el avance del
     contador del tema.
   - progreso: barra visual que representa el avance del formulario.
   - pasosListos: número de pasos obligatorios completados.
   - botonEnviar, textoEnviar: botón de envío y texto que contiene.
   - PASOS: nombres de los cinco pasos que se deben completar.
   - TEXTOS_VACIOS: mensajes que aparecen en el resumen cuando todavía
     no se ha elegido o escrito un dato.
   - formulario: formulario sobre el que se realizan las validaciones.
   - casillasAjuste: lista de opciones de ajustes razonables.
   - campoOtro: contenedor que aparece cuando se selecciona "Otro".
   - detalleOtro: campo donde se describe el ajuste seleccionado.
   - caracteresAjuste, contadorAjuste: elementos del contador del ajuste.

   FUNCIONES:
   - anunciar(mensaje): comunica un mensaje a los lectores de pantalla.
   - valorDe(paso): obtiene el valor elegido o escrito en un paso.
   - actualizarPaso(paso): actualiza el estado visual del paso y su
     información en el resumen.
   - actualizarProgreso(): calcula y actualiza el avance de los cinco
     pasos obligatorios.
   - mostrarError(paso): muestra el mensaje de error de un paso.
   - quitarError(paso): oculta el mensaje de error de un paso.
   - actualizarContadorTema(): actualiza el número de caracteres del tema.
   - otroMarcado(): comprueba si el estudiante seleccionó la opción "Otro".
   - otroSinDetalle(): comprueba si se seleccionó "Otro" sin escribir
     una descripción.
   - actualizarContadorAjuste(): actualiza el contador de caracteres
     de la descripción del ajuste.
   - actualizarAjustes(): actualiza las opciones seleccionadas, muestra
     u oculta el campo de "Otro" y modifica el resumen de ajustes.

   EVENTOS PRINCIPALES:
   - change: se utiliza para detectar la selección de materias, tipos
     de apoyo, modalidades, horarios y ajustes razonables.
   - input: actualiza los contadores y el resumen mientras se escribe.
   - keydown: evita que la tecla Enter envíe el formulario desde el
     campo del tema antes de completar los demás pasos.
   - submit: revisa los campos obligatorios y la descripción de "Otro"
     antes de permitir el envío.
   - click: permite regresar a la página anterior cuando corresponde.

   HISTORIAL DE VERSIONES:
   - 1.0.0: implementación del comportamiento de la solicitud de apoyo
     (SCRUM-81, RF-08).
   - 1.1.0 (2026-11-25): documentación del archivo y explicación de
     sus elementos principales. No se modifica la lógica.

   OBSERVACIONES:
   - Los cinco pasos que cuentan para el progreso son materia, tema,
     tipo de apoyo, horario y modalidad. Los ajustes razonables son
     opcionales y no aumentan ni disminuyen ese porcentaje.
   - Los valores del resumen se obtienen de los campos del formulario
     y de los atributos data-texto definidos en el HTML.
   - La validación de este archivo se realiza antes de enviar el
     formulario. El servidor también debe validar la información
     recibida.
   - La opción "Otro" solo requiere una descripción cuando está marcada.
     Si se desmarca, deja de ser obligatorio escribir ese detalle.
   - La preferencia prefers-reduced-motion se tiene en cuenta para evitar
     algunas animaciones cuando el usuario ha solicitado reducir el
     movimiento.
   - El comportamiento de esta pantalla depende de los atributos data-*
     que se encuentran en solicitar-apoyo.html. Si se cambian esos
     atributos, también se deben revisar los selectores de este archivo.
   - Las animaciones y los estilos visuales utilizados aquí deben
     coincidir con las clases definidas en solicitar-apoyo.css.
   - El archivo solicitudes.js se encarga de otros comportamientos
     compartidos por las pantallas de solicitudes.

*/

document.addEventListener("DOMContentLoaded", () => {
    "use strict";

    // Consulta la preferencia del sistema para saber si se deben reducir las animaciones.
    const reducirMovimiento = window.matchMedia("(prefers-reduced-motion: reduce)");

    // Elementos principales que se actualizan mientras se completa el formulario.
    const seccion = document.querySelector("[data-solicitar]");
    const formulario = seccion ? seccion.querySelector("[data-formulario-solicitud]") : null;

    // Si esta pantalla no tiene el formulario, no hay nada que inicializar.
    if (!formulario) {
        return;
    }

    // Región que utiliza el lector de pantalla para anunciar los cambios.
    const avisoLector = seccion.querySelector("[data-aviso]");

    // Campo del tema y elementos que muestran su contador de caracteres.
    const campoTema = formulario.querySelector("[data-campo-tema]");
    const contadorTema = seccion.querySelector("[data-contador-tema]");
    const caracteres = seccion.querySelector("[data-caracteres]");

    // Elementos que muestran el avance de los pasos obligatorios.
    const progreso = seccion.querySelector("[data-progreso]");
    const pasosListos = seccion.querySelector("[data-pasos-listos]");

    // Botón de envío y texto que se reemplaza mientras se procesa la solicitud.
    const botonEnviar = formulario.querySelector("[data-enviar]");
    const textoEnviar = formulario.querySelector("[data-enviar-texto]");

    // Estos son los cinco pasos que el estudiante debe completar para enviar.
    // Los ajustes razonables se manejan aparte porque son opcionales.
    const PASOS = ["materia", "tema", "tipo", "horario", "modalidad"];

    // Textos que aparecen en el resumen cuando todavía no hay una respuesta.
    const TEXTOS_VACIOS = {
        materia: "Sin elegir",
        tema: "Sin escribir",
        tipo: "Sin elegir",
        horario: "Sin elegir",
        modalidad: "Sin elegir"
    };

    /**
     * Anuncia un mensaje a los lectores de pantalla.
     *
     * @param {string} mensaje texto a anunciar
     */
    function anunciar(mensaje) {
        if (avisoLector) {
            avisoLector.textContent = mensaje;
        }
    }

    /**
     * Devuelve el texto que el estudiante eligió o escribió en un paso, o "" si está vacío.
     *
     * @param {string} paso nombre del paso
     * @returns {string} texto del valor elegido
     */
    function valorDe(paso) {
        // El tema es el único paso cuyo valor se obtiene de un campo de texto.
        if (paso === "tema") {
            return campoTema ? campoTema.value.trim() : "";
        }

        // Para los demás pasos se busca la opción de tipo radio que esté seleccionada.
        const elegido = formulario.querySelector(`input[name="${paso}"]:checked`);
        return elegido ? (elegido.dataset.texto || elegido.value) : "";
    }

    /**
     * Actualiza un paso: lo marca como listo, quita el error y llena el resumen.
     *
     * @param {string} paso nombre del paso
     */
    function actualizarPaso(paso) {
        const contenedor = formulario.querySelector(`[data-paso="${paso}"]`);
        const valor = valorDe(paso);
        const resumen = seccion.querySelector(`[data-resumen="${paso}"]`);

        // Un paso se considera completo cuando tiene un valor.
        if (contenedor) {
            contenedor.classList.toggle("completo", valor !== "");
            if (valor !== "") {
                quitarError(paso);
            }
        }

        // Actualiza el resumen y deja el texto correspondiente cuando no hay una respuesta.
        if (resumen) {
            const nuevoTexto = valor || TEXTOS_VACIOS[paso];
            resumen.classList.toggle("pendiente", valor === "");
            if (resumen.textContent !== nuevoTexto) {
                resumen.textContent = nuevoTexto;
                resumen.classList.remove("actualizado");
                void resumen.offsetWidth;
                resumen.classList.add("actualizado");
            }
        }

        // Marca la opción elegida para navegadores sin :has().
        formulario.querySelectorAll(`input[name="${paso}"]`).forEach(entrada => {
            entrada.closest("label").classList.toggle("elegida", entrada.checked);
        });
    }

    /**
     * Actualiza la barra de progreso del resumen.
     */
    function actualizarProgreso() {
        // Cuenta cuántos de los cinco pasos obligatorios tienen información.
        const listos = PASOS.filter(paso => valorDe(paso) !== "").length;
        if (progreso) {
            progreso.style.width = `${(listos / PASOS.length) * 100}%`;
        }
        if (pasosListos) {
            pasosListos.textContent = String(listos);
        }
    }

    /**
     * Muestra el error de un paso.
     *
     * @param {string} paso nombre del paso
     */
    function mostrarError(paso) {
        const contenedor = formulario.querySelector(`[data-paso="${paso}"]`);
        const error = formulario.querySelector(`[data-error="${paso}"]`);

        // Reinicia la clase para que la animación del error pueda ejecutarse nuevamente.
        if (contenedor) {
            contenedor.classList.remove("con-error");
            void contenedor.offsetWidth;
            contenedor.classList.add("con-error");
        }
        if (error) {
            error.hidden = false;
        }
    }

    /**
     * Quita el error de un paso.
     *
     * @param {string} paso nombre del paso
     */
    function quitarError(paso) {
        const contenedor = formulario.querySelector(`[data-paso="${paso}"]`);
        const error = formulario.querySelector(`[data-error="${paso}"]`);

        // Retira el estilo de error y oculta el mensaje cuando el paso ya está corregido.
        if (contenedor) {
            contenedor.classList.remove("con-error");
        }
        if (error) {
            error.hidden = true;
        }
    }

    /**
     * Actualiza el contador de caracteres del tema.
     */
    function actualizarContadorTema() {
        if (!campoTema || !caracteres) {
            return;
        }

        // Obtiene la cantidad de caracteres y el máximo definido en el HTML.
        const largo = campoTema.value.length;
        const maximo = parseInt(campoTema.getAttribute("maxlength"), 10) || 150;
        caracteres.textContent = String(largo);

        // Cambia el estilo cuando faltan 20 caracteres o menos para llegar al límite.
        if (contadorTema) {
            contadorTema.classList.toggle("cerca", largo >= maximo - 20);
        }
    }

    // 1. Al elegir una opción: animación, paso listo y resumen.
    formulario.querySelectorAll('input[type="radio"]').forEach(entrada => {
        entrada.addEventListener("change", () => {
            const etiqueta = entrada.closest("label");

            // La animación se omite si el usuario tiene activada la reducción de movimiento.
            if (etiqueta && !reducirMovimiento.matches) {
                etiqueta.classList.remove("pop");
                void etiqueta.offsetWidth;
                etiqueta.classList.add("pop");
            }

            // Actualiza el paso al que pertenece la opción que acaba de cambiar.
            actualizarPaso(entrada.name);
            actualizarProgreso();

            // Informa al lector de pantalla qué opción se acaba de seleccionar.
            anunciar(`Elegiste: ${entrada.dataset.texto || entrada.value}.`);
        });
    });

    // 2. Tema: contador, paso listo y resumen mientras se escribe.
    if (campoTema) {
        campoTema.addEventListener("input", () => {
            actualizarContadorTema();
            actualizarPaso("tema");
            actualizarProgreso();
        });

        // Enter en el tema no envía el formulario antes de tiempo.
        campoTema.addEventListener("keydown", evento => {
            if (evento.key === "Enter") {
                evento.preventDefault();
            }
        });
    }

    // 3. Ajustes razonables (RF-09): paso opcional con casillas.
    // Se obtienen las casillas y los elementos relacionados con la opción "Otro".
    const casillasAjuste = Array.from(formulario.querySelectorAll("[data-ajuste]"));
    const campoOtro = formulario.querySelector("[data-campo-otro]");
    const detalleOtro = formulario.querySelector("[data-detalle-otro]");
    const caracteresAjuste = seccion.querySelector("[data-caracteres-ajuste]");
    const contadorAjuste = seccion.querySelector("[data-contador-ajuste]");

    /**
     * Indica si la casilla "Otro" está marcada.
     *
     * @returns {boolean} true si eligió "Otro"
     */
    function otroMarcado() {
        // Busca la casilla cuyo valor identifica la opción "OTRO".
        const otro = casillasAjuste.find(casilla => casilla.value === "OTRO");
        return Boolean(otro && otro.checked);
    }

    /**
     * Caso de fallo de RF-09: marcó "Otro" y no describió el ajuste.
     *
     * @returns {boolean} true si falta la descripción
     */
    function otroSinDetalle() {
        // La descripción solo se exige cuando la casilla "Otro" está seleccionada.
        return otroMarcado() && (!detalleOtro || detalleOtro.value.trim() === "");
    }

    /**
     * Actualiza el contador de caracteres de la descripción del ajuste.
     */
    function actualizarContadorAjuste() {
        if (!detalleOtro || !caracteresAjuste) {
            return;
        }

        const largo = detalleOtro.value.length;
        caracteresAjuste.textContent = String(largo);

        // El aviso aparece cuando quedan 20 caracteres o menos del límite de 255.
        if (contadorAjuste) {
            contadorAjuste.classList.toggle("cerca", largo >= 235);
        }
    }

    /**
     * Marca las casillas elegidas, muestra u oculta la descripción de "Otro"
     * y llena el resumen con los ajustes.
     */
    function actualizarAjustes() {
        // Resalta las casillas que están seleccionadas.
        casillasAjuste.forEach(casilla => {
            casilla.closest("label").classList.toggle("elegida", casilla.checked);
        });

        // El campo de descripción solo aparece cuando se marca "Otro".
        if (campoOtro) {
            const mostrar = otroMarcado();
            if (campoOtro.hidden === mostrar) {
                campoOtro.hidden = !mostrar;
                if (mostrar && !reducirMovimiento.matches) {
                    campoOtro.classList.remove("abriendo");
                    void campoOtro.offsetWidth;
                    campoOtro.classList.add("abriendo");
                }
            }
        }

        // Prepara los nombres de los ajustes seleccionados para mostrarlos en el resumen.
        const elegidos = casillasAjuste
            .filter(casilla => casilla.checked)
            .map(casilla => {
                const detalle = detalleOtro ? detalleOtro.value.trim() : "";
                return casilla.value === "OTRO" && detalle !== ""
                    ? `Otro: ${detalle}`
                    : (casilla.dataset.texto || casilla.value);
            });

        // El paso se marca como completo si hay al menos un ajuste y "Otro"
        // tiene su descripción cuando corresponde.
        const paso = formulario.querySelector('[data-paso="ajustes"]');
        if (paso) {
            paso.classList.toggle("completo", elegidos.length > 0 && !otroSinDetalle());
        }
        if (!otroSinDetalle()) {
            quitarError("ajustes");
        }

        // Muestra los ajustes seleccionados o "Ninguno" cuando no se marcó ninguno.
        const resumen = seccion.querySelector('[data-resumen="ajustes"]');
        if (resumen) {
            const texto = elegidos.length > 0 ? elegidos.join(", ") : "Ninguno";
            if (resumen.textContent !== texto) {
                resumen.textContent = texto;
                resumen.classList.remove("actualizado");
                void resumen.offsetWidth;
                resumen.classList.add("actualizado");
            }
        }
    }

    // Cada vez que cambia una casilla, se actualizan los ajustes y el resumen.
    casillasAjuste.forEach(casilla => {
        casilla.addEventListener("change", () => {
            const etiqueta = casilla.closest("label");

            // Aplica la animación de selección si el sistema permite movimiento.
            if (etiqueta && !reducirMovimiento.matches) {
                etiqueta.classList.remove("pop");
                void etiqueta.offsetWidth;
                etiqueta.classList.add("pop");
            }

            actualizarAjustes();

            // Anuncia si el estudiante marcó o quitó una opción.
            const texto = casilla.dataset.texto || casilla.value;
            anunciar(casilla.checked ? `Marcaste: ${texto}.` : `Quitaste: ${texto}.`);

            // Al marcar "Otro", el cursor pasa al campo para describirlo.
            if (casilla.value === "OTRO" && casilla.checked && detalleOtro) {
                setTimeout(() => detalleOtro.focus(), 150);
            }
        });
    });

    // Mientras se escribe la descripción de "Otro", actualiza el contador y el resumen.
    if (detalleOtro) {
        detalleOtro.addEventListener("input", () => {
            actualizarContadorAjuste();
            actualizarAjustes();
        });
    }

    // 4. Antes de enviar: revisa que todo esté completo.
    formulario.addEventListener("submit", evento => {
        // Busca cuáles de los cinco pasos obligatorios siguen vacíos.
        const faltantes = PASOS.filter(paso => valorDe(paso) === "");

        // Si se marcó "Otro" sin escribir su descripción, también se cuenta como faltante.
        if (otroSinDetalle()) {
            faltantes.push("ajustes");
        }

        if (faltantes.length > 0) {
            // Detiene el envío para que el estudiante pueda corregir los datos.
            evento.preventDefault();
            faltantes.forEach(mostrarError);

            // Lleva la pantalla al primer paso que necesita atención.
            const primerPaso = faltantes[0];
            const primero = formulario.querySelector(`[data-paso="${primerPaso}"]`);
            if (primero) {
                primero.scrollIntoView({ behavior: reducirMovimiento.matches ? "auto" : "smooth", block: "center" });

                // Si falta la descripción de "Otro", el foco va directamente a ese campo.
                // En los demás casos, se enfoca la primera opción disponible del paso.
                const foco = primerPaso === "ajustes"
                    ? detalleOtro
                    : primero.querySelector("input:not([type=hidden])");
                if (foco) {
                    setTimeout(() => foco.focus({ preventScroll: true }), 300);
                }
            }

            // Informa cuántos pasos faltan por completar.
            anunciar(faltantes.length === 1
                ? "Falta completar un paso."
                : `Faltan completar ${faltantes.length} pasos.`);
            return;
        }

        // Todo listo: muestra que se está enviando y evita el doble envío.
        if (botonEnviar) {
            botonEnviar.disabled = true;
            botonEnviar.setAttribute("aria-busy", "true");
        }
        if (textoEnviar) {
            textoEnviar.innerHTML = '<span class="boton-accion__cargando" aria-hidden="true"></span>Enviando solicitud...';
        }
        anunciar("Enviando tu solicitud.");
    });

    // 5. Volver a la página anterior si viene de Feeling; si no, a la búsqueda.
    const volver = seccion.querySelector("[data-volver]");
    if (volver) {
        volver.addEventListener("click", evento => {
            // Solo utiliza el historial si la página anterior pertenece al mismo sitio
            // y existe una página previa en la navegación.
            if (document.referrer && new URL(document.referrer).origin === window.location.origin
                    && window.history.length > 1) {
                evento.preventDefault();
                window.history.back();
            }
        });
    }

    // 6. Estado inicial (por si el servidor devolvió el formulario con datos).
    // Deja sincronizados los pasos, el resumen y los contadores al cargar la pantalla.
    PASOS.forEach(actualizarPaso);
    actualizarProgreso();
    actualizarContadorTema();
    actualizarContadorAjuste();
    actualizarAjustes();
});
