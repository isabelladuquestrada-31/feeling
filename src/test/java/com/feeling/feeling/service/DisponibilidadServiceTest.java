package com.feeling.feeling.service;

import com.feeling.feeling.model.DiaSemana;
import com.feeling.feeling.model.Disponibilidad;
import com.feeling.feeling.repository.DisponibilidadRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Pruebas de casos de éxito y de fallo.
 * El repositorio es simulado con Mockito, y así que no se necesita la base de datos.
 *
 * @author Isabella Duque
 * @version 1.0
 */
@ExtendWith(MockitoExtension.class)
class DisponibilidadServiceTest {

    /**
     * Repositorio simulado: no toca la base de datos, devuelve lo que cada prueba le indique.
     */
    @Mock
    private DisponibilidadRepository disponibilidadRepository;

    /**
     * Servicio que se prueba, con el repositorio simulado ya inyectado.
     */
    @InjectMocks
    private DisponibilidadService disponibilidadService;

    /**
     * Crea una franja del voluntario 1 el lunes, como si ya estuviera guardada (con id).
     *
     * @param id identificador de la franja
     * @param inicio hora de inicio en formato HH:mm
     * @param fin hora de fin en formato HH:mm
     * @return la franja creada
     */
    private Disponibilidad franja(int id, String inicio, String fin) {
        Disponibilidad d = new Disponibilidad(1, DiaSemana.LUNES, LocalTime.parse(inicio), LocalTime.parse(fin));
        d.setId(id);
        return d;
    }

    /**
     * Caso de éxito: una franja con fin posterior al inicio y sin cruces se guarda.
     */
    @Test
    void agregaFranjaValida() {
        when(disponibilidadRepository.findByVoluntarioIdAndDia(1, DiaSemana.LUNES)).thenReturn(List.of());
        when(disponibilidadRepository.save(any(Disponibilidad.class))).thenAnswer(inv -> inv.getArgument(0));

        Disponibilidad guardada = disponibilidadService.agregar(1, DiaSemana.LUNES,
                LocalTime.of(14, 0), LocalTime.of(16, 0));

        assertEquals(DiaSemana.LUNES, guardada.getDia());
        assertEquals(LocalTime.of(14, 0), guardada.getHoraInicio());
        assertEquals(LocalTime.of(16, 0), guardada.getHoraFin());
    }

    /**
     * Caso de fallo: si la hora de fin es anterior a la de inicio, no se guarda y se muestra el error.
     */
    @Test
    void rechazaHoraFinAnteriorAInicio() {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> disponibilidadService.agregar(1, DiaSemana.LUNES, LocalTime.of(16, 0), LocalTime.of(14, 0)));

        assertEquals("La hora de fin debe ser posterior a la hora de inicio.", error.getMessage());
        verify(disponibilidadRepository, never()).save(any());
    }

    /**
     * Caso de fallo: si la hora de fin es igual a la de inicio, la franja no dura nada y no se guarda.
     */
    @Test
    void rechazaHoraFinIgualAInicio() {
        assertThrows(IllegalArgumentException.class,
                () -> disponibilidadService.agregar(1, DiaSemana.LUNES, LocalTime.of(14, 0), LocalTime.of(14, 0)));
        verify(disponibilidadRepository, never()).save(any());
    }

    /**
     * Caso de fallo: una franja que se cruza con otra del mismo día no se guarda y se muestra el error.
     */
    @Test
    void rechazaFranjaQueSeCruza() {
        when(disponibilidadRepository.findByVoluntarioIdAndDia(1, DiaSemana.LUNES))
                .thenReturn(List.of(franja(10, "14:00", "16:00")));

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> disponibilidadService.agregar(1, DiaSemana.LUNES, LocalTime.of(15, 0), LocalTime.of(17, 0)));

        assertEquals("La franja se cruza con otra que ya registraste ese día.", error.getMessage());
        verify(disponibilidadRepository, never()).save(any());
    }

    /**
     * Caso de éxito: una franja que empieza justo cuando termina otra (16:00) no cuenta como cruce.
     */
    @Test
    void permiteFranjasSeguidas() {
        when(disponibilidadRepository.findByVoluntarioIdAndDia(1, DiaSemana.LUNES))
                .thenReturn(List.of(franja(10, "14:00", "16:00")));
        when(disponibilidadRepository.save(any(Disponibilidad.class))).thenAnswer(inv -> inv.getArgument(0));

        Disponibilidad guardada = disponibilidadService.agregar(1, DiaSemana.LUNES,
                LocalTime.of(16, 0), LocalTime.of(18, 0));

        assertEquals(LocalTime.of(16, 0), guardada.getHoraInicio());
    }

    /**
     * Caso de éxito: al editar una franja, no se compara consigo misma al buscar cruces.
     */
    @Test
    void editarNoSeComparaConsigoMisma() {
        Disponibilidad existente = franja(10, "14:00", "16:00");
        when(disponibilidadRepository.findById(10)).thenReturn(Optional.of(existente));
        when(disponibilidadRepository.findByVoluntarioIdAndDia(1, DiaSemana.LUNES)).thenReturn(List.of(existente));
        when(disponibilidadRepository.save(any(Disponibilidad.class))).thenAnswer(inv -> inv.getArgument(0));

        Disponibilidad editada = disponibilidadService.editar(10, 1, DiaSemana.LUNES,
                LocalTime.of(14, 0), LocalTime.of(17, 0));

        assertEquals(LocalTime.of(17, 0), editada.getHoraFin());
    }

    /**
     * Caso de fallo: un voluntario no puede eliminar una franja que es de otro voluntario.
     */
    @Test
    void noPermiteEliminarFranjaDeOtroVoluntario() {
        when(disponibilidadRepository.findById(10)).thenReturn(Optional.of(franja(10, "14:00", "16:00")));

        assertThrows(IllegalArgumentException.class, () -> disponibilidadService.eliminar(10, 2));
        verify(disponibilidadRepository, never()).delete(any());
    }
}