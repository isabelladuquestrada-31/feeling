package com.feeling.feeling.service;

import com.feeling.feeling.model.EstadoSolicitud;
import com.feeling.feeling.model.Estudiante;
import com.feeling.feeling.model.Perfil;
import com.feeling.feeling.model.Solicitud;
import com.feeling.feeling.model.Voluntario;
import com.feeling.feeling.repository.IPerfilRepository;
import com.feeling.feeling.repository.IVoluntarioRepository;
import com.feeling.feeling.repository.SolicitudRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.util.List;
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
 * Usan los ids de data.sql: la solicitud guarda el voluntario 1, que es el usuario 5
 * (voluntario1); el estudiante es el usuario 2 (estudiante1). Así se comprueba que no
 * se confunden el id del voluntario con el id del usuario.
 *
 * @author Yeymi Tatiana Cuéllar Montes
 * @version 1.1 (Daniel Blandón: ids reales del voluntario y pruebas del enlace)
 */
class ContactoSolicitudServiceTest {

    private final SolicitudRepository solicitudRepositorio = mock(SolicitudRepository.class);
    private final IPerfilRepository perfilRepositorio = mock(IPerfilRepository.class);
    private final IVoluntarioRepository voluntarioRepositorio = mock(IVoluntarioRepository.class);
    private final ContactoSolicitudService servicio =
            new ContactoSolicitudService(solicitudRepositorio, perfilRepositorio, voluntarioRepositorio);

    /**
     * Deja lista la solicitud 1 (estudiante 2, voluntario 1 = usuario 5) con el estado indicado,
     * los perfiles con su WhatsApp y, como trampa, un perfil del usuario 1 (el admin).
     */
    private void solicitudCon(EstadoSolicitud estado) {
        Solicitud solicitud = new Solicitud();
        solicitud.setEstudianteId(2);
        solicitud.setVoluntarioId(1);
        solicitud.setEstado(estado);
        when(solicitudRepositorio.findById(1)).thenReturn(Optional.of(solicitud));

        Estudiante estudianteVoluntario = new Estudiante();
        estudianteVoluntario.setId(5);
        when(voluntarioRepositorio.findById(1)).thenReturn(Optional.of(
                new Voluntario(estudianteVoluntario, List.of(), List.of())));

        Perfil perfilAdmin = mock(Perfil.class);
        when(perfilAdmin.getWhatsapp()).thenReturn("0001000001");
        when(perfilRepositorio.findById(1)).thenReturn(Optional.of(perfilAdmin));

        Perfil perfilEstudiante = mock(Perfil.class);
        when(perfilEstudiante.getWhatsapp()).thenReturn("3001111111");
        when(perfilRepositorio.findById(2)).thenReturn(Optional.of(perfilEstudiante));

        Perfil perfilVoluntario = mock(Perfil.class);
        when(perfilVoluntario.getWhatsapp()).thenReturn("3002222222");
        when(perfilRepositorio.findById(5)).thenReturn(Optional.of(perfilVoluntario));
    }

    /**
     * Caso de éxito: el estudiante recibe el número del voluntario (no el del usuario 1).
     */
    @Test
    void estudianteRecibeWhatsappDelVoluntario() {
        solicitudCon(EstadoSolicitud.ACEPTADA);
        assertEquals("3002222222", servicio.whatsappDeLaOtraParte(1, 2));
    }

    /**
     * Caso de éxito: el voluntario (usuario 5) recibe el número del estudiante.
     */
    @Test
    void voluntarioRecibeWhatsappDelEstudiante() {
        solicitudCon(EstadoSolicitud.ACEPTADA);
        assertEquals("3001111111", servicio.whatsappDeLaOtraParte(1, 5));
    }

    /**
     * Caso de éxito: el enlace abre el chat con el indicativo de Colombia.
     */
    @Test
    void armaElEnlaceDeWhatsapp() {
        solicitudCon(EstadoSolicitud.ACEPTADA);
        assertEquals("https://wa.me/573002222222", servicio.enlaceWhatsapp(1, 2));
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
     * Caso de fallo: si la solicitud fue rechazada o cancelada tampoco se entrega.
     */
    @Test
    void noEntregaWhatsappSiLaSolicitudFueRechazadaOCancelada() {
        solicitudCon(EstadoSolicitud.RECHAZADA);
        assertThrows(IllegalStateException.class, () -> servicio.whatsappDeLaOtraParte(1, 5));

        solicitudCon(EstadoSolicitud.CANCELADA);
        assertThrows(IllegalStateException.class, () -> servicio.enlaceWhatsapp(1, 2));
        verify(perfilRepositorio, never()).findById(anyInt());
    }

    /**
     * Caso de fallo: alguien que no es parte de la solicitud no recibe el número,
     * ni siquiera si su id de usuario es igual al id del voluntario (el usuario 1).
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
        assertThrows(IllegalArgumentException.class, () -> servicio.whatsappDeLaOtraParte(1, 1));
        verify(perfilRepositorio, never()).findById(anyInt());
    }
}
