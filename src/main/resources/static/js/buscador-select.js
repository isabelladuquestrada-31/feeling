/*
 * PROYECTO: Feeling
 * ARCHIVO: buscador-select.js
 * UBICACIÓN: src/main/resources/static/js/buscador-select.js
 * MÓDULO: Buscar voluntarios (panel del estudiante)
 *
 * AUTOR: Yulipaola Domínguez
 * VERSIÓN: 1.1.0
 * FECHA DE DOCUMENTACIÓN: 2026-11-25
 * ESTADO: Finalizado
 * TICKET: RF-07 (búsqueda de voluntarios)
 *
 * DESCRIPCIÓN:
 * Convierte cada lista desplegable con data-buscador en un campo donde se
 * escribe y se eligen sugerencias. La búsqueda ignora tildes y mayúsculas,
 * así "algebra" encuentra "Álgebra Lineal" y "sistemas" encuentra
 * "Ingeniería de Sistemas". El select original queda oculto y es el que se
 * envía en el formulario, por eso el servidor no cambia y, sin
 * JavaScript, la página sigue funcionando con la lista normal.
 *
 * FUNCIONALIDADES:
 * - Campo de texto con lista de sugerencias agrupadas (por ejemplo, por facultad).
 * - Búsqueda por varias palabras, sin tildes ni mayúsculas.
 * - Navegación con el teclado: flechas para moverse, Enter para elegir y
 *   Escape para cerrar.
 * - Botón × para quitar el filtro.
 * - Al elegir una opción avisa con el evento change, para que
 *   solicitudes.js envíe la búsqueda.
 * - Roles y atributos ARIA de combobox y listbox para lectores de pantalla.
 *
 * DEPENDENCIAS:
 * - buscar-voluntarios.html: listas desplegables con data-buscador.
 * - voluntarios.css: estilos de las clases buscador__*.
 * - solicitudes.js: envía la búsqueda cuando el select cambia.
 *
 * ATRIBUTOS DATA-* UTILIZADOS:
 * - data-buscador: marca las listas que se convierten en buscador.
 * - data-buscador-placeholder: texto de ejemplo del campo (opcional).
 *
 * HISTORIAL DE VERSIONES:
 * - 1.0.0: buscador con sugerencias para listas largas (RF-07).
 * - 1.1.0 (2026-11-25): documentación completa. No cambia la lógica.
 */


// El script se ejecuta cuando el documento terminó de cargarse.
document.addEventListener("DOMContentLoaded", () => {
    "use strict";

    /**
     * Quita tildes, pasa a minúsculas y deja un solo espacio entre palabras.
     *
     * @param {string} texto texto a comparar
     * @returns {string} texto normalizado
     */
    function normalizar(texto) {
        return (texto || "")
            .normalize("NFD")
            .replace(/[̀-ͯ]/g, "")
            .toLowerCase()
            .replace(/\s+/g, " ")
            .trim();
    }

    // Se convierte cada lista desplegable marcada con data-buscador.
    document.querySelectorAll("select[data-buscador]").forEach((select, indice) => {
        // Identificadores del campo y de la lista, y etiqueta del select.
        const idLista = `${select.id || "buscador-" + indice}-sugerencias`;
        const idCampo = `${select.id || "buscador-" + indice}-texto`;
        const etiqueta = select.id ? document.querySelector(`label[for="${select.id}"]`) : null;

        // Opciones del select, con el grupo (facultad) al que pertenecen.
        const opciones = Array.from(select.options).map(opcion => ({
            valor: opcion.value,
            texto: opcion.textContent.trim(),
            grupo: opcion.parentElement.tagName === "OPTGROUP" ? opcion.parentElement.label : "",
            busqueda: normalizar(`${opcion.textContent} ${opcion.value} ${opcion.parentElement.label || ""}`)
        }));
        // Opción vacía ("Todas"), que sirve para quitar el filtro.
        const opcionTodas = opciones.find(opcion => opcion.valor === "");

        // 1. Construye el campo y la lista de sugerencias.
        // Contenedor del buscador.
        const contenedor = document.createElement("div");
        contenedor.className = "buscador";

        // Campo de texto con el rol de combobox para lectores de pantalla.
        const campo = document.createElement("input");
        campo.type = "text";
        campo.id = idCampo;
        campo.className = "buscador__campo";
        campo.autocomplete = "off";
        campo.spellcheck = false;
        campo.placeholder = select.dataset.buscadorPlaceholder || (opcionTodas ? opcionTodas.texto : "Escribe para buscar");
        campo.setAttribute("role", "combobox");
        campo.setAttribute("aria-autocomplete", "list");
        campo.setAttribute("aria-expanded", "false");
        campo.setAttribute("aria-controls", idLista);

        // Botón × para borrar el valor.
        const botonLimpiar = document.createElement("button");
        botonLimpiar.type = "button";
        botonLimpiar.className = "buscador__limpiar";
        botonLimpiar.setAttribute("aria-label", opcionTodas ? `Quitar filtro: ${opcionTodas.texto}` : "Borrar");
        botonLimpiar.textContent = "×";

        // Lista de sugerencias, oculta al inicio. La etiqueta del select pasa
        // a apuntar al campo nuevo.
        const lista = document.createElement("ul");
        lista.id = idLista;
        lista.className = "buscador__lista";
        lista.setAttribute("role", "listbox");
        lista.hidden = true;
        if (etiqueta) {
            lista.setAttribute("aria-label", etiqueta.textContent.trim());
            etiqueta.htmlFor = idCampo;
        }

        // El select original se oculta y deja de recibir el foco, pero se sigue
        // enviando en el formulario. El buscador se pone justo después.
        select.classList.add("buscador__select-oculto");
        select.tabIndex = -1;
        select.setAttribute("aria-hidden", "true");
        select.after(contenedor);
        contenedor.append(campo, botonLimpiar, lista);

        // Sugerencias que se ven y posición de la sugerencia marcada.
        let visibles = [];
        let activa = -1;

        // 2. Muestra en el campo la opción elegida actualmente.
        // Texto de la opción elegida, o vacío si es "Todas".
        function textoElegido() {
            const elegida = opciones.find(opcion => opcion.valor === select.value);
            return elegida && elegida.valor !== "" ? elegida.texto : "";
        }

        // Escribe en el campo la opción elegida y muestra u oculta el botón ×.
        function actualizarCampo() {
            campo.value = textoElegido();
            contenedor.classList.toggle("buscador--con-valor", campo.value !== "");
        }

        // 3. Pinta las sugerencias que coinciden con lo escrito.
        function pintar(consulta) {
            // Una opción coincide si contiene todas las palabras escritas.
            const terminos = normalizar(consulta).split(" ").filter(Boolean);
            visibles = opciones.filter(opcion =>
                terminos.every(termino => opcion.busqueda.includes(termino)));

            // Se vacía la lista y se agregan las sugerencias, con un título por grupo.
            lista.replaceChildren();
            let grupoActual = null;

            visibles.forEach((opcion, posicion) => {
                if (opcion.grupo && opcion.grupo !== grupoActual) {
                    grupoActual = opcion.grupo;
                    const titulo = document.createElement("li");
                    titulo.className = "buscador__grupo";
                    titulo.setAttribute("role", "presentation");
                    titulo.textContent = opcion.grupo;
                    lista.append(titulo);
                }

                // Cada sugerencia con el rol de opción.
                const item = document.createElement("li");
                item.id = `${idLista}-${posicion}`;
                item.className = "buscador__opcion";
                if (opcion.valor === "") {
                    item.classList.add("buscador__opcion--todas");
                }
                item.setAttribute("role", "option");
                item.setAttribute("aria-selected", String(opcion.valor === select.value));
                item.textContent = opcion.texto;

                // mousedown en lugar de click: así se elige antes de que el campo pierda el foco.
                item.addEventListener("mousedown", evento => {
                    evento.preventDefault();
                    elegir(opcion);
                });
                lista.append(item);
            });

            // Mensaje cuando ninguna opción coincide.
            if (visibles.length === 0) {
                const vacio = document.createElement("li");
                vacio.className = "buscador__vacio";
                vacio.setAttribute("role", "presentation");
                vacio.textContent = "No hay coincidencias";
                lista.append(vacio);
            }

            // Si se escribió algo, se marca la primera sugerencia.
            marcar(visibles.length > 0 && terminos.length > 0 ? 0 : -1);
        }

        // Marca la sugerencia de la posición indicada y la deja a la vista.
        function marcar(posicion) {
            activa = posicion;
            lista.querySelectorAll(".buscador__opcion").forEach((item, i) => {
                item.classList.toggle("buscador__opcion--activa", i === activa);
            });
            const item = activa >= 0 ? document.getElementById(`${idLista}-${activa}`) : null;
            if (item) {
                campo.setAttribute("aria-activedescendant", item.id);
                item.scrollIntoView({ block: "nearest" });
            } else {
                campo.removeAttribute("aria-activedescendant");
            }
        }

        // Abre la lista de sugerencias.
        function abrir() {
            if (!lista.hidden) {
                return;
            }
            lista.hidden = false;
            campo.setAttribute("aria-expanded", "true");
            contenedor.classList.add("buscador--abierto");
        }

        // Cierra la lista de sugerencias.
        function cerrar() {
            lista.hidden = true;
            campo.setAttribute("aria-expanded", "false");
            campo.removeAttribute("aria-activedescendant");
            contenedor.classList.remove("buscador--abierto");
        }

        // 4. Elige una opción: actualiza el select y avisa con "change"
        //    para que solicitudes.js aplique el filtro solo.
        function elegir(opcion) {
            cerrar();
            const cambio = select.value !== opcion.valor;
            select.value = opcion.valor;
            actualizarCampo();
            if (cambio) {
                select.dispatchEvent(new Event("change", { bubbles: true }));
            }
        }

        // 5. Eventos del campo.
        // Al entrar al campo se selecciona su texto y se muestran todas las opciones.
        campo.addEventListener("focus", () => {
            campo.select();
            pintar("");
            abrir();
        });

        // Al escribir se filtran las sugerencias.
        campo.addEventListener("input", () => {
            contenedor.classList.toggle("buscador--con-valor", campo.value !== "");
            pintar(campo.value);
            abrir();
        });

        // Teclado: las flechas mueven la sugerencia marcada (y vuelven al inicio
        // al llegar al final), Enter la elige y Escape cierra la lista.
        campo.addEventListener("keydown", evento => {
            if (evento.key === "ArrowDown" || evento.key === "ArrowUp") {
                evento.preventDefault();
                if (lista.hidden) {
                    pintar(campo.value);
                    abrir();
                }
                if (visibles.length === 0) {
                    return;
                }
                const paso = evento.key === "ArrowDown" ? 1 : -1;
                marcar((activa + paso + visibles.length) % visibles.length);
            } else if (evento.key === "Enter") {
                // Enter elige la sugerencia marcada en vez de enviar el formulario.
                if (!lista.hidden && activa >= 0) {
                    evento.preventDefault();
                    elegir(visibles[activa]);
                } else if (!lista.hidden) {
                    evento.preventDefault();
                }
            } else if (evento.key === "Escape") {
                if (!lista.hidden) {
                    evento.preventDefault();
                    cerrar();
                    actualizarCampo();
                }
            }
        });

        // Al salir sin elegir: si borró todo, quita el filtro;
        // si dejó un texto a medias, vuelve a mostrar lo que estaba elegido.
        campo.addEventListener("blur", () => {
            if (lista.hidden) {
                return;
            }
            if (campo.value.trim() === "" && opcionTodas) {
                elegir(opcionTodas);
                return;
            }
            cerrar();
            actualizarCampo();
        });

        // El botón × quita el filtro y devuelve el foco al campo.
        botonLimpiar.addEventListener("click", () => {
            if (opcionTodas) {
                elegir(opcionTodas);
            }
            campo.focus();
        });

        // Si otra parte de la página cambia el select, el campo se actualiza.
        select.addEventListener("change", actualizarCampo);

        // Al cargar se muestra en el campo la opción que ya estaba elegida.
        actualizarCampo();
    });
});
