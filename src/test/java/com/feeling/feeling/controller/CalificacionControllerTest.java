package com.feeling.feeling.controller;

import com.feeling.feeling.model.Calificacion;
import com.feeling.feeling.model.EstadoSolicitud;
import com.feeling.feeling.model.Estudiante;
import com.feeling.feeling.model.Solicitud;
import com.feeling.feeling.repository.ICalificacionRepository;
import com.feeling.feeling.repository.SolicitudRepository;
import com.feeling.feeling.service.AjusteRazonableService;
import com.feeling.feeling.service.CalificacionService;
import com.feeling.feeling.service.ContactoSolicitudService;
import com.feeling.feeling.service.PanelService;
import com.feeling.feeling.service.SeguimientoService;
import com.feeling.feeling.service.SolicitudService;
import com.feeling.feeling.service.VoluntarioService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;

/**
 * Pruebas de "Calificar" en Mis acompañamientos (RF-16, SCRUM-101).
 * Usa el servicio real de calificaciones con los repositorios simulados.
 */
class CalificacionControllerTest {

    private final SolicitudRepository solicitudRepository = mock(SolicitudRepository.class);
    private final ICalificacionRepository calificacionRepository = mock(ICalificacionRepository.class);
    private final CalificacionService calificacionService =
            new CalificacionService(solicitudRepository, calificacionRepository);
    private final MockMvc mvc = MockMvcBuilders
            .standaloneSetup(new SolicitudController(mock(SolicitudService.class), mock(VoluntarioService.class),
                    mock(PanelService.class), mock(ContactoSolicitudService.class),
                    mock(AjusteRazonableService.class), mock(SeguimientoService.class),
                    calificacionService))
            .build();

    /** Sesión del estudiante 7. */
    private MockHttpSession sesionDeEstudiante() {
        Estudiante estudiante = new Estudiante();
        estudiante.setId(7);
        MockHttpSession sesion = new MockHttpSession();
        sesion.setAttribute("usuario", estudiante);
        return sesion;
    }

    /** Solicitud 20 del estudiante 7 con el estado indicado. */
    private Solicitud solicitud(EstadoSolicitud estado) {
        Solicitud s = new Solicitud();
        s.setEstudianteId(7);
        s.setEstado(estado);
        when(solicitudRepository.findById(20)).thenReturn(Optional.of(s));
        return s;
    }

    /**
     * Caso de éxito: guarda las estrellas y el comentario de un acompañamiento realizado.
     */
    @Test
    void calificaUnAcompanamientoRealizado() throws Exception {
        solicitud(EstadoSolicitud.REALIZADA);

        mvc.perform(post("/solicitudes/20/calificar").session(sesionDeEstudiante())
                        .param("puntuacion", "4").param("comentario", "  Me explicó con paciencia  "))
                .andExpect(redirectedUrl("/mis-acompanamientos"))
                .andExpect(flash().attribute("mensajeExito",
                        "Calificaste el acompañamiento con 4 estrellas. Gracias por tu opinión."));

        ArgumentCaptor<Calificacion> guardada = ArgumentCaptor.forClass(Calificacion.class);
        verify(calificacionRepository).save(guardada.capture());
        assertEquals(Integer.valueOf(20), guardada.getValue().getSolicitudId());
        assertEquals(Integer.valueOf(4), guardada.getValue().getPuntuacion());
        assertEquals("Me explicó con paciencia", guardada.getValue().getComentario());
    }

    /**
     * Caso de fallo: el acompañamiento todavía no está realizado.
     */
    @Test
    void noPermiteCalificarSiNoEstaRealizado() throws Exception {
        solicitud(EstadoSolicitud.ACEPTADA);

        mvc.perform(post("/solicitudes/20/calificar").session(sesionDeEstudiante()).param("puntuacion", "5"))
                .andExpect(redirectedUrl("/mis-acompanamientos"))
                .andExpect(flash().attribute("mensajeError",
                        "Solo se puede calificar un acompañamiento que ya fue realizado."));

        verify(calificacionRepository, never()).save(any());
    }

    /**
     * Caso de fallo: el acompañamiento ya fue calificado.
     */
    @Test
    void noPermiteCalificarDosVeces() throws Exception {
        solicitud(EstadoSolicitud.REALIZADA);
        when(calificacionRepository.existsById(20)).thenReturn(true);

        mvc.perform(post("/solicitudes/20/calificar").session(sesionDeEstudiante()).param("puntuacion", "3"))
                .andExpect(flash().attribute("mensajeError", "Este acompañamiento ya fue calificado."));

        verify(calificacionRepository, never()).save(any());
    }

    /**
     * Caso de fallo: no eligió estrellas.
     */
    @Test
    void noPermiteCalificarSinEstrellas() throws Exception {
        solicitud(EstadoSolicitud.REALIZADA);

        mvc.perform(post("/solicitudes/20/calificar").session(sesionDeEstudiante()))
                .andExpect(flash().attribute("mensajeError", "La calificación debe ser de 1 a 5 estrellas."));

        verify(calificacionRepository, never()).save(any());
    }

    /**
     * El historial solo busca calificaciones de los acompañamientos realizados.
     */
    @Test
    void calificacionesDeSoloBuscaLosRealizados() {
        Solicitud realizada = new Solicitud();
        ReflectionTestUtils.setField(realizada, "id", 20);
        realizada.setEstado(EstadoSolicitud.REALIZADA);
        Solicitud pendiente = new Solicitud();
        ReflectionTestUtils.setField(pendiente, "id", 21);
        pendiente.setEstado(EstadoSolicitud.PENDIENTE);
        Calificacion calificacion = new Calificacion();
        calificacion.setSolicitudId(20);
        calificacion.setPuntuacion(5);
        when(calificacionRepository.findAllById(List.of(20))).thenReturn(List.of(calificacion));

        Map<Integer, Calificacion> calificaciones = calificacionService.calificacionesDe(List.of(realizada, pendiente));

        assertEquals(1, calificaciones.size());
        assertTrue(calificaciones.containsKey(20));
    }
}
