package com.feeling.feeling.controller;

import com.feeling.feeling.model.Estudiante;
import com.feeling.feeling.model.PreferenciasAccesibilidad;
import com.feeling.feeling.service.PreferenciasService;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;

/**
 * Pruebas del guardado de las preferencias de accesibilidad desde "Mi perfil" (RF-04).
 *
 * @author Daniel Blandón
 */
class AccesibilidadControllerTest {

    private final PreferenciasService servicio = mock(PreferenciasService.class);
    private final MockMvc mvc = MockMvcBuilders
            .standaloneSetup(new AccesibilidadController(servicio))
            .build();

    /**
     * Crea una sesión con estudiante1 (usuario 2), como si hubiera iniciado sesión.
     */
    private MockHttpSession sesionDeEstudiante() {
        Estudiante estudiante = new Estudiante();
        estudiante.setId(2);
        MockHttpSession sesion = new MockHttpSession();
        sesion.setAttribute("usuario", estudiante);
        return sesion;
    }

    /**
     * Caso de éxito: se guardan y quedan en la sesión para aplicarse en todas las páginas.
     */
    @Test
    void guardaYActualizaLaSesion() throws Exception {
        PreferenciasAccesibilidad guardadas = new PreferenciasAccesibilidad();
        guardadas.setLetraGrande(true);
        when(servicio.guardar(2, true, false)).thenReturn(guardadas);
        MockHttpSession sesion = sesionDeEstudiante();

        mvc.perform(post("/perfil/accesibilidad").session(sesion).param("letraGrande", "on"))
                .andExpect(redirectedUrl("/perfil"))
                .andExpect(flash().attributeExists("mensaje"));

        assertSame(guardadas, sesion.getAttribute(PreferenciasService.ATRIBUTO_SESION));
    }

    /**
     * Caso de fallo: si la base de datos falla, se avisa y la sesión no cambia.
     */
    @Test
    void siNoSePuedeGuardarAvisaYDejaLasAnteriores() throws Exception {
        when(servicio.guardar(2, true, true))
                .thenThrow(new DataAccessResourceFailureException("Sin conexión con la base de datos"));
        MockHttpSession sesion = sesionDeEstudiante();

        mvc.perform(post("/perfil/accesibilidad").session(sesion)
                        .param("letraGrande", "on")
                        .param("altoContraste", "on"))
                .andExpect(redirectedUrl("/perfil"))
                .andExpect(flash().attributeExists("errorAccesibilidad"));

        assertNull(sesion.getAttribute(PreferenciasService.ATRIBUTO_SESION));
    }

    /**
     * Sin sesión iniciada se va al login.
     */
    @Test
    void sinSesionVaAlLogin() throws Exception {
        mvc.perform(post("/perfil/accesibilidad"))
                .andExpect(redirectedUrl("/login"));
    }
}