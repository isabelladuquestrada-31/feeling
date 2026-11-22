package com.feeling.feeling.service;

import com.feeling.feeling.model.Administrador;
import com.feeling.feeling.model.Estudiante;
import com.feeling.feeling.model.Materia;
import com.feeling.feeling.model.TipoApoyo;
import com.feeling.feeling.repository.IMateriaRepository;
import com.feeling.feeling.repository.IVoluntarioRepository;
import com.feeling.feeling.repository.SolicitudRepository;
import com.feeling.feeling.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import com.feeling.feeling.model.EstadoSolicitud;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Pruebas de la consulta del perfil de voluntario que usa la pantalla "Ser voluntario" (RF-05).
 *
 * @author Daniel Blandón
 */
class VoluntarioServiceTest {
    private final UsuarioRepository usuarioRepository = mock(UsuarioRepository.class);
    private final SolicitudRepository solicitudRepository = mock(SolicitudRepository.class);
    private final VoluntarioService servicio = new VoluntarioService(
            usuarioRepository, mock(IMateriaRepository.class), mock(IVoluntarioRepository.class),
            solicitudRepository);
    /**
     * Si el estudiante ya se activó, se devuelve su perfil con sus materias y tipos de apoyo.
     */
    @Test
    void devuelveElPerfilDeUnEstudianteVoluntario() {
        Estudiante estudiante = new Estudiante();
        estudiante.activarseComoVoluntario(List.of(new Materia("MAT103", "Álgebra Lineal")),
                List.of(TipoApoyo.TUTORIA));
        when(usuarioRepository.findById(3)).thenReturn(Optional.of(estudiante));

        var voluntario = servicio.buscarPorEstudiante(3).orElseThrow();

        assertEquals("Álgebra Lineal", voluntario.getMaterias().get(0).getNombre());
        assertEquals(List.of(TipoApoyo.TUTORIA), voluntario.getTiposApoyo());
    }

    /**
     * Un estudiante que todavía no se activa no tiene perfil: la vista muestra el formulario.
     */
    @Test
    void estudianteSinActivarNoTienePerfil() {
        when(usuarioRepository.findById(3)).thenReturn(Optional.of(new Estudiante()));

        assertTrue(servicio.buscarPorEstudiante(3).isEmpty());
    }

    /**
     * Un administrador o un usuario que no existe tampoco tienen perfil de voluntario.
     */
    @Test
    void administradorOUsuarioInexistenteNoTienenPerfil() {
        when(usuarioRepository.findById(1)).thenReturn(Optional.of(new Administrador()));
        when(usuarioRepository.findById(99)).thenReturn(Optional.empty());

        assertTrue(servicio.buscarPorEstudiante(1).isEmpty());
        assertTrue(servicio.buscarPorEstudiante(99).isEmpty());
    }
    /**
     * RF-15, caso de éxito: si el voluntario tiene acompañamientos realizados,
     * se suman sus minutos y se pasan a horas (90 minutos = 1.5 horas).
     */
    @Test
    void calcularHorasConAcompanamientosRealizados() {
        when(solicitudRepository.sumarMinutosPorVoluntarioYEstado(1, EstadoSolicitud.REALIZADA))
                .thenReturn(90L);

        double horas = servicio.calcularHorasVoluntariado(1);

        assertEquals(1.5, horas);
    }

    /**
     * RF-15, caso de fallo: si el voluntario no tiene acompañamientos realizados,
     * la suma llega vacía (null) y el servicio devuelve 0 horas.
     */
    @Test
    void calcularHorasSinAcompanamientosRealizados() {
        when(solicitudRepository.sumarMinutosPorVoluntarioYEstado(1, EstadoSolicitud.REALIZADA))
                .thenReturn(null);

        double horas = servicio.calcularHorasVoluntariado(1);

        assertEquals(0, horas);
    }
}
