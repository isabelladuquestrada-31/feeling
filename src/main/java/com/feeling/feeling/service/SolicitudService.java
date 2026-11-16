package com.feeling.feeling.service;

import com.feeling.feeling.model.EstadoSolicitud;
import com.feeling.feeling.model.Solicitud;
import com.feeling.feeling.repository.SolicitudRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Arrays;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Contiene la lógica de negocio de las solicitudes de acompañamiento.
 *
 * @author Daniel Blandón
 */
@Service
public class SolicitudService {

    /**
     * Repositorio para consultar y guardar las solicitudes.
     */
    private final SolicitudRepository solicitudRepository;

    /**
     * Crea el servicio con el repositorio que le entrega Spring.
     *
     * @param solicitudRepository repositorio de solicitudes
     */
    public SolicitudService(SolicitudRepository solicitudRepository) {
        this.solicitudRepository = solicitudRepository;
    }

    /**
     * RF-12: cancela una solicitud pendiente o aceptada. Al quedar en estado CANCELADA,
     * la franja del voluntario vuelve a estar disponible para otros estudiantes.
     *
     * @param solicitudId  id de la solicitud que se quiere cancelar
     * @param estudianteId id del estudiante que pide la cancelación
     * @return la solicitud ya cancelada
     * @throws IllegalArgumentException si la solicitud no existe o no es del estudiante
     * @throws IllegalStateException    si la solicitud ya no se puede cancelar por su estado
     */
    @Transactional
    public Solicitud cancelar(Integer solicitudId, Integer estudianteId) {
        Solicitud solicitud = solicitudRepository.findById(solicitudId)
                .orElseThrow(() -> new IllegalArgumentException("La solicitud no existe."));

        if (!solicitud.getEstudianteId().equals(estudianteId)) {
            throw new IllegalArgumentException("Solo el estudiante que hizo la solicitud puede cancelarla.");
        }

        EstadoSolicitud estado = solicitud.getEstado();
        if (estado != EstadoSolicitud.PENDIENTE && estado != EstadoSolicitud.ACEPTADA) {
            throw new IllegalStateException(motivoNoCancelable(estado));
        }

        solicitud.setEstado(EstadoSolicitud.CANCELADA);
        return solicitudRepository.save(solicitud);
    }
    /**
     * RF-xx: el voluntario acepta una solicitud que sigue pendiente.
     *
     * @param solicitudId id de la solicitud
     * @param voluntarioId id del voluntario que responde
     * @return la solicitud en estado ACEPTADA
     */
    @Transactional
    public Solicitud aceptar(Integer solicitudId, Integer voluntarioId) {
        return responder(solicitudId, voluntarioId, EstadoSolicitud.ACEPTADA);
    }

    /**
     * RF-xx: el voluntario rechaza una solicitud que sigue pendiente.
     *
     * @param solicitudId id de la solicitud
     * @param voluntarioId id del voluntario que responde
     * @return la solicitud en estado RECHAZADA
     */
    @Transactional
    public Solicitud rechazar(Integer solicitudId, Integer voluntarioId) {
        return responder(solicitudId, voluntarioId, EstadoSolicitud.RECHAZADA);
    }

    /**
     * Cambia el estado de una solicitud pendiente al estado indicado (aceptada o rechazada).
     * Falla si la solicitud no existe, es de otro voluntario o ya no está pendiente.
     */
    private Solicitud responder(Integer solicitudId, Integer voluntarioId, EstadoSolicitud nuevoEstado) {
        Solicitud solicitud = solicitudRepository.findById(solicitudId)
                .orElseThrow(() -> new IllegalArgumentException("La solicitud no existe."));

        if (!voluntarioId.equals(solicitud.getVoluntarioId())) {
            throw new IllegalArgumentException("Solo el voluntario al que se le hizo la solicitud puede responderla.");
        }
        if (solicitud.getEstado() != EstadoSolicitud.PENDIENTE) {
            throw new IllegalStateException(motivoNoRespondible(solicitud.getEstado()));
        }

        solicitud.setEstado(nuevoEstado);
        return solicitudRepository.save(solicitud);
    }

    /**
     * Explica por qué una solicitud que ya no está pendiente no se puede aceptar ni rechazar.
     */
    private String motivoNoRespondible(EstadoSolicitud estado) {
        switch (estado) {
            case CANCELADA:
                return "No se puede responder porque el estudiante canceló la solicitud.";
            case ACEPTADA:
                return "No se puede responder porque la solicitud ya fue aceptada.";
            case RECHAZADA:
                return "No se puede responder porque la solicitud ya fue rechazada.";
            case REALIZADA:
                return "No se puede responder porque el acompañamiento ya fue realizado.";
            default:
                return "No se puede responder porque la solicitud ya no está pendiente.";
        }
    }

    /**
     * Devuelve el mensaje que se le muestra al estudiante cuando no puede cancelar.
     *
     * @param estado estado actual de la solicitud
     * @return motivo por el que no se puede cancelar
     */
    private String motivoNoCancelable(EstadoSolicitud estado) {
        return switch (estado) {
            case REALIZADA -> "No se puede cancelar porque el acompañamiento ya fue realizado.";
            case RECHAZADA -> "No se puede cancelar porque el voluntario ya la rechazó.";
            case CANCELADA -> "Esta solicitud ya estaba cancelada.";
            default -> "Esta solicitud no se puede cancelar.";
        };
    }

    /**
     * RF-13: Devuelve los próximos acompañamientos aceptados del estudiante.
     * del más cercano al más lejano
     *
     * @param estudianteId id del estudiante
     * @return lista de acompañamientos aceptados que todavía no han pasado
     */
    public List<Solicitud> proximosDelEstudiante(Integer estudianteId) {
        return solicitudRepository.findByEstudianteIdAndEstadoAndFechaProgramadaAfterOrderByFechaProgramadaAsc(
                estudianteId, EstadoSolicitud.ACEPTADA, LocalDateTime.now());
    }

    /**
     * RF-13: Devuelve los próximos acompañamientos aceptados del voluntario,
     * del más cercano al más lejano
     *
     * @param voluntarioId id del voluntario
     * @return lista de acompañamientos aceptados que tadavía no han pasado
     */
    public List<Solicitud> proximosDelVoluntario(Integer voluntarioId) {
        return solicitudRepository.findByVoluntarioIdAndEstadoAndFechaProgramadaAfterOrderByFechaProgramadaAsc(
                voluntarioId, EstadoSolicitud.ACEPTADA, LocalDateTime.now());
    }

    /**
     * Inicio del estudiante (SCRUM-53): devuelve sus próximas solicitudes
     * pendientes o aceptadas, de la más cercana a la más lejana.
     *
     * @param estudianteId id del estudiante
     * @param limite      cantidad máxima de solicitudes que se devuelven
     * @return próximas solicitudes; vacía si no tiene ninguna
     */
    public List<Solicitud> proximasSolicitudesDelEstudiante(Integer estudianteId, int limite) {
        LocalDateTime ahora = LocalDateTime.now();
        return solicitudRepository.findByEstudianteIdOrderByFechaCreacionDesc(estudianteId).stream()
                .filter(s -> s.getEstado() == EstadoSolicitud.PENDIENTE || s.getEstado() == EstadoSolicitud.ACEPTADA)
                .filter(s -> s.getFechaProgramada() != null && s.getFechaProgramada().isAfter(ahora))
                .sorted(Comparator.comparing(Solicitud::getFechaProgramada))
                .limit(limite)
                .toList();
    }

    /**
     * RF-13: devuelve todas la solicitudes del estudiante, sin importar su estado,
     * de la más reciente a la más antigua.
     *
     * @param estudianteId id del estudiante
     * @return historial de solicitudes; vacío si el estudiante no a hecho ninguna
     */
    public List<Solicitud> historialDelEstudiante(Integer estudianteId) {
        return solicitudRepository.findByEstudianteIdOrderByFechaCreacionDesc(estudianteId);
    }

    /**
     * RF-10: devuelve las solicitudes pendientes que ha recibido el voluntario,
     * de la más cercana a la más lejana.
     *
     * @param voluntarioId id del voluntario
     * @return solicitudes pendientes del voluntario; vacía si no tiene ninguna
     */
    public List<Solicitud> solicitudesRecibidas(Integer voluntarioId) {
        return solicitudRepository.findByVoluntarioIdAndEstadoOrderByFechaProgramadaAsc(
                voluntarioId, EstadoSolicitud.PENDIENTE);
    }

    /**
     * RF-08: registra una solicitud de acompañamiento en estado PENDIENTE.
     * Valida que vengan los campos obligatorios y que el voluntario no tenga
     * otra solicitud pendiente o aceptada en la misma fecha y hora.
     *
     * @param solicitud solicitud a registrar
     * @return la solicitud guardada
     */
    @Transactional
    public Solicitud registrar(Solicitud solicitud) {
        if (solicitud.getEstudianteId() == null
                || solicitud.getVoluntarioId() == null
                || solicitud.getMateriaCodigo() == null || solicitud.getMateriaCodigo().isBlank()
                || solicitud.getTema() == null || solicitud.getTema().isBlank()
                || solicitud.getTipo() == null || solicitud.getTipo().isBlank()
                || solicitud.getModalidad() == null || solicitud.getModalidad().isBlank()
                || solicitud.getFechaProgramada() == null) {
            throw new IllegalArgumentException("Faltan campos obligatorios de la solicitud.");
        }

        List<EstadoSolicitud> ocupan =
                Arrays.asList(EstadoSolicitud.PENDIENTE, EstadoSolicitud.ACEPTADA);
        boolean ocupada = solicitudRepository.existsByVoluntarioIdAndFechaProgramadaAndEstadoIn(solicitud.getVoluntarioId(), solicitud.getFechaProgramada(), ocupan);
        if (ocupada) {
            throw new IllegalStateException("El voluntario ya tiene una solicitud en esa franja.");
        }

        solicitud.setEstado(EstadoSolicitud.PENDIENTE);
        return solicitudRepository.save(solicitud);
    }
}
