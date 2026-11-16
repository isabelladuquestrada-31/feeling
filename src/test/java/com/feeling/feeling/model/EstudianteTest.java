package com.feeling.feeling.model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas de la activación de un estudiante como voluntario (RF-05).
 *
 * @author Isabella Duque
 * @version 1.0
 */
class EstudianteTest {

    private final Materia materia = new Materia("sis103", "Bases de Datos");

    /**
     * Caso de éxito: elige una materia y un tipo de apoyo.
     */
    @Test
    void activarConDatosValidos() {
        Estudiante estudiante = new Estudiante();

        estudiante.activarseComoVoluntario(List.of(materia), List.of(TipoApoyo.PREPARACION_EXAMENES));

        assertNotNull(estudiante.getVoluntario());
        assertEquals(EstadoVoluntario.ACTIVO, estudiante.getVoluntario().getEstado());
    }

    /**
     * Caso de fallo: no elige ninguna materia.
     */
    @Test
    void activarSinMaterias() {
        Estudiante estudiante = new Estudiante();

        Exception error = assertThrows(IllegalArgumentException.class,
                () -> estudiante.activarseComoVoluntario(List.of(), List.of(TipoApoyo.PREPARACION_EXAMENES)));

        assertEquals("Debes elegir al menos una materia.", error.getMessage());
        assertNull(estudiante.getVoluntario());
    }

    /**
     * Caso de fallo: no elige ningún tipo de apoyo.
     */
    @Test
    void activarSinTiposApoyo() {
        Estudiante estudiante = new Estudiante();

        Exception error = assertThrows(IllegalArgumentException.class,
                () -> estudiante.activarseComoVoluntario(List.of(materia), List.of()));

        assertEquals("Debes elegir al menos un tipo de apoyo.", error.getMessage());
        assertNull(estudiante.getVoluntario());
    }

    /**
     * Caso de fallo: ya es voluntario.
     */
    @Test
    void activarDosVeces() {
        Estudiante estudiante = new Estudiante();
        estudiante.activarseComoVoluntario(List.of(materia), List.of(TipoApoyo.PREPARACION_EXAMENES));

        assertThrows(IllegalStateException.class,
                () -> estudiante.activarseComoVoluntario(List.of(materia), List.of(TipoApoyo.PREPARACION_EXAMENES)));
    }
}