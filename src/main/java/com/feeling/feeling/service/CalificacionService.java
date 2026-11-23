package com.feeling.feeling.service;

import com.feeling.feeling.model.Calificacion;
import com.feeling.feeling.model.EstadoSolicitud;
import com.feeling.feeling.model.Solicitud;
import com.feeling.feeling.repository.ICalificacionRepository;
import com.feeling.feeling.repository.SolicitudRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Lógica para que el estudiante califique al voluntario después de un acompañamiento.
 *
 * @author Yeymi
 */
@Service
public class CalificacionService {

    /**
     * Puntuación mínima permitida.
     */
    private static final int PUNTUACION_MINIMA = 1;

    /**
     * Puntuación máxima permitida.
     */
    private static final int PUNTUACION_MAXIMA = 5;

    /**
     * Largo máximo del comentario, igual al de la columna en la base de datos.
     */
    private static final int COMENTARIO_MAXIMO = 255;

    /**
     * Repositorio para consultar las solicitudes.
     */
    private final SolicitudRepository solicitudRepository;

    /**
     * Repositorio para guardar las calificaciones.
     */
    private final ICalificacionRepository calificacionRepository;

    /**
     * Crea el servicio con los repositorios que le entrega Spring.
     *
     * @param solicitudRepository    repositorio de solicitudes
     * @param calificacionRepository repositorio de calificaciones
     */
    public CalificacionService(SolicitudRepository solicitudRepository,
                               ICalificacionRepository calificacionRepository) {
        this.solicitudRepository = solicitudRepository;
        this.calificacionRepository = calificacionRepository;
    }

    /**
     * Guarda la calificación de un acompañamiento. Solo se permite si la solicitud es del
     * estudiante, está REALIZADA y todavía no fue calificada.
     *
     * @param solicitudId  id de la solicitud que se califica
     * @param estudianteId id del estudiante que califica
     * @param puntuacion   estrellas, de 1 a 5
     * @param comentario   comentario opcional, de máximo 255 caracteres
     * @return la calificación guardada
     * @throws IllegalArgumentException si la solicitud no existe, no es del estudiante o los datos no son válidos
     * @throws IllegalStateException    si el acompañamiento no está realizado o ya fue calificado
     */
    @Transactional
    public Calificacion calificar(Integer solicitudId, Integer estudianteId,
                                  Integer puntuacion, String comentario) {
        Optional<Solicitud> encontrada = solicitudRepository.findById(solicitudId);
        if (!encontrada.isPresent()) {
            throw new IllegalArgumentException("La solicitud no existe.");
        }
        Solicitud solicitud = encontrada.get();

        if (!solicitud.getEstudianteId().equals(estudianteId)) {
            throw new IllegalArgumentException("Solo el estudiante que hizo la solicitud puede calificarla.");
        }

        if (solicitud.getEstado() != EstadoSolicitud.REALIZADA) {
            throw new IllegalStateException("Solo se puede calificar un acompañamiento que ya fue realizado.");
        }

        if (calificacionRepository.existsById(solicitudId)) {
            throw new IllegalStateException("Este acompañamiento ya fue calificado.");
        }

        if (puntuacion == null || puntuacion < PUNTUACION_MINIMA || puntuacion > PUNTUACION_MAXIMA) {
            throw new IllegalArgumentException("La calificación debe ser de 1 a 5 estrellas.");
        }

        String comentarioLimpio = null;
        if (comentario != null && !comentario.trim().isEmpty()) {
            comentarioLimpio = comentario.trim();
            if (comentarioLimpio.length() > COMENTARIO_MAXIMO) {
                throw new IllegalArgumentException(
                        "El comentario no puede pasar de " + COMENTARIO_MAXIMO + " caracteres.");
            }
        }

        Calificacion calificacion = new Calificacion();
        calificacion.setSolicitudId(solicitudId);
        calificacion.setPuntuacion(puntuacion);
        calificacion.setComentario(comentarioLimpio);
        calificacion.setFecha(LocalDateTime.now());
        calificacionRepository.save(calificacion);

        return calificacion;
    }

    /**
     * Indica si una solicitud ya tiene calificación. La vista lo usa para
     * mostrar o esconder el botón "Calificar".
     *
     * @param solicitudId id de la solicitud
     * @return true si ya fue calificada
     */
    public boolean yaFueCalificada(Integer solicitudId) {
        return calificacionRepository.existsById(solicitudId);
    }

    /**
     * Busca las calificaciones de los acompañamientos realizados de una lista de solicitudes.
     * La vista del historial la usa para mostrar las estrellas que ya se dieron
     * o el botón "Calificar".
     *
     * @param solicitudes solicitudes del historial del estudiante
     * @return calificaciones por id de solicitud; las que no tienen calificación no aparecen
     */
    public Map<Integer, Calificacion> calificacionesDe(List<Solicitud> solicitudes) {
        List<Integer> realizadas = solicitudes.stream()
                .filter(s -> s.getEstado() == EstadoSolicitud.REALIZADA)
                .map(Solicitud::getId)
                .toList();
        Map<Integer, Calificacion> calificaciones = new HashMap<>();
        if (realizadas.isEmpty()) {
            return calificaciones;
        }
        for (Calificacion calificacion : calificacionRepository.findAllById(realizadas)) {
            calificaciones.put(calificacion.getSolicitudId(), calificacion);
        }
        return calificaciones;
    }
}
