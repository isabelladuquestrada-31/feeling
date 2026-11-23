package com.feeling.feeling.controller;

import com.feeling.feeling.model.Estudiante;
import com.feeling.feeling.model.Voluntario;
import com.feeling.feeling.service.PanelService;
import com.feeling.feeling.service.SolicitudService;
import com.feeling.feeling.service.VoluntarioService;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

/**
 * Pruebas de las horas de voluntariado en "Mi perfil de voluntario" y en el inicio
 * (RF-15, SCRUM-99).
 */
class HorasVoluntariadoControllerTest {

    private final VoluntarioService voluntarioService = mock(VoluntarioService.class);
    private final MockMvc mvc = MockMvcBuilders
            .standaloneSetup(new SerVoluntarioController(voluntarioService),
                    new PanelController(mock(PanelService.class), mock(SolicitudService.class), voluntarioService))
            .build();

    /** Sesión del estudiante 5, que es el voluntario 1. */
    private MockHttpSession sesionDeVoluntario() {
        Estudiante estudiante = new Estudiante();
        estudiante.setId(5);
        Voluntario voluntario = mock(Voluntario.class);
        when(voluntario.getId()).thenReturn(1);
        when(voluntarioService.buscarPorEstudiante(5)).thenReturn(Optional.of(voluntario));
        when(voluntarioService.listarMaterias()).thenReturn(List.of());
        MockHttpSession sesion = new MockHttpSession();
        sesion.setAttribute("usuario", estudiante);
        return sesion;
    }

    /**
     * Caso de éxito: el perfil del voluntario muestra el total de horas de sus acompañamientos realizados.
     */
    @Test
    void elPerfilMuestraLasHorasAcumuladas() throws Exception {
        MockHttpSession sesion = sesionDeVoluntario();
        when(voluntarioService.calcularHorasVoluntariado(1)).thenReturn(2.5);

        mvc.perform(get("/ser-voluntario").session(sesion))
                .andExpect(view().name("paginas/ser-voluntario"))
                .andExpect(model().attribute("horasVoluntariado", 2.5));
    }

    /**
     * Caso de fallo: sin acompañamientos realizados, las horas son 0 (la vista muestra el mensaje informativo).
     */
    @Test
    void sinAcompanamientosRealizadosLasHorasSonCero() throws Exception {
        MockHttpSession sesion = sesionDeVoluntario();
        when(voluntarioService.calcularHorasVoluntariado(1)).thenReturn(0.0);

        mvc.perform(get("/ser-voluntario").session(sesion))
                .andExpect(model().attribute("horasVoluntariado", 0.0));
    }

    /**
     * El inicio también muestra las horas en la tarjeta del perfil de voluntario.
     */
    @Test
    void elInicioMuestraLasHorasDelVoluntario() throws Exception {
        MockHttpSession sesion = sesionDeVoluntario();
        when(voluntarioService.calcularHorasVoluntariado(1)).thenReturn(1.0);

        mvc.perform(get("/inicio").session(sesion))
                .andExpect(view().name("paginas/inicio-estudiante"))
                .andExpect(model().attribute("horasVoluntariado", 1.0));
    }

    /**
     * Quien todavía no es voluntario no recibe horas en el modelo.
     */
    @Test
    void sinPerfilDeVoluntarioNoHayHoras() throws Exception {
        Estudiante estudiante = new Estudiante();
        estudiante.setId(8);
        when(voluntarioService.buscarPorEstudiante(8)).thenReturn(Optional.empty());
        when(voluntarioService.listarMaterias()).thenReturn(List.of());
        MockHttpSession sesion = new MockHttpSession();
        sesion.setAttribute("usuario", estudiante);

        mvc.perform(get("/ser-voluntario").session(sesion))
                .andExpect(model().attributeDoesNotExist("horasVoluntariado"));
    }
}
