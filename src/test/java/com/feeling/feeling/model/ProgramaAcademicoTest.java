package com.feeling.feeling.model;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas de la lista de programas académicos que usan el registro y la búsqueda de voluntarios.
 *
 * @author Daniel Blandón
 */
class ProgramaAcademicoTest {

    /**
     * Un código válido del formulario se convierte en su programa con el nombre oficial.
     */
    @Test
    void encuentraProgramaPorCodigo() {
        assertEquals("Ingeniería de Sistemas",
                ProgramaAcademico.desdeCodigo("INGENIERIA_SISTEMAS").orElseThrow().getEtiqueta());
    }

    /**
     * Textos escritos a mano, vacíos o nulos no son programas válidos.
     */
    @Test
    void rechazaCodigosQueNoEstanEnLaLista() {
        assertTrue(ProgramaAcademico.desdeCodigo("Ingeniería en Sistemas").isEmpty());
        assertTrue(ProgramaAcademico.desdeCodigo("").isEmpty());
        assertTrue(ProgramaAcademico.desdeCodigo(null).isEmpty());
    }

    /**
     * El nombre guardado en la base de datos se reconoce, y una variación escrita distinto no.
     */
    @Test
    void reconoceSoloElNombreOficial() {
        assertEquals(ProgramaAcademico.FISIOTERAPIA, ProgramaAcademico.desdeEtiqueta("Fisioterapia").orElseThrow());
        assertTrue(ProgramaAcademico.desdeEtiqueta("Ingeniería en Sistemas").isEmpty());
    }

    /**
     * Los programas quedan agrupados en las tres facultades, sin perder ninguno.
     */
    @Test
    void agrupaLosProgramasPorFacultad() {
        Map<String, List<ProgramaAcademico>> grupos = ProgramaAcademico.porFacultad();

        assertEquals(List.of("Ingeniería", "Salud", "Estudios Sociales y Empresariales"), List.copyOf(grupos.keySet()));
        assertEquals(ProgramaAcademico.values().length, grupos.values().stream().mapToInt(List::size).sum());
    }
}
