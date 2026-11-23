package com.feeling.feeling.controller;

import com.feeling.feeling.model.Administrador;
import com.feeling.feeling.model.EstadoVoluntario;
import com.feeling.feeling.model.Estudiante;
import com.feeling.feeling.model.Suspension;
import com.feeling.feeling.model.Usuario;
import com.feeling.feeling.model.Voluntario;
import com.feeling.feeling.repository.IVoluntarioRepository;
import com.feeling.feeling.repository.SuspensionRepository;
import com.feeling.feeling.repository.UsuarioRepository;
import com.feeling.feeling.service.GestionUsuariosService;
import com.feeling.feeling.service.PanelService;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

/**
 * Pruebas de la gestión de usuarios del administrador (RF-17, SCRUM-103).
 * Usa el servicio real con los repositorios simulados.
 */
class GestionUsuariosControllerTest {

    private final UsuarioRepository usuarioRepository = mock(UsuarioRepository.class);
    private final IVoluntarioRepository voluntarioRepository = mock(IVoluntarioRepository.class);
    private final SuspensionRepository suspensionRepository = mock(SuspensionRepository.class);
    private final MockMvc mvc = MockMvcBuilders
            .standaloneSetup(new AdminController(mock(PanelService.class),
                    new GestionUsuariosService(usuarioRepository, voluntarioRepository, suspensionRepository)))
            .build();

    /** Sesión del administrador. */
    private MockHttpSession sesionDeAdministrador() {
        Administrador admin = new Administrador();
        admin.setId(1);
        admin.setNombreUsuario("admin");
        MockHttpSession sesion = new MockHttpSession();
        sesion.setAttribute("usuario", admin);
        return sesion;
    }

    /** Voluntario 3 con el estado indicado. */
    private Voluntario voluntario(EstadoVoluntario estado) {
        Voluntario voluntario = new Voluntario();
        ReflectionTestUtils.setField(voluntario, "id", 3);
        voluntario.setEstado(estado);
        when(voluntarioRepository.findById(3)).thenReturn(Optional.of(voluntario));
        return voluntario;
    }

    /**
     * La página lista los usuarios con su rol y la suspensión vigente de cada voluntario suspendido.
     */
    @Test
    void muestraLosUsuariosConSuRol() throws Exception {
        Administrador admin = new Administrador();
        admin.setId(1);
        Estudiante estudiante = new Estudiante();
        estudiante.setId(2);
        Voluntario suspendido = voluntario(EstadoVoluntario.SUSPENDIDO);
        Suspension suspension = new Suspension(suspendido, admin, "Faltó a dos acompañamientos", LocalDate.now().plusDays(5));
        when(usuarioRepository.findAll()).thenReturn(List.<Usuario>of(admin, estudiante));
        when(voluntarioRepository.findAll()).thenReturn(List.of(suspendido));
        when(suspensionRepository.findFirstByVoluntarioIdOrderByIdDesc(3)).thenReturn(Optional.of(suspension));

        mvc.perform(get("/admin/usuarios").session(sesionDeAdministrador()))
                .andExpect(view().name("paginas/admin-usuarios"))
                .andExpect(model().attribute("roles", Map.of(1, "Administrador", 2, "Estudiante")))
                .andExpect(model().attribute("suspensiones", Map.of(3, suspension)));
    }

    /**
     * Caso de éxito: suspende al voluntario con motivo y fecha de fin.
     */
    @Test
    void suspendeAUnVoluntario() throws Exception {
        Voluntario voluntario = voluntario(EstadoVoluntario.ACTIVO);

        mvc.perform(post("/admin/usuarios/3/suspender").session(sesionDeAdministrador())
                        .param("motivo", "Faltó a dos acompañamientos")
                        .param("fechaFin", LocalDate.now().plusDays(7).toString()))
                .andExpect(redirectedUrl("/admin/usuarios"))
                .andExpect(flash().attribute("exito", "El voluntario fue suspendido."));

        assertEquals(EstadoVoluntario.SUSPENDIDO, voluntario.getEstado());
        verify(suspensionRepository).save(any());
    }

    /**
     * Caso de fallo: sin motivo no se suspende.
     */
    @Test
    void noSuspendeSinMotivo() throws Exception {
        Voluntario voluntario = voluntario(EstadoVoluntario.ACTIVO);

        mvc.perform(post("/admin/usuarios/3/suspender").session(sesionDeAdministrador())
                        .param("fechaFin", LocalDate.now().plusDays(7).toString()))
                .andExpect(flash().attribute("error", "Debe indicar el motivo de la suspensión."));

        assertEquals(EstadoVoluntario.ACTIVO, voluntario.getEstado());
        verify(suspensionRepository, never()).save(any());
    }

    /**
     * Caso de fallo: la fecha de fin no es posterior a hoy.
     */
    @Test
    void noSuspendeConFechaPasada() throws Exception {
        voluntario(EstadoVoluntario.ACTIVO);

        mvc.perform(post("/admin/usuarios/3/suspender").session(sesionDeAdministrador())
                        .param("motivo", "Faltó a dos acompañamientos")
                        .param("fechaFin", LocalDate.now().toString()))
                .andExpect(flash().attribute("error", "La fecha de fin debe ser posterior a hoy."));

        verify(suspensionRepository, never()).save(any());
    }

    /**
     * Caso de fallo: el motivo pasa del largo de la base de datos.
     */
    @Test
    void noSuspendeConMotivoMuyLargo() throws Exception {
        voluntario(EstadoVoluntario.ACTIVO);

        mvc.perform(post("/admin/usuarios/3/suspender").session(sesionDeAdministrador())
                        .param("motivo", "a".repeat(256))
                        .param("fechaFin", LocalDate.now().plusDays(7).toString()))
                .andExpect(flash().attribute("error", "El motivo puede tener máximo 255 caracteres."));

        verify(suspensionRepository, never()).save(any());
    }

    /**
     * Reactivar deja al voluntario activo otra vez.
     */
    @Test
    void reactivaAUnVoluntarioSuspendido() throws Exception {
        Voluntario voluntario = voluntario(EstadoVoluntario.SUSPENDIDO);

        mvc.perform(post("/admin/usuarios/3/reactivar").session(sesionDeAdministrador()))
                .andExpect(flash().attribute("exito", "El voluntario fue reactivado."));

        assertEquals(EstadoVoluntario.ACTIVO, voluntario.getEstado());
    }
}
