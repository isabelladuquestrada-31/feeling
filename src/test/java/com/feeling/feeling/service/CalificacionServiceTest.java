package com.feeling.feeling.service;

import com.feeling.feeling.model.Calificacion;
import com.feeling.feeling.model.EstadoSolicitud;
import com.feeling.feeling.model.Solicitud;
import com.feeling.feeling.repository.ICalificacionRepository;
import com.feeling.feeling.repository.SolicitudRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CalificacionServiceTest {

    private SolicitudRepository solicitudRepository;
    private ICalificacionRepository calificacionRepository;
    private CalificacionService service;

    @BeforeEach
    void preparar() {
        solicitudRepository = mock(SolicitudRepository.class);
        calificacionRepository = mock(ICalificacionRepository.class);
        service = new CalificacionService(solicitudRepository, calificacionRepository);
    }

    /** Crea una solicitud del estudiante 2 con el estado indicado. */
    private Solicitud solicitud(EstadoSolicitud estado) {
        Solicitud s = new Solicitud();
        s.setEstudianteId(2);
        s.setEstado(estado);
        return s;
    }

    /** Comprueba que calificar falla con el error esperado y no guarda nada. */
    private void esperarError(Class<? extends Throwable> tipoError, final Integer estudianteId,
                              final Integer puntuacion, final String comentario) {
        assertThrows(tipoError, new Executable() {
            @Override
            public void execute() {
                service.calificar(10, estudianteId, puntuacion, comentario);
            }
        });
        verify(calificacionRepository, never()).save(any(Calificacion.class));
    }

    @Test
    void casoDeExitoConComentario() {
        when(solicitudRepository.findById(10)).thenReturn(Optional.of(solicitud(EstadoSolicitud.REALIZADA)));

        Calificacion resultado = service.calificar(10, 2, 5, "Me explicó muy bien");

        assertEquals(Integer.valueOf(10), resultado.getSolicitudId());
        assertEquals(Integer.valueOf(5), resultado.getPuntuacion());
        assertEquals("Me explicó muy bien", resultado.getComentario());
        verify(calificacionRepository).save(any(Calificacion.class));
    }

    @Test
    void casoDeExitoSinComentario() {
        when(solicitudRepository.findById(10)).thenReturn(Optional.of(solicitud(EstadoSolicitud.REALIZADA)));

        Calificacion resultado = service.calificar(10, 2, 4, "   ");

        assertEquals(Integer.valueOf(4), resultado.getPuntuacion());
        assertNull(resultado.getComentario());
        verify(calificacionRepository).save(any(Calificacion.class));
    }

    @Test
    void casoDeFalloNoEstaRealizado() {
        when(solicitudRepository.findById(10)).thenReturn(Optional.of(solicitud(EstadoSolicitud.ACEPTADA)));
        esperarError(IllegalStateException.class, 2, 5, null);
    }

    @Test
    void casoDeFalloYaFueCalificado() {
        when(solicitudRepository.findById(10)).thenReturn(Optional.of(solicitud(EstadoSolicitud.REALIZADA)));
        when(calificacionRepository.existsById(10)).thenReturn(true);
        esperarError(IllegalStateException.class, 2, 5, null);
    }

    @Test
    void casoDeFalloOtroEstudiante() {
        when(solicitudRepository.findById(10)).thenReturn(Optional.of(solicitud(EstadoSolicitud.REALIZADA)));
        esperarError(IllegalArgumentException.class, 3, 5, null);
    }

    @Test
    void casoDeFalloSolicitudNoExiste() {
        when(solicitudRepository.findById(10)).thenReturn(Optional.<Solicitud>empty());
        esperarError(IllegalArgumentException.class, 2, 5, null);
    }

    @Test
    void casoDeFalloPuntuacionMenorQueUno() {
        when(solicitudRepository.findById(10)).thenReturn(Optional.of(solicitud(EstadoSolicitud.REALIZADA)));
        esperarError(IllegalArgumentException.class, 2, 0, null);
    }

    @Test
    void casoDeFalloPuntuacionMayorQueCinco() {
        when(solicitudRepository.findById(10)).thenReturn(Optional.of(solicitud(EstadoSolicitud.REALIZADA)));
        esperarError(IllegalArgumentException.class, 2, 6, null);
    }

    @Test
    void casoDeFalloComentarioMuyLargo() {
        when(solicitudRepository.findById(10)).thenReturn(Optional.of(solicitud(EstadoSolicitud.REALIZADA)));
        esperarError(IllegalArgumentException.class, 2, 5, "x".repeat(256));
    }
}