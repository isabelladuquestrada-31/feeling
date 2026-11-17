package com.feeling.feeling.service;

import com.feeling.feeling.dto.VoluntarioResumen;
import com.feeling.feeling.repository.BusquedaVoluntariosRepository;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Pruebas del caso de éxito y del caso de fallo de RF-07 (buscar voluntarios).
 *
 * @author Daniel Blandón
 */
class BusquedaVoluntariosServiceTest {

    private final BusquedaVoluntariosRepository repositorio = mock(BusquedaVoluntariosRepository.class);
    private final BusquedaVoluntariosService servicio = new BusquedaVoluntariosService(repositorio);

    /**
     * Caso de éxito: encuentra voluntarios por materia y muestra textos legibles y días ordenados.
     */
    @Test
    void encuentraVoluntariosPorMateria() {
        VoluntarioResumen voluntario = new VoluntarioResumen(1, "Voluntario1", "Ingeniería de Sistemas",
                List.of("Cálculo Diferencial"), List.of("TUTORIA"), List.of("VIERNES", "LUNES"));
        when(repositorio.buscar("MAT101", null, null)).thenReturn(List.of(voluntario));

        List<VoluntarioResumen> resultado = servicio.buscar("MAT101", "", "");

        assertEquals(1, resultado.size());
        assertEquals(List.of("Tutoría"), resultado.get(0).getTiposApoyo());
        assertEquals(List.of("Lunes", "Viernes"), resultado.get(0).getDias());
    }

    /**
     * Caso de fallo: si nadie cumple los filtros, devuelve una lista vacía (la vista muestra el mensaje).
     */
    @Test
    void sinResultadosDevuelveListaVacia() {
        when(repositorio.buscar("FIS101", "TUTORIA", null)).thenReturn(List.of());

        assertTrue(servicio.buscar("FIS101", "TUTORIA", null).isEmpty());
    }

    /**
     * Los filtros vacíos o con espacios no se aplican en la consulta.
     */
    @Test
    void filtrosVaciosNoSeAplican() {
        servicio.buscar("  ", null, " ");

        verify(repositorio).buscar(null, null, null);
    }

    /**
     * El filtro de programa llega como código y se consulta con el nombre oficial guardado en la base de datos.
     */
    @Test
    void filtraPorProgramaConSuNombreOficial() {
        servicio.buscar(null, null, "INGENIERIA_SISTEMAS");

        verify(repositorio).buscar(null, null, "Ingeniería de Sistemas");
    }

    /**
     * Un código de programa que no existe no se usa como filtro, en lugar de dejar la búsqueda vacía.
     */
    @Test
    void programaInexistenteNoSeAplica() {
        servicio.buscar(null, null, "INGENIERIA_EN_SISTEMAS");

        verify(repositorio).buscar(null, null, null);
    }
}
