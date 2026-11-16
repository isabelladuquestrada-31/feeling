package com.feeling.feeling.service;

import com.feeling.feeling.dto.ResumenAdmin;
import com.feeling.feeling.model.EstadoVoluntario;
import com.feeling.feeling.model.Materia;
import com.feeling.feeling.model.Perfil;
import com.feeling.feeling.model.Usuario;
import com.feeling.feeling.repository.IMateriaRepository;
import com.feeling.feeling.repository.IPerfilRepository;
import com.feeling.feeling.repository.IVoluntarioRepository;
import com.feeling.feeling.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Reúne los datos de las pantallas de inicio de cada rol (SCRUM-53, RF-02).
 *
 * @author Daniel Blandón
 * @version 1.0
 */
@Service
public class PanelService {

    private final IPerfilRepository perfilRepository;
    private final IMateriaRepository materiaRepository;
    private final UsuarioRepository usuarioRepository;
    private final IVoluntarioRepository voluntarioRepository;

    /**
     * Crea el servicio con los repositorios que necesita.
     *
     * @param perfilRepository     repositorio de perfiles
     * @param materiaRepository    repositorio de materias
     * @param usuarioRepository    repositorio de usuarios
     * @param voluntarioRepository repositorio de voluntarios
     */
    public PanelService(IPerfilRepository perfilRepository,
                        IMateriaRepository materiaRepository,
                        UsuarioRepository usuarioRepository,
                        IVoluntarioRepository voluntarioRepository) {
        this.perfilRepository = perfilRepository;
        this.materiaRepository = materiaRepository;
        this.usuarioRepository = usuarioRepository;
        this.voluntarioRepository = voluntarioRepository;
    }

    /**
     * Devuelve el nombre con el que se saluda al usuario: su nombre preferido
     * del perfil o, si no tiene, su nombre de usuario.
     *
     * @param usuario usuario que inició sesión
     * @return nombre para el saludo
     */
    @Transactional(readOnly = true)
    public String nombreParaSaludo(Usuario usuario) {
        return perfilRepository.findById(usuario.getId())
                .map(Perfil::getNombrePreferido)
                .filter(nombre -> nombre != null && !nombre.isBlank())
                .map(String::trim)
                .orElse(usuario.getNombreUsuario());
    }

    /**
     * Devuelve el nombre con el que se muestra cada estudiante (su nombre preferido
     * o, si no tiene, su nombre de usuario), por ejemplo en las solicitudes recibidas.
     *
     * @param ids ids de los estudiantes
     * @return mapa de id del estudiante a su nombre
     */
    @Transactional(readOnly = true)
    public Map<Integer, String> nombresDeEstudiantes(Collection<Integer> ids) {
        Map<Integer, String> nombres = new HashMap<>();
        if (ids == null || ids.isEmpty()) {
            return nombres;
        }
        usuarioRepository.findAllById(ids)
                .forEach(usuario -> nombres.put(usuario.getId(), usuario.getNombreUsuario()));
        perfilRepository.findAllById(ids).forEach(perfil -> {
            String preferido = perfil.getNombrePreferido();
            Integer id = perfil.getUsuarioId() != null ? perfil.getUsuarioId()
                    : perfil.getUsuario() != null ? perfil.getUsuario().getId() : null;
            if (id != null && preferido != null && !preferido.isBlank()) {
                nombres.put(id, preferido.trim());
            }
        });
        return nombres;
    }

    /**
     * Devuelve el nombre de cada materia según su código, para mostrar
     * "Cálculo Diferencial" en lugar de "MAT101".
     *
     * @return mapa de código a nombre de la materia
     */
    @Transactional(readOnly = true)
    public Map<String, String> nombresDeMaterias() {
        return materiaRepository.findAll().stream()
                .collect(Collectors.toMap(Materia::getCodigo, Materia::getNombre, (a, b) -> a));
    }

    /**
     * Cuenta los estudiantes y voluntarios para la pantalla del administrador.
     *
     * @return cifras del resumen
     */
    @Transactional(readOnly = true)
    public ResumenAdmin resumenAdmin() {
        return new ResumenAdmin(
                usuarioRepository.contarEstudiantes(),
                voluntarioRepository.countByEstado(EstadoVoluntario.ACTIVO),
                voluntarioRepository.countByEstado(EstadoVoluntario.SUSPENDIDO));
    }
}
