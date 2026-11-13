package com.feeling.feeling.service;

import com.feeling.feeling.model.EstadoSolicitud;
import com.feeling.feeling.model.Solicitud;
import com.feeling.feeling.repository.SolicitudRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
}