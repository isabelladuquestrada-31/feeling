package com.feeling.feeling.controller;

import com.feeling.feeling.model.EstadoSolicitud;
import com.feeling.feeling.model.Estudiante;
import com.feeling.feeling.model.Solicitud;
import com.feeling.feeling.model.Voluntario;
import com.feeling.feeling.repository.ISeguimientoRepository;
import com.feeling.feeling.repository.SolicitudRepository;
import com.feeling.feeling.service.AjusteRazonableService;
import com.feeling.feeling.service.ContactoSolicitudService;
import com.feeling.feeling.service.PanelService;
import com.feeling.feeling.service.SeguimientoService;
import com.feeling.feeling.service.SolicitudService;
import com.feeling.feeling.service.VoluntarioService;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;

/**
 * Pruebas de "Marcar como realizado" en Solicitudes recibidas (RF-14, SCRUM-97).
 * Usa el servicio real de seguimiento (SCRUM-96) con los repositorios simulados.
 *
 * @author Daniel Blandón
 */
class SeguimientoControllerTest {

    private final SolicitudRepository solicitudRepository = mock(SolicitudRepository.class);
    private final ISeguimientoRepository seguimientoRepository = mock(ISeguimientoRepository.class);
    private final VoluntarioService voluntarioService = mock(VoluntarioService.class);
    private final SolicitudService solicitudService = new SolicitudService(solicitudRepository);
    private final MockMvc mvc = MockMvcBuilders
            .standaloneSetup(new SolicitudController(solicitudService, voluntarioService,
                    mock(PanelService.class), mock(ContactoSolicitudService.class),
                    mock(AjusteRazonableService.class),
                    new SeguimientoService(solicitudRepository, seguimientoRepository)))
            .build();

    /**
     * Sesión de voluntario1: el estudiante 5 es el voluntario 1.
     */
    private MockHttpSession sesionDeVoluntario() {
        Estudiante estudiante = new Estudiante();
        estudiante.setId(5);
        Voluntario voluntario = mock(Voluntario.class);
        when(voluntario.getId()).thenReturn(1);
        when(voluntarioService.buscarPorEstudiante(5)).thenReturn(Optional.of(voluntario));
        MockHttpSession sesion = new MockHttpSession();
        sesion.setAttribute("usuario", estudiante);
        return sesion;
    }

    /** Solicitud 10 del voluntario 1 con el estado y la fecha indicados. */
    private Solicitud solicitud(EstadoSolicitud estado, LocalDateTime fecha) {
        Solicitud s = new Solicitud();
        s.setVoluntarioId(1);
        s.setEstado(estado);
        s.setFechaProgramada(fecha);
        when(solicitudRepository.findById(10)).thenReturn(Optional.of(s));
        return s;
    }

    /**
     * Caso de éxito: queda REALIZADA y la duración pasa a la solicitud, para sumar las horas (RF-15).
     */
    @Test
    void marcaComoRealizadaYGuardaLaDuracion() throws Exception {
        MockHttpSession sesion = sesionDeVoluntario();
        Solicitud s = solicitud(EstadoSolicitud.ACEPTADA, LocalDateTime.now().minusDays(1));

        mvc.perform(post("/solicitudes/10/realizada").session(sesion)
                        .param("duracionMinutos", "90").param("notas", "Repasamos derivadas"))
                .andExpect(redirectedUrl("/solicitudes-recibidas"))
                .andExpect(flash().attribute("mensajeExito",
                        "Registraste el acompañamiento como realizado (1 h 30 min). Ya suma a tus horas de voluntariado."));

        assertEquals(EstadoSolicitud.REALIZADA, s.getEstado());
        assertEquals(Integer.valueOf(90), s.getDuracionMinutos());
        verify(seguimientoRepository).save(any());
    }

    /**
     * Caso de fallo: la fecha del acompañamiento no ha llegado; no se guarda nada.
     */
    @Test
    void noPermiteMarcarSiLaFechaNoHaLlegado() throws Exception {
        MockHttpSession sesion = sesionDeVoluntario();
        Solicitud s = solicitud(EstadoSolicitud.ACEPTADA, LocalDateTime.now().plusDays(2));

        mvc.perform(post("/solicitudes/10/realizada").session(sesion).param("duracionMinutos", "60"))
                .andExpect(redirectedUrl("/solicitudes-recibidas"))
                .andExpect(flash().attribute("mensajeError",
                        "Todavía no se puede marcar como realizado porque la fecha del acompañamiento no ha llegado."));

        assertEquals(EstadoSolicitud.ACEPTADA, s.getEstado());
        verify(seguimientoRepository, never()).save(any());
    }

    /**
     * Caso de fallo: la solicitud no ha sido aceptada.
     */
    @Test
    void noPermiteMarcarUnaSolicitudPendiente() throws Exception {
        MockHttpSession sesion = sesionDeVoluntario();
        solicitud(EstadoSolicitud.PENDIENTE, LocalDateTime.now().minusDays(1));

        mvc.perform(post("/solicitudes/10/realizada").session(sesion).param("duracionMinutos", "60"))
                .andExpect(flash().attribute("mensajeError",
                        "No se puede marcar como realizado porque la solicitud todavía no ha sido aceptada."));

        verify(seguimientoRepository, never()).save(any());
    }

    /**
     * Solo aparecen por cerrar los aceptados cuya fecha ya pasó.
     */
    @Test
    void pendientesDeSeguimientoSoloTraeAceptadosConFechaPasada() {
        Solicitud pasada = new Solicitud();
        pasada.setFechaProgramada(LocalDateTime.now().minusHours(3));
        Solicitud futura = new Solicitud();
        futura.setFechaProgramada(LocalDateTime.now().plusDays(1));
        when(solicitudRepository.findByVoluntarioIdAndEstadoOrderByFechaProgramadaAsc(1, EstadoSolicitud.ACEPTADA))
                .thenReturn(List.of(pasada, futura));

        assertEquals(List.of(pasada), solicitudService.pendientesDeSeguimiento(1));
    }
}
