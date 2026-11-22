package com.feeling.feeling.service;
import com.feeling.feeling.model.*;
import com.feeling.feeling.repository.IMateriaRepository;
import com.feeling.feeling.repository.IVoluntarioRepository;
import com.feeling.feeling.repository.UsuarioRepository;
import com.feeling.feeling.repository.SolicitudRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Servicio para gestionar los perfiles de voluntario.
 *
 * @author Isabella Duque
 * @version 1.0
 */
@Service
public class VoluntarioService {
    private final UsuarioRepository usuarioRepository;
    private final IMateriaRepository materiaRepository;
    private final IVoluntarioRepository voluntarioRepository;
    private final SolicitudRepository solicitudRepository;

    /**
     * Crea el servicio con los repositorios que necesita.
     *
     * @param usuarioRepository repositorio de usuarios
     * @param materiaRepository repositorio de materias
     * @param voluntarioRepository repositorio de voluntarios
     * @param solicitudRepository repositorio de solicitudes, para calcular las horas de voluntariado
     */
    public VoluntarioService(UsuarioRepository usuarioRepository, IMateriaRepository materiaRepository, IVoluntarioRepository voluntarioRepository, SolicitudRepository solicitudRepository) {
        this.usuarioRepository = usuarioRepository;
        this.materiaRepository = materiaRepository;
        this.voluntarioRepository = voluntarioRepository;
        this.solicitudRepository = solicitudRepository;
    }

    /**
     * Activa a un estudiante como voluntario en estado ACTIVO.
     *
     * <p>Busca al usuario por su id, verifica que sea un estudiante,
     * obtiene las materias elegidas a partir de sus códigos y le pide
     * al estudiante que cree su perfil de voluntario. Si todo es válido,
     * guarda el perfil en la base de datos.</p>
     *
     * @param idEstudiante    id del estudiante que se activa como voluntario
     * @param codigosMaterias códigos de las materias que el estudiante eligió
     * @param tiposApoyo      tipos de apoyo que el estudiante eligió
     * @return el perfil de voluntario guardado
     * @throws IllegalArgumentException si el usuario no existe, si no es estudiante, o si no eligió al menos una materia y un tipo de apoyo
     * @throws IllegalStateException    si el estudiante ya está registrado como voluntario
     */
    @Transactional
    public Voluntario activarVoluntario(Integer idEstudiante, List<String> codigosMaterias, List<TipoApoyo> tiposApoyo) {

        Optional<Usuario> resultado = usuarioRepository.findById(idEstudiante);

        if (resultado.isEmpty()) {
            throw new IllegalArgumentException("El usuario no existe.");
        }
        Usuario usuario = resultado.get();

        if (!(usuario instanceof Estudiante)) {
            throw new IllegalArgumentException("Solo los estudiantes pueden ser voluntarios.");
        }
        Estudiante estudiante = (Estudiante) usuario;

        List<Materia> materias = new ArrayList<>();
        if (codigosMaterias != null) {
            materias = materiaRepository.findAllById(codigosMaterias);
        }
        estudiante.activarseComoVoluntario(materias, tiposApoyo);

        return voluntarioRepository.save(estudiante.getVoluntario());
    }

    /**
     * Obtiene todas las materias que un voluntario puede elegir.
     *
     * @return lista de todas las materias
     */
    public List<Materia> listarMaterias() {
        return materiaRepository.findAll();
    }

    /**
     * Busca el perfil de voluntario de un estudiante, si ya se activó (RF-05).
     * Deja cargadas sus materias y tipos de apoyo para mostrarlos en la vista.
     *
     * @param idEstudiante id del estudiante que inició sesión
     * @return el perfil de voluntario, o vacío si todavía no es voluntario
     */
    @Transactional(readOnly = true)
    public Optional<Voluntario> buscarPorEstudiante(Integer idEstudiante) {
        return usuarioRepository.findById(idEstudiante)
                .filter(Estudiante.class::isInstance)
                .map(usuario -> ((Estudiante) usuario).getVoluntario())
                .map(voluntario -> {
                    voluntario.getMaterias().size();
                    voluntario.getTiposApoyo().size();
                    return voluntario;
                });
    }

    /**
     * Calcula las horas de voluntariado acumuladas de un voluntarios
     * Suma la duración de sus acompañamientos en estado REALIZADA y la pasa a horas
     *
     * @param voluntarioId ide del voluntario
     * @return total de horas
     */
    @Transactional(readOnly = true)
    public double calcularHorasVoluntariado (Integer voluntarioId) {
        Long minutos = solicitudRepository.sumarMinutosPorVoluntarioYEstado(voluntarioId, EstadoSolicitud.REALIZADA);
        if (minutos==null){
            return 0;
        }
        return minutos/60;
    }
}
