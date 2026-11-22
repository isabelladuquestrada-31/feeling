package com.feeling.feeling.service;

import com.feeling.feeling.model.PreferenciasAccesibilidad;
import com.feeling.feeling.model.Usuario;
import com.feeling.feeling.repository.IPreferenciaAccesibilidadRepository;
import com.feeling.feeling.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Clase que contiene las preferencias de accesibilidad estipuladas en el RF-04: Letra grande
 * y alto contraste. Las preferencias se guardan en la tabla "preferencias_accesibilidad" y se cargan
 * en la sesión cuando el usuario inicia sesión
 *
 * @author Daniel Blandón
 * @version 1.0
 */
@Service
public class PreferenciasService {

    /**
     * Nombre del atributo de la sesión donde quedan las preferencias del usuario
     */
    public static final String ATRIBUTO_SESION = "preferencias";

    /**
     * Clase CSS que agrabda la letra e todas la páginas
     */
    public static final String CLASE_LETRA_GRANDE = "letra-grande";

    /**
     * Clase CSS que activa los colores de alto constraste en todas las páginas
     */
    public static final String CLASE_ALTO_CONTRASTE = "alto-contraste";

    /**
     * Repositorio de las preferencias de accesbilidad
     */
    private final IPreferenciaAccesibilidadRepository preferenciasRepository;

    /**
     * Repositorio de usuarios, para crear las peferencias si todavía no existen.
     */
    private final UsuarioRepository usuarioRepository;

    /**
     * Crea el servicio con sus repositorios.
     *
     * @param preferenciasRepository repositorio de preferencias
     * @param usuarioRepository      repositorio de usuarios
     */
    public PreferenciasService(IPreferenciaAccesibilidadRepository preferenciasRepository, UsuarioRepository usuarioRepository) {
        this.preferenciasRepository = preferenciasRepository;
        this.usuarioRepository = usuarioRepository;
    }

    /**
     * Busca las prefencias de un usuario. Si nunca las ha guardado,
     * devuelve unas preferencias con todo apagado.
     *
     * @param usuarioId id del usuario
     * @return las preferencias del usuario
     */
    @Transactional(readOnly = true)
    public PreferenciasAccesibilidad obtener(Integer usuarioId) {
        return preferenciasRepository.findById(usuarioId).orElseGet(PreferenciasAccesibilidad::new);
    }

    /**
     * Guarda las preferencias de un usuario. Si todavía no tiene una fila
     * en la base de datos, la crea.
     *
     * @param usuarioId     id del usuario
     * @param letraGrande   true para activar la letra grande
     * @param altoContraste true para activar el alto contraste
     * @return las preferencias guardadas
     * @throws IllegalArgumentException si el usuario no existe
     */
    @Transactional
    public PreferenciasAccesibilidad guardar(Integer usuarioId, boolean letraGrande, boolean altoContraste) {
        PreferenciasAccesibilidad preferencias = preferenciasRepository.findById(usuarioId).orElseGet(() -> crearPara(usuarioId));
        preferencias.setLetraGrande(letraGrande);
        preferencias.setAltoContraste(altoContraste);
        return preferenciasRepository.save(preferencias);
    }

    /**
     * Convierte las preferencias en las clases CSS que se ponen en la página.
     *
     * @param preferencias preferencias del usuario (puede ser null)
     * @return por ejmeplo "letra-grande alto-contraste" o "" si no hay ninguna activa
     */
    public static String clasesCss(PreferenciasAccesibilidad preferencias) {
        if (preferencias == null) {
            return "";
        }
        StringBuilder clases = new StringBuilder();
        if (preferencias.isLetraGrande()) {
            clases.append(CLASE_LETRA_GRANDE);
        }
        if (preferencias.isAltoContraste()) {
            if (!clases.isEmpty()) {
                clases.append(' ');
            }
            clases.append(CLASE_ALTO_CONTRASTE);
        }
        return clases.toString();
    }

    /**
     * Crea unas preferencias nuevas (con todo apagado) para un usuario que no tiene
     *
     * @param usuarioId id del usuario
     * @return las preferencias nuevas, tdavia sin guardar
     */
    private PreferenciasAccesibilidad crearPara(Integer usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId).orElseThrow(() -> new IllegalArgumentException("El usuario no existe"));
        PreferenciasAccesibilidad nuevas = new PreferenciasAccesibilidad();
        nuevas.setUsuario(usuario);
        return nuevas;
    }
}
