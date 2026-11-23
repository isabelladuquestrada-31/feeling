package com.feeling.feeling.controller;

import com.feeling.feeling.model.AjusteRazonable;
import com.feeling.feeling.model.Estudiante;
import com.feeling.feeling.model.Solicitud;
import com.feeling.feeling.model.TipoAjuste;
import com.feeling.feeling.model.Voluntario;
import com.feeling.feeling.repository.IAjusteRazonableRepository;
import com.feeling.feeling.service.AjusteRazonableService;
import com.feeling.feeling.service.PanelService;
import com.feeling.feeling.service.SolicitarApoyoService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

/**
 * Pruebas de los ajustes razonables al enviar el formulario "Solicitar apoyo" (RF-09, SCRUM-95).
 * Usa el servicio real de ajustes (SCRUM-94) con el repositorio simulado.
 *
 * @author Daniel Blandón
 */
class SolicitarApoyoControllerTest {

    private final SolicitarApoyoService solicitarApoyoService = mock(SolicitarApoyoService.class);
    private final PanelService panelService = mock(PanelService.class);
    private final IAjusteRazonableRepository ajusteRepository = mock(IAjusteRazonableRepository.class);
    private final MockMvc mvc = MockMvcBuilders
            .standaloneSetup(new SolicitarApoyoController(solicitarApoyoService, panelService,
                    new AjusteRazonableService(ajusteRepository)))
            .build();

    /**
     * Sesión de estudiante1 (usuario 2) y voluntario 1 (estudiante 5) disponible.
     */
    private MockHttpSession prepararSesion() {
        Estudiante estudiante = new Estudiante();
        estudiante.setId(2);
        Estudiante estudianteVoluntario = new Estudiante();
        estudianteVoluntario.setId(5);
        Voluntario voluntario = new Voluntario(estudianteVoluntario, List.of(), List.of());
        when(solicitarApoyoService.voluntarioActivo(1)).thenReturn(Optional.of(voluntario));

        MockHttpSession sesion = new MockHttpSession();
        sesion.setAttribute("usuario", estudiante);
        return sesion;
    }

    /**
     * Caso de fallo: marca "Otro" sin describirlo; no se crea la solicitud y se pide la descripción.
     */
    @Test
    void otroSinDetalleNoCreaLaSolicitud() throws Exception {
        MockHttpSession sesion = prepararSesion();

        mvc.perform(post("/solicitudes/nueva").session(sesion)
                        .param("voluntario", "1")
                        .param("materia", "MAT101").param("tema", "Derivadas").param("tipo", "TUTORIA")
                        .param("horario", "2026-11-30T14:00").param("modalidad", "PRESENCIAL")
                        .param("ajustes", "OTRO").param("detalleAjuste", "   "))
                .andExpect(view().name("paginas/solicitar-apoyo"))
                .andExpect(model().attribute("error", "Describe el ajuste razonable que necesitas."))
                .andExpect(model().attribute("ajustesElegidos", List.of("OTRO")));

        verify(solicitarApoyoService, never()).solicitar(anyInt(), anyInt(), anyString(), anyString(),
                anyString(), anyString(), any(), any());
        verify(ajusteRepository, never()).save(any());
    }

    /**
     * Caso de éxito: los ajustes marcados se guardan con la solicitud creada.
     */
    @Test
    void guardaLosAjustesConLaSolicitud() throws Exception {
        MockHttpSession sesion = prepararSesion();
        Solicitud creada = mock(Solicitud.class);
        when(creada.getId()).thenReturn(10);
        when(solicitarApoyoService.solicitar(anyInt(), anyInt(), anyString(), anyString(),
                anyString(), anyString(), any(), any())).thenReturn(creada);

        mvc.perform(post("/solicitudes/nueva").session(sesion)
                        .param("voluntario", "1")
                        .param("materia", "MAT101").param("tema", "Derivadas").param("tipo", "TUTORIA")
                        .param("horario", "2026-11-30T14:00").param("modalidad", "PRESENCIAL")
                        .param("ajustes", "ESPACIO_ACCESIBLE", "OTRO")
                        .param("detalleAjuste", "Letra grande en el material"))
                .andExpect(redirectedUrl("/mis-acompanamientos"));

        ArgumentCaptor<AjusteRazonable> guardados = ArgumentCaptor.forClass(AjusteRazonable.class);
        verify(ajusteRepository, times(2)).save(guardados.capture());
        AjusteRazonable espacio = guardados.getAllValues().get(0);
        AjusteRazonable otro = guardados.getAllValues().get(1);
        assertEquals(10, espacio.getSolicitudId());
        assertEquals(TipoAjuste.ESPACIO_ACCESIBLE, espacio.getTipo());
        assertNull(espacio.getDetalle());
        assertEquals(TipoAjuste.OTRO, otro.getTipo());
        assertEquals("Letra grande en el material", otro.getDetalle());
    }

    /**
     * Sin ajustes marcados la solicitud se crea igual: el paso es opcional.
     */
    @Test
    void sinAjustesTambienSeEnvia() throws Exception {
        MockHttpSession sesion = prepararSesion();
        Solicitud creada = mock(Solicitud.class);
        when(creada.getId()).thenReturn(11);
        when(solicitarApoyoService.solicitar(anyInt(), anyInt(), anyString(), anyString(),
                anyString(), anyString(), any(), any())).thenReturn(creada);

        mvc.perform(post("/solicitudes/nueva").session(sesion)
                        .param("voluntario", "1")
                        .param("materia", "MAT101").param("tema", "Derivadas").param("tipo", "TUTORIA")
                        .param("horario", "2026-11-30T14:00").param("modalidad", "VIRTUAL"))
                .andExpect(redirectedUrl("/mis-acompanamientos"));

        verify(ajusteRepository, never()).save(any());
    }
}
