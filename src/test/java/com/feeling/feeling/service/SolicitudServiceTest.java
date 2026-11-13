package com.feeling.feeling.service;

import com.feeling.feeling.model.EstadoSolicitud;
import com.feeling.feeling.model.Solicitud;
import com.feeling.feeling.repository.SolicitudRepository;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Pruebas del caso de éxito y del caso de fallo de RF-12 (cancelar solicitud).
 *
 * @author Daniel Blandón
 */
class SolicitudServiceTest {

    private final SolicitudRepository repositorio = mock(SolicitudRepository.class);
    private final SolicitudService servicio = new SolicitudService(repositorio);

    /**
     * Crea una solicitud de prueba del estudiante 2 con el estado indicado.
     */
    private Solicitud solicitudCon(EstadoSolicitud estado) {
        Solicitud solicitud = new Solicitud();
        solicitud.setEstudianteId(2);
        solicitud.setEstado(estado);
        when(repositorio.findById(1)).thenReturn(Optional.of(solicitud));
        when(repositorio.save(solicitud)).thenReturn(solicitud);
        return solicitud;
    }

    /**
     * Caso de éxito: una solicitud pendiente pasa a cancelada.
     */
    @Test
    void cancelaSolicitudPendiente() {
        Solicitud solicitud = solicitudCon(EstadoSolicitud.PENDIENTE);
        servicio.cancelar(1, 2);
        assertEquals(EstadoSolicitud.CANCELADA, solicitud.getEstado());
    }

    /**
     * Caso de éxito: una solicitud aceptada pasa a cancelada.
     */
    @Test
    void cancelaSolicitudAceptada() {
        Solicitud solicitud = solicitudCon(EstadoSolicitud.ACEPTADA);
        servicio.cancelar(1, 2);
        assertEquals(EstadoSolicitud.CANCELADA, solicitud.getEstado());
    }

    /**
     * Caso de fallo: una solicitud realizada no se puede cancelar y se muestra el motivo.
     */
    @Test
    void noCancelaSolicitudRealizada() {
        Solicitud solicitud = solicitudCon(EstadoSolicitud.REALIZADA);
        IllegalStateException error = assertThrows(IllegalStateException.class, () -> servicio.cancelar(1, 2));
        assertEquals("No se puede cancelar porque el acompañamiento ya fue realizado.", error.getMessage());
        assertEquals(EstadoSolicitud.REALIZADA, solicitud.getEstado());
    }

    /**
     * Caso de fallo: un estudiante no puede cancelar la solicitud de otro.
     */
    @Test
    void noCancelaSolicitudDeOtroEstudiante() {
        solicitudCon(EstadoSolicitud.PENDIENTE);
        assertThrows(IllegalArgumentException.class, () -> servicio.cancelar(1, 99));
    }
}