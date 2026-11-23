package com.feeling.feeling.service;

import com.feeling.feeling.model.EstadoSolicitud;
import com.feeling.feeling.model.Seguimiento;
import com.feeling.feeling.model.Solicitud;
import com.feeling.feeling.repository.ISeguimientoRepository;
import com.feeling.feeling.repository.SolicitudRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Lógica para registrar el seguimiento de un acompañamiento y marcarlo como realizado.
 *
 * @author Yeymi Cuellar
 */
@Service
public class SeguimientoService {

    /**
     * Máximo de minutos que se acepta para un acompañamiento.
     */
    private static final int DURACION_MAXIMA_MINUTOS = 180;

    /**
     * Repositorio para consultar y actualizar las solicitudes.
     */
    private final SolicitudRepository solicitudRepository;

    /**
     * Repositorio para guardar los seguimientos.
     */
    private final ISeguimientoRepository seguimientoRepository;

    /**
     * Crea el servicio con los repositorios que le entrega Spring.
     *
     * @param solicitudRepository   repositorio de solicitudes
     * @param seguimientoRepository repositorio de seguimientos
     */
    public SeguimientoService(SolicitudRepository solicitudRepository,
                              ISeguimientoRepository seguimientoRepository) {
        this.solicitudRepository = solicitudRepository;
        this.seguimientoRepository = seguimientoRepository;
    }

    /**
     * Guarda el seguimiento y cambia la solicitud a REALIZADA. Solo se permite si la
     * solicitud es del voluntario, estaba ACEPTADA y su fecha programada ya pasó.
     * Todo ocurre en una transacción: si algo falla, no se guarda nada.
     *
     * @param solicitudId     id de la solicitud
     * @param voluntarioId    id del voluntario que la marca como realizada
     * @param notas           notas del acompañamiento; pueden ir vacías
     * @param duracionMinutos duración del acompañamiento, entre 1 y 180 minutos
     * @return el seguimiento guardado
     * @throws IllegalArgumentException si la solicitud no existe, no es del voluntario o la duración no es válida
     * @throws IllegalStateException    si no estaba aceptada o su fecha todavía no llega
     */
    @Transactional
    public Seguimiento marcarComoRealizada(Integer solicitudId, Integer voluntarioId,
                                           String notas, Integer duracionMinutos) {
        Optional<Solicitud> encontrada = solicitudRepository.findById(solicitudId);
        if (!encontrada.isPresent()) {
            throw new IllegalArgumentException("La solicitud no existe.");
        }
        Solicitud solicitud = encontrada.get();

        if (!solicitud.getVoluntarioId().equals(voluntarioId)) {
            throw new IllegalArgumentException(
                    "Solo el voluntario al que se le hizo la solicitud puede marcarla como realizada.");
        }

        if (solicitud.getEstado() != EstadoSolicitud.ACEPTADA) {
            throw new IllegalStateException(motivoNoRealizable(solicitud.getEstado()));
        }

        if (!solicitud.getFechaProgramada().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException(
                    "Todavía no se puede marcar como realizado porque la fecha del acompañamiento no ha llegado.");
        }

        if (duracionMinutos == null || duracionMinutos <= 0 || duracionMinutos > DURACION_MAXIMA_MINUTOS) {
            throw new IllegalArgumentException(
                    "La duración debe estar entre 1 y " + DURACION_MAXIMA_MINUTOS + " minutos.");
        }

        Seguimiento seguimiento = new Seguimiento();
        seguimiento.setSolicitudId(solicitudId);
        if (notas != null && !notas.trim().isEmpty()) {
            seguimiento.setNotas(notas.trim());
        }
        seguimiento.setDuracionMinutos(duracionMinutos);
        seguimiento.setFecha(LocalDateTime.now());
        seguimientoRepository.save(seguimiento);

        solicitud.setEstado(EstadoSolicitud.REALIZADA);
        // La duración también queda en la solicitud, porque de ahí se suman las
        // horas de voluntariado (RF-15, VoluntarioService.calcularHorasVoluntariado).
        solicitud.setDuracionMinutos(duracionMinutos);
        solicitudRepository.save(solicitud);

        return seguimiento;
    }

    /**
     * Explica por qué una solicitud que no está aceptada no se puede marcar como realizada.
     */
    private String motivoNoRealizable(EstadoSolicitud estado) {
        switch (estado) {
            case PENDIENTE:
                return "No se puede marcar como realizado porque la solicitud todavía no ha sido aceptada.";
            case RECHAZADA:
                return "No se puede marcar como realizado porque la solicitud fue rechazada.";
            case CANCELADA:
                return "No se puede marcar como realizado porque el estudiante canceló la solicitud.";
            case REALIZADA:
                return "Este acompañamiento ya estaba marcado como realizado.";
            default:
                return "Este acompañamiento no se puede marcar como realizado.";
        }
    }
}