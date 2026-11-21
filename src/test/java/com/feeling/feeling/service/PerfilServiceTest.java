package com.feeling.feeling.service;

import com.feeling.feeling.model.Perfil;
import com.feeling.feeling.repository.IPerfilRepository;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Pruebas de la edición del perfil, casos de exito y de fallo del RF-03
 *
 * @author Daniel Blandon
 */
public class PerfilServiceTest {

    private final IPerfilRepository perfilRepository = mock(IPerfilRepository.class);
    private final PerfilService servicio = new PerfilService(perfilRepository);

    /**
     * Caso de éxito: los datos válidos se guardan sin espacios sobrantes.
     */
    @Test
    void guardarLosCambiosDelPerfil() {
        Perfil perfil = new Perfil();
        when(perfilRepository.findById(2)).thenReturn(Optional.of(perfil));
        when(perfilRepository.save(perfil)).thenReturn(perfil);

        servicio.actualizarPerfil(2, " Dani ", "Él", " 3001234567 ");

        assertEquals("Dani", perfil.getNombrePreferido());
        assertEquals("Él", perfil.getPronombres());
        assertEquals("3001234567", perfil.getWhatsapp());
        verify(perfilRepository).save(perfil);
    }

    /**
     * El nombre preferido y los pronombres son opcionales: si se dejan vacios deben guardarse como null.
     */
    @Test
    void nombreYPronombresVaciosSeGuardanComoNull() {
        Perfil perfil = new Perfil();
        perfil.setNombrePreferido("Dani");
        perfil.setPronombres("Él");
        when(perfilRepository.findById(2)).thenReturn(Optional.of(perfil));
        when(perfilRepository.save(perfil)).thenReturn(perfil);

        servicio.actualizarPerfil(2, "", "", "3001234567");

        assertNull(perfil.getNombrePreferido());
        assertNull(perfil.getPronombres());
    }

    /**
     * Caso de fallo: El WhatsApp vació o con formato incorrecto marca error en ese campo.
     */
    @Test
    void whatsappVacioOInvalidoMarcaErrorEnElCampo() {
        assertEquals(Set.of("whatsapp"), servicio.validar("Dani", "Él", "").keySet());
        assertEquals(Set.of("whatsapp"), servicio.validar("Dani", "Él", "12345").keySet());
        assertEquals(Set.of("whatsapp"), servicio.validar("Dani", "Él", "2001234567").keySet());
    }

    /**
     * Caso de fallo nombre muy largo o unos pronombres que no están en la lista son errores.
     */
    @Test
    void nombreLargoYPronombreFueraDeLaListaSonErrores() {
        var errores = servicio.validar("a".repeat(51), "otro", "3001234567");
        assertEquals(Set.of("nombrePreferido", "pronombres"), errores.keySet());
    }

    /**
     * Si hay errores no se guarda nada en la base de datos.
     */
    @Test
    void noGuardaSiHayErrores() {
        assertThrows(IllegalArgumentException.class,
                () -> servicio.actualizarPerfil(2, "Dani", "Él", ""));

        verify(perfilRepository, never()).save(any());
    }
}
