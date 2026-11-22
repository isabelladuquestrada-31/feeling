package com.feeling.feeling.service;

import com.feeling.feeling.model.Estudiante;
import com.feeling.feeling.model.PreferenciasAccesibilidad;
import com.feeling.feeling.repository.IPreferenciaAccesibilidadRepository;
import com.feeling.feeling.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Pruebas de las preferencias de accesibilidad (RF-04).
 *
 * @author Daniel Blandón
 */
class PreferenciasServiceTest {

    private final IPreferenciaAccesibilidadRepository preferenciasRepository =
            mock(IPreferenciaAccesibilidadRepository.class);
    private final UsuarioRepository usuarioRepository = mock(UsuarioRepository.class);
    private final PreferenciasService servicio =
            new PreferenciasService(preferenciasRepository, usuarioRepository);

    /**
     * Un usuario que nunca ha guardado preferencias las tiene todas apagadas.
     */
    @Test
    void sinPreferenciasGuardadasTodoEstaApagado() {
        when(preferenciasRepository.findById(2)).thenReturn(Optional.empty());

        PreferenciasAccesibilidad preferencias = servicio.obtener(2);

        assertFalse(preferencias.isLetraGrande());
        assertFalse(preferencias.isAltoContraste());
    }

    /**
     * Caso de éxito: se actualizan las preferencias que ya existían.
     */
    @Test
    void guardaLasPreferenciasDelUsuario() {
        PreferenciasAccesibilidad existentes = new PreferenciasAccesibilidad();
        when(preferenciasRepository.findById(2)).thenReturn(Optional.of(existentes));
        when(preferenciasRepository.save(existentes)).thenReturn(existentes);

        PreferenciasAccesibilidad guardadas = servicio.guardar(2, true, false);

        assertSame(existentes, guardadas);
        assertTrue(guardadas.isLetraGrande());
        assertFalse(guardadas.isAltoContraste());
        verify(preferenciasRepository).save(existentes);
    }

    /**
     * Si el usuario no tenía fila de preferencias, se crea una nueva.
     */
    @Test
    void creaLasPreferenciasSiNoExistian() {
        Estudiante estudiante = new Estudiante();
        when(preferenciasRepository.findById(2)).thenReturn(Optional.empty());
        when(usuarioRepository.findById(2)).thenReturn(Optional.of(estudiante));
        when(preferenciasRepository.save(any())).thenAnswer(invocacion -> invocacion.getArgument(0));

        PreferenciasAccesibilidad guardadas = servicio.guardar(2, false, true);

        assertSame(estudiante, guardadas.getUsuario());
        assertTrue(guardadas.isAltoContraste());
    }

    /**
     * Caso de fallo: si el usuario no existe no se guarda nada.
     */
    @Test
    void usuarioInexistenteNoGuardaNada() {
        when(preferenciasRepository.findById(99)).thenReturn(Optional.empty());
        when(usuarioRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> servicio.guardar(99, true, true));
        verify(preferenciasRepository, never()).save(any());
    }

    /**
     * Las preferencias se convierten en las clases CSS de la página.
     */
    @Test
    void conviertePreferenciasEnClasesCss() {
        PreferenciasAccesibilidad preferencias = new PreferenciasAccesibilidad();
        assertEquals("", PreferenciasService.clasesCss(preferencias));
        assertEquals("", PreferenciasService.clasesCss(null));

        preferencias.setLetraGrande(true);
        assertEquals("letra-grande", PreferenciasService.clasesCss(preferencias));

        preferencias.setAltoContraste(true);
        assertEquals("letra-grande alto-contraste", PreferenciasService.clasesCss(preferencias));
    }
}