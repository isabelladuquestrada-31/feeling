package com.feeling.feeling.service;

import com.feeling.feeling.model.EstadoSolicitud;
import com.feeling.feeling.model.Seguimiento;
import com.feeling.feeling.model.Solicitud;
import com.feeling.feeling.repository.ISeguimientoRepository;
import com.feeling.feeling.repository.SolicitudRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SeguimientoServiceTest {

    private SolicitudRepository solicitudRepository;
    private ISeguimientoRepository seguimientoRepository;
    private SeguimientoService service;

    @BeforeEach
    void preparar() {
        solicitudRepository = mock(SolicitudRepository.class);
        seguimientoRepository = mock(ISeguimientoRepository.class);
        service = new SeguimientoService(solicitudRepository, seguimientoRepository);
    }

    /** Crea una solicitud del voluntario 1 con el estado y la fecha indicados. */
    private Solicitud solicitud(EstadoSolicitud estado, LocalDateTime fecha) {
        Solicitud s = new Solicitud();
        s.setVoluntarioId(1);
        s.setEstado(estado);
        s.setFechaProgramada(fecha);
        return s;
    }

    /** Comprueba que marcar como realizada falla con el error esperado y no guarda nada. */
    private void esperarError(Class<? extends Throwable> tipoError, final Integer voluntarioId,
                              final Integer duracion) {
        assertThrows(tipoError, new Executable() {
            @Override
            public void execute() {
                service.marcarComoRealizada(10, voluntarioId, "Notas", duracion);
            }
        });
        verify(seguimientoRepository, never()).save(any(Seguimiento.class));
    }

    @Test
    void casoDeExitoGuardaSeguimientoYMarcaRealizada() {
        Solicitud s = solicitud(EstadoSolicitud.ACEPTADA, LocalDateTime.now().minusDays(1));
        when(solicitudRepository.findById(10)).thenReturn(Optional.of(s));

        Seguimiento resultado = service.marcarComoRealizada(10, 1, "Repasamos derivadas", 60);

        assertEquals(EstadoSolicitud.REALIZADA, s.getEstado());
        assertEquals(Integer.valueOf(10), resultado.getSolicitudId());
        assertEquals(Integer.valueOf(60), resultado.getDuracionMinutos());
        assertEquals("Repasamos derivadas", resultado.getNotas());
        verify(seguimientoRepository).save(any(Seguimiento.class));
        verify(solicitudRepository).save(s);
    }

    @Test
    void casoDeFalloSolicitudNoAceptada() {
        Solicitud s = solicitud(EstadoSolicitud.PENDIENTE, LocalDateTime.now().minusDays(1));
        when(solicitudRepository.findById(10)).thenReturn(Optional.of(s));
        esperarError(IllegalStateException.class, 1, 60);
        assertEquals(EstadoSolicitud.PENDIENTE, s.getEstado());
    }

    @Test
    void casoDeFalloFechaTodaviaNoLlega() {
        Solicitud s = solicitud(EstadoSolicitud.ACEPTADA, LocalDateTime.now().plusDays(1));
        when(solicitudRepository.findById(10)).thenReturn(Optional.of(s));
        esperarError(IllegalStateException.class, 1, 60);
        assertEquals(EstadoSolicitud.ACEPTADA, s.getEstado());
    }

    @Test
    void casoDeFalloYaEstabaRealizada() {
        Solicitud s = solicitud(EstadoSolicitud.REALIZADA, LocalDateTime.now().minusDays(1));
        when(solicitudRepository.findById(10)).thenReturn(Optional.of(s));
        esperarError(IllegalStateException.class, 1, 60);
    }

    @Test
    void casoDeFalloOtroVoluntario() {
        Solicitud s = solicitud(EstadoSolicitud.ACEPTADA, LocalDateTime.now().minusDays(1));
        when(solicitudRepository.findById(10)).thenReturn(Optional.of(s));
        esperarError(IllegalArgumentException.class, 2, 60);
    }

    @Test
    void casoDeFalloSolicitudNoExiste() {
        when(solicitudRepository.findById(10)).thenReturn(Optional.<Solicitud>empty());
        esperarError(IllegalArgumentException.class, 1, 60);
    }

    @Test
    void casoDeFalloDuracionInvalida() {
        Solicitud s = solicitud(EstadoSolicitud.ACEPTADA, LocalDateTime.now().minusDays(1));
        when(solicitudRepository.findById(10)).thenReturn(Optional.of(s));
        esperarError(IllegalArgumentException.class, 1, 0);
        assertEquals(EstadoSolicitud.ACEPTADA, s.getEstado());
    }

    @Test
    void casoDeFalloDuracionMayorAlMaximo() {
        Solicitud s = solicitud(EstadoSolicitud.ACEPTADA, LocalDateTime.now().minusDays(1));
        when(solicitudRepository.findById(10)).thenReturn(Optional.of(s));
        esperarError(IllegalArgumentException.class, 1, 181);
        assertEquals(EstadoSolicitud.ACEPTADA, s.getEstado());
    }
}