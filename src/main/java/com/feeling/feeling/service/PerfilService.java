package com.feeling.feeling.service;


import com.feeling.feeling.model.Perfil;
import com.feeling.feeling.repository.IPerfilRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Lógica de negocio de la pantalla "Mi perfil" (RF-03): Consiste en que el ususario pueda
 * consultar y editar el nombre preferido, los pronombres y el número de WhatsApp del estudiante.
 *
 * @author Daniel Blandón
 * @version 1.0
 */
@Service
public class PerfilService {

    /**
     * Opciones de pronombres que puede elegir el estudiante
     */
    public static final List<String> PRONOMBRES = List.of("Él", "Ella", "Prefiero no decir");

    /**
     * Largo máximo del nombre preferido (es el mismo de la columna en la base de datos)
     */
    private static final int MAXIMO_NOMBRE = 50;

    /**
     * Repositorio para consultar y guardar los perfiles.
     */
    private final IPerfilRepository perfilRepository;

    /**
     * Crea el servicio con el repositorio de perfiles
     *
     * @param perfilRepository repositorio de perfiles
     */
    public PerfilService(IPerfilRepository perfilRepository) {
        this.perfilRepository = perfilRepository;

    }

    /**
     * Busca el perfil del ususario.
     *
     * @param usuarioId id del ususario (es el mismo que se guarda en la sesión)
     * @return el perfil del usuario
     * @throws IllegalArgumentException si el usuario no tiene perfil
     */
    @Transactional(readOnly = true)
    public Perfil obtenerPerfil(Integer usuarioId) {
        return perfilRepository.findById(usuarioId).orElseThrow(() -> new IllegalArgumentException("El perfil no existe."));
    }

    /**
     * Revisa los datos del formulario, Cada error queda asoscioado al campo que lo tiene,
     * para que la vista lo muestre debajo de ese campo.
     *
     * @param nombrePreferido nombre preferido (es opcional y de máximo 50 caracteres)
     * @param pronombres      pronombres (es opcional, pero debe ser una opcion de la lista)
     * @param whatsapp        WhatsApp (es obligatorio, debe ser de 10 dígitos y comenzar con 3, como todo número colombiano)
     * @return mapa campo, mensaje de error; sale vacio si todo esta bien
     */
    public Map<String, String> validar(String nombrePreferido, String pronombres, String whatsapp) {
        Map<String, String> errores = new LinkedHashMap<>();
        String nombre = limpiar(nombrePreferido);
        String pronombre = limpiar(pronombres);
        String numero = limpiar(whatsapp);

        if (nombre != null && nombre.length() > MAXIMO_NOMBRE) {
            errores.put("nombrePreferido", "El nombre referido no puede tener más de 50 caracteres.");
        }

        if (pronombre != null && !PRONOMBRES.contains(pronombre)) {
            errores.put("pronombres", "Elige una opción de la lista.");
        }

        if (numero == null) {
            errores.put("whatsapp", "El número de WhatsApp es obligatorio.");
        } else if (!numero.matches("3\\d{9}")) {
            errores.put("whatsapp", "Escribe un número de celular de 10 dígitos que empiece por 3.");
        }
        return errores;
    }

    /**
     * Valida y guarda los cambios del perfil hechos por el ususario.
     *
     * @param usuarioId       id del usuario dueño del perfil
     * @param nombrePreferido nuevo nombre preferido (si esta vacio se borra)
     * @param pronombres      nuevos pronombres (si esta vacio se borra)
     * @param whatsapp        nuevo número de WhatsApp
     * @return el perfil actualizado
     * @throws IllegalArgumentException salta si algun dato no es válido o el perfil no existe
     */
    @Transactional
    public Perfil actualizarPerfil(Integer usuarioId, String nombrePreferido, String pronombres, String whatsapp) {
        Map<String, String> errores = validar(nombrePreferido, pronombres, whatsapp);
        if (!errores.isEmpty()) {
            throw new IllegalArgumentException(errores.values().iterator().next());
        }

        Perfil perfil = obtenerPerfil(usuarioId);
        perfil.setNombrePreferido((limpiar(nombrePreferido)));
        perfil.setPronombres(limpiar(pronombres));
        perfil.setWhatsapp((limpiar(whatsapp)));
        return perfilRepository.save(perfil);
    }

    /**
     * Quita los espacios de los extremos. Un texto vacio se convierne en null.
     *
     * @param texto texto escrito en un campo del formulario
     * @return el texto sin espacios en los extremos, o null si estaba vacío
     */
    private String limpiar(String texto) {
        if (texto == null || texto.isBlank()) {
            return null;
        }
        return texto.trim();
    }
}
