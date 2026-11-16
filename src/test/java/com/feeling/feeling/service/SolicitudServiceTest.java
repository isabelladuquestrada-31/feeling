package com.feeling.feeling.service;

import com.feeling.feeling.model.EstadoSolicitud;
import com.feeling.feeling.model.Solicitud;
import com.feeling.feeling.repository.SolicitudRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * * Pruebas de RF-08 (registrar solicitud), RF-12 (cancelar solicitud) y RF-13 (próximos acompañamientos e historial).
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

    /**
     * Caso de éxito RF-13: el estudiante ve sus próximos acompañamientos aceptados.
     */
    @Test
    void muestraProximosAcompanamientos() {
        Solicitud aceptada = new Solicitud();
        aceptada.setEstado(EstadoSolicitud.ACEPTADA);
        when(repositorio.findByEstudianteIdAndEstadoAndFechaProgramadaAfterOrderByFechaProgramadaAsc(
                eq(2), eq(EstadoSolicitud.ACEPTADA), any(LocalDateTime.class))).thenReturn(List.of(aceptada));

        List<Solicitud> proximos = servicio.proximosDelEstudiante(2);

        assertEquals(1, proximos.size());
        assertEquals(EstadoSolicitud.ACEPTADA, proximos.get(0).getEstado());
    }

    /**
     * Caso de fallo RF-13: un estudiante sin solicitudes recibe una lista vacía y la vista muestra el mensaje.
     */
    @Test
    void historialVacioSinSolicitudes() {
        when(repositorio.findByEstudianteIdOrderByFechaCreacionDesc(3)).thenReturn(List.of());

        assertTrue(servicio.historialDelEstudiante(3).isEmpty());
    }

    /**
     * Crea una solicitud completa y válida para probar RF-08 (estudiante 2, voluntario 5).
     */
    private Solicitud solicitudParaRegistrar() {
        Solicitud solicitud = new Solicitud();
        solicitud.setEstudianteId(2);
        solicitud.setVoluntarioId(5);
        solicitud.setMateriaCodigo("CAL101");
        solicitud.setTema("Derivadas");
        solicitud.setTipo("EXPLICACION");
        solicitud.setModalidad("PRESENCIAL");
        solicitud.setFechaProgramada(LocalDateTime.now().plusDays(1));
        return solicitud;
    }

    /**
     * Caso de éxito RF-08: una solicitud completa y con la franja libre se guarda en estado PENDIENTE.
     */
    @Test
    void registraSolicitudPendiente() {
        Solicitud solicitud = solicitudParaRegistrar();
        when(repositorio.existsByVoluntarioIdAndFechaProgramadaAndEstadoIn(
                eq(5), eq(solicitud.getFechaProgramada()), anyList())).thenReturn(false);
        when(repositorio.save(solicitud)).thenReturn(solicitud);

        Solicitud guardada = servicio.registrar(solicitud);

        assertEquals(EstadoSolicitud.PENDIENTE, guardada.getEstado());
        verify(repositorio).save(solicitud);
    }

    /**
     * Caso de fallo RF-08: si falta un campo obligatorio no se guarda y se avisa el motivo.
     */
    @Test
    void noRegistraSolicitudSinMateria() {
        final Solicitud solicitud = solicitudParaRegistrar();
        solicitud.setMateriaCodigo("");

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class, new Executable() {
            @Override
            public void execute() {
                servicio.registrar(solicitud);
            }
        });

        assertEquals("Faltan campos obligatorios de la solicitud.", error.getMessage());
        verify(repositorio, never()).save(any(Solicitud.class));
    }

    /**
     * Caso de fallo RF-08: si el voluntario ya tiene una solicitud en esa fecha y hora no se guarda.
     */
    @Test
    void noRegistraSolicitudConFranjaOcupada() {
        final Solicitud solicitud = solicitudParaRegistrar();
        when(repositorio.existsByVoluntarioIdAndFechaProgramadaAndEstadoIn(
                eq(5), eq(solicitud.getFechaProgramada()), anyList())).thenReturn(true);

        IllegalStateException error = assertThrows(IllegalStateException.class, new Executable() {
            @Override
            public void execute() {
                servicio.registrar(solicitud);
            }
        });

        assertEquals("El voluntario ya tiene una solicitud en esa franja.", error.getMessage());
        verify(repositorio, never()).save(any(Solicitud.class));
    }

    /**
     * Crea una solicitud de prueba del voluntario 5 con el estado indicado.
     */
    private Solicitud solicitudDelVoluntario(EstadoSolicitud estado) {
        Solicitud solicitud = new Solicitud();
        solicitud.setVoluntarioId(5);
        solicitud.setEstado(estado);
        when(repositorio.findById(1)).thenReturn(Optional.of(solicitud));
        when(repositorio.save(solicitud)).thenReturn(solicitud);
        return solicitud;
    }

    /**
     * Caso de éxito: el voluntario acepta una solicitud pendiente.
     */
    @Test
    void aceptaSolicitudPendiente() {
        Solicitud solicitud = solicitudDelVoluntario(EstadoSolicitud.PENDIENTE);
        servicio.aceptar(1, 5);
        assertEquals(EstadoSolicitud.ACEPTADA, solicitud.getEstado());
    }

    /**
     * Caso de éxito: el voluntario rechaza una solicitud pendiente.
     */
    @Test
    void rechazaSolicitudPendiente() {
        Solicitud solicitud = solicitudDelVoluntario(EstadoSolicitud.PENDIENTE);
        servicio.rechazar(1, 5);
        assertEquals(EstadoSolicitud.RECHAZADA, solicitud.getEstado());
    }

    /**
     * Caso de fallo: no se puede aceptar una solicitud que el estudiante ya canceló.
     */
    @Test
    void noAceptaSolicitudCancelada() {
        Solicitud solicitud = solicitudDelVoluntario(EstadoSolicitud.CANCELADA);

        IllegalStateException error = assertThrows(IllegalStateException.class, new Executable() {
            @Override
            public void execute() {
                servicio.aceptar(1, 5);
            }
        });

        assertEquals("No se puede responder porque el estudiante canceló la solicitud.", error.getMessage());
        assertEquals(EstadoSolicitud.CANCELADA, solicitud.getEstado());
        verify(repositorio, never()).save(any(Solicitud.class));
    }

    /**
     * Caso de fallo: no se puede rechazar una solicitud que ya fue aceptada.
     */
    @Test
    void noRechazaSolicitudYaAceptada() {
        Solicitud solicitud = solicitudDelVoluntario(EstadoSolicitud.ACEPTADA);

        IllegalStateException error = assertThrows(IllegalStateException.class, new Executable() {
            @Override
            public void execute() {
                servicio.rechazar(1, 5);
            }
        });

        assertEquals("No se puede responder porque la solicitud ya fue aceptada.", error.getMessage());
        assertEquals(EstadoSolicitud.ACEPTADA, solicitud.getEstado());
    }

    /**
     * Caso de fallo: un voluntario no puede responder la solicitud de otro.
     */
    @Test
    void noAceptaSolicitudDeOtroVoluntario() {
        solicitudDelVoluntario(EstadoSolicitud.PENDIENTE);

        assertThrows(IllegalArgumentException.class, new Executable() {
            @Override
            public void execute() {
                servicio.aceptar(1, 99);
            }
        });
        verify(repositorio, never()).save(any(Solicitud.class));
    }

    /**
     * Crea una solicitud del estudiante 2 con estado y fecha programada.
     */
    private Solicitud solicitudProgramada(String tema, EstadoSolicitud estado, LocalDateTime fecha) {
        Solicitud solicitud = new Solicitud();
        solicitud.setEstudianteId(2);
        solicitud.setTema(tema);
        solicitud.setEstado(estado);
        solicitud.setFechaProgramada(fecha);
        return solicitud;
    }

    /**
     * SCRUM-53: el inicio muestra solo las pendientes y aceptadas que no han pasado,
     * de la más cercana a la más lejana y sin pasar del límite.
     */
    @Test
    void proximasSolicitudesFiltraOrdenaYLimita() {
        LocalDateTime ahora = LocalDateTime.now();
        when(repositorio.findByEstudianteIdOrderByFechaCreacionDesc(2)).thenReturn(List.of(
                solicitudProgramada("lejana", EstadoSolicitud.PENDIENTE, ahora.plusDays(9)),
                solicitudProgramada("cancelada", EstadoSolicitud.CANCELADA, ahora.plusDays(1)),
                solicitudProgramada("pasada", EstadoSolicitud.ACEPTADA, ahora.minusDays(1)),
                solicitudProgramada("cercana", EstadoSolicitud.ACEPTADA, ahora.plusDays(2)),
                solicitudProgramada("media", EstadoSolicitud.PENDIENTE, ahora.plusDays(5)),
                solicitudProgramada("rechazada", EstadoSolicitud.RECHAZADA, ahora.plusDays(3))));

        List<Solicitud> proximas = servicio.proximasSolicitudesDelEstudiante(2, 2);

        assertEquals(2, proximas.size());
        assertEquals("cercana", proximas.get(0).getTema());
        assertEquals("media", proximas.get(1).getTema());
    }

    /**
     * SCRUM-53: si el estudiante no tiene solicitudes, el inicio recibe una lista vacía.
     */
    @Test
    void proximasSolicitudesVaciaSinSolicitudes() {
        when(repositorio.findByEstudianteIdOrderByFechaCreacionDesc(2)).thenReturn(List.of());

        assertTrue(servicio.proximasSolicitudesDelEstudiante(2, 3).isEmpty());
    }
}
