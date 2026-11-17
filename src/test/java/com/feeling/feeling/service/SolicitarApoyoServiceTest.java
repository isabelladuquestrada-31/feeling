package com.feeling.feeling.service;

import com.feeling.feeling.dto.HorarioLibre;
import com.feeling.feeling.model.DiaSemana;
import com.feeling.feeling.model.Disponibilidad;
import com.feeling.feeling.model.EstadoSolicitud;
import com.feeling.feeling.model.Estudiante;
import com.feeling.feeling.model.Materia;
import com.feeling.feeling.model.Solicitud;
import com.feeling.feeling.model.TipoApoyo;
import com.feeling.feeling.model.Voluntario;
import com.feeling.feeling.repository.DisponibilidadRepository;
import com.feeling.feeling.repository.IVoluntarioRepository;
import com.feeling.feeling.repository.SolicitudRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Pruebas del formulario "Solicitar apoyo" (RF-08, SCRUM-81): horarios libres
 * del voluntario y validaciones antes de crear la solicitud.
 *
 * @author Daniel Blandón
 */
class SolicitarApoyoServiceTest {

    /** Lunes 16 de noviembre de 2026 a las 10:00 a. m. */
    private static final LocalDateTime AHORA = LocalDateTime.of(2026, 11, 16, 10, 0);

    private final IVoluntarioRepository voluntarioRepository = mock(IVoluntarioRepository.class);
    private final DisponibilidadRepository disponibilidadRepository = mock(DisponibilidadRepository.class);
    private final SolicitudRepository solicitudRepository = mock(SolicitudRepository.class);
    private final SolicitarApoyoService servicio = new SolicitarApoyoService(voluntarioRepository,
            disponibilidadRepository, solicitudRepository, new SolicitudService(solicitudRepository));

    /**
     * Voluntario 1 (estudiante 5): domina MAT101, ofrece tutoría y atiende los lunes de 2 a 4 p. m.
     */
    @BeforeEach
    void prepararVoluntario() {
        Estudiante estudiante = new Estudiante();
        estudiante.setId(5);
        estudiante.setNombreUsuario("voluntario1");
        Voluntario voluntario = new Voluntario(estudiante,
                List.of(new Materia("MAT101", "Cálculo Diferencial")), List.of(TipoApoyo.TUTORIA));
        when(voluntarioRepository.findById(1)).thenReturn(Optional.of(voluntario));
        when(disponibilidadRepository.findByVoluntarioId(1)).thenReturn(List.of(
                new Disponibilidad(1, DiaSemana.LUNES, LocalTime.of(14, 0), LocalTime.of(16, 0))));
        when(solicitudRepository.findByVoluntarioIdAndEstadoInAndFechaProgramadaAfter(eq(1), anyList(), any()))
                .thenReturn(List.of());
        when(solicitudRepository.save(any(Solicitud.class))).thenAnswer(invocacion -> invocacion.getArgument(0));
    }

    /**
     * Crea una solicitud que ocupa el horario indicado.
     */
    private Solicitud ocupada(LocalDateTime fecha) {
        Solicitud solicitud = new Solicitud();
        solicitud.setFechaProgramada(fecha);
        return solicitud;
    }

    /**
     * Cada lunes de los próximos 14 días da un horario de 2:00 a 4:00 p. m.
     */
    @Test
    void ofreceLasFranjasDeLosProximosDias() {
        List<HorarioLibre> horarios = servicio.horariosLibres(1, AHORA);

        assertEquals(List.of(
                new HorarioLibre(LocalDateTime.of(2026, 11, 16, 14, 0), LocalDateTime.of(2026, 11, 16, 16, 0)),
                new HorarioLibre(LocalDateTime.of(2026, 11, 23, 14, 0), LocalDateTime.of(2026, 11, 23, 16, 0))),
                horarios);
    }

    /**
     * Un horario con una solicitud pendiente o aceptada, o que ya pasó, no se ofrece.
     */
    @Test
    void noOfreceHorariosOcupadosNiPasados() {
        when(solicitudRepository.findByVoluntarioIdAndEstadoInAndFechaProgramadaAfter(eq(1), anyList(), any()))
                .thenReturn(List.of(ocupada(LocalDateTime.of(2026, 11, 23, 14, 0))));

        List<HorarioLibre> horarios = servicio.horariosLibres(1, LocalDateTime.of(2026, 11, 16, 15, 0));

        assertEquals(List.of(), horarios);
    }

    /**
     * Caso de éxito: con todos los datos válidos se crea la solicitud en estado PENDIENTE.
     */
    @Test
    void creaLaSolicitudPendiente() {
        Solicitud solicitud = servicio.solicitar(3, 1, "MAT101", "  Derivadas  ", "TUTORIA", "VIRTUAL",
                LocalDateTime.of(2026, 11, 23, 14, 0), AHORA);

        assertEquals(EstadoSolicitud.PENDIENTE, solicitud.getEstado());
        assertEquals(3, solicitud.getEstudianteId());
        assertEquals(1, solicitud.getVoluntarioId());
        assertEquals("Derivadas", solicitud.getTema());
        assertEquals(LocalDateTime.of(2026, 11, 23, 14, 0), solicitud.getFechaProgramada());
    }

    /**
     * Caso de fallo: si falta un campo no se crea la solicitud y se dice qué completar.
     */
    @Test
    void noCreaSolicitudSinTema() {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class, () ->
                servicio.solicitar(3, 1, "MAT101", " ", "TUTORIA", "VIRTUAL",
                        LocalDateTime.of(2026, 11, 23, 14, 0), AHORA));

        assertEquals("Completa todos los campos: materia, tema, tipo de apoyo, horario y modalidad.",
                error.getMessage());
        verify(solicitudRepository, never()).save(any(Solicitud.class));
    }

    /**
     * Caso de fallo: si otro estudiante ya tomó el horario, no se crea y se pide elegir otro.
     */
    @Test
    void noCreaSolicitudSiElHorarioYaNoEstaDisponible() {
        when(solicitudRepository.findByVoluntarioIdAndEstadoInAndFechaProgramadaAfter(eq(1), anyList(), any()))
                .thenReturn(List.of(ocupada(LocalDateTime.of(2026, 11, 23, 14, 0))));

        IllegalStateException error = assertThrows(IllegalStateException.class, () ->
                servicio.solicitar(3, 1, "MAT101", "Derivadas", "TUTORIA", "VIRTUAL",
                        LocalDateTime.of(2026, 11, 23, 14, 0), AHORA));

        assertEquals("El horario que elegiste ya no está disponible. Elige otro.", error.getMessage());
        verify(solicitudRepository, never()).save(any(Solicitud.class));
    }

    /**
     * Caso de fallo: la materia y el tipo de apoyo deben ser de los que ofrece el voluntario.
     */
    @Test
    void noCreaSolicitudConMateriaOTipoQueNoOfrece() {
        LocalDateTime horario = LocalDateTime.of(2026, 11, 23, 14, 0);

        assertThrows(IllegalArgumentException.class, () ->
                servicio.solicitar(3, 1, "ISW203", "Normalización", "TUTORIA", "VIRTUAL", horario, AHORA));
        assertThrows(IllegalArgumentException.class, () ->
                servicio.solicitar(3, 1, "MAT101", "Derivadas", "COMPRENSION_MATERIAL", "VIRTUAL", horario, AHORA));
        verify(solicitudRepository, never()).save(any(Solicitud.class));
    }

    /**
     * Caso de fallo: un voluntario no puede pedirse apoyo a sí mismo.
     */
    @Test
    void noPuedeSolicitarseApoyoASiMismo() {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class, () ->
                servicio.solicitar(5, 1, "MAT101", "Derivadas", "TUTORIA", "VIRTUAL",
                        LocalDateTime.of(2026, 11, 23, 14, 0), AHORA));

        assertEquals("No puedes solicitarte apoyo a ti mismo.", error.getMessage());
    }
}
