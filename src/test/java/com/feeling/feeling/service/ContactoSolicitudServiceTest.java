package com.feeling.feeling.service;

import com.feeling.feeling.model.EstadoSolicitud;
import com.feeling.feeling.model.Perfil;
import com.feeling.feeling.model.Solicitud;
import com.feeling.feeling.repository.IPerfilRepository;
import com.feeling.feeling.repository.SolicitudRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Pruebas del caso de éxito y de los casos de fallo de RNF-01 (entrega del WhatsApp).
 */
class ContactoSolicitudServiceTest {

    private final SolicitudRepository solicitudRepositorio = mock(SolicitudRepository.class);
    private final IPerfilRepository perfilRepositorio = mock(IPerfilRepository.class);
    private final ContactoSolicitudService servicio =
            new ContactoSolicitudService(solicitudRepositorio, perfilRepositorio);

    /**
     * Deja lista la solicitud 1 (estudiante 2, voluntario 5) con el estado indicado,
     * y los dos perfiles con su número de WhatsApp.
     */
    private void solicitudCon(EstadoSolicitud estado) {
        Solicitud solicitud = new Solicitud();
        solicitud.setEstudianteId(2);
        solicitud.setVoluntarioId(5);
        solicitud.setEstado(estado);
        when(solicitudRepositorio.findById(1)).thenReturn(Optional.of(solicitud));

        Perfil perfilEstudiante = mock(Perfil.class);
        when(perfilEstudiante.getWhatsapp()).thenReturn("3001111111");
        when(perfilRepositorio.findById(2)).thenReturn(Optional.of(perfilEstudiante));

        Perfil perfilVoluntario = mock(Perfil.class);
        when(perfilVoluntario.getWhatsapp()).thenReturn("3002222222");
        when(perfilRepositorio.findById(5)).thenReturn(Optional.of(perfilVoluntario));
    }

    /**
     * Caso de éxito: el estudiante recibe el número del voluntario.
     */
    @Test
    void estudianteRecibeWhatsappDelVoluntario() {
        solicitudCon(EstadoSolicitud.ACEPTADA);
        assertEquals("3002222222", servicio.whatsappDeLaOtraParte(1, 2));
    }

    /**
     * Caso de éxito: el voluntario recibe el número del estudiante.
     */
    @Test
    void voluntarioRecibeWhatsappDelEstudiante() {
        solicitudCon(EstadoSolicitud.ACEPTADA);
        assertEquals("3001111111", servicio.whatsappDeLaOtraParte(1, 5));
    }

    /**
     * Caso de fallo: si la solicitud sigue pendiente no se entrega el número.
     */
    @Test
    void noEntregaWhatsappSiLaSolicitudEstaPendiente() {
        solicitudCon(EstadoSolicitud.PENDIENTE);

        IllegalStateException error = assertThrows(IllegalStateException.class, new Executable() {
            @Override
            public void execute() {
                servicio.whatsappDeLaOtraParte(1, 2);
            }
        });

        assertEquals("El número de WhatsApp solo se comparte cuando la solicitud está aceptada.",
                error.getMessage());
        verify(perfilRepositorio, never()).findById(anyInt());
    }

    /**
     * Caso de fallo: alguien que no es parte de la solicitud no recibe el número.
     */
    @Test
    void noEntregaWhatsappAUnTercero() {
        solicitudCon(EstadoSolicitud.ACEPTADA);

        assertThrows(IllegalArgumentException.class, new Executable() {
            @Override
            public void execute() {
                servicio.whatsappDeLaOtraParte(1, 99);
            }
        });
        verify(perfilRepositorio, never()).findById(anyInt());
    }
}
