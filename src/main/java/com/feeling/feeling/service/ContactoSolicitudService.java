package com.feeling.feeling.service;
import com.feeling.feeling.model.EstadoSolicitud;
import com.feeling.feeling.model.Perfil;
import com.feeling.feeling.model.Solicitud;
import com.feeling.feeling.repository.IPerfilRepository;
import com.feeling.feeling.repository.SolicitudRepository;
import org.springframework.stereotype.Service;

/**
 * Entrega el número de WhatsApp de la otra parte de una solicitud (RNF-01).
 * El número solo se comparte si la solicitud está aceptada y quien lo pide
 * es el estudiante o el voluntario de esa solicitud.
 *
 * @author Yeymi Tatiana Cuéllar Montes
 */
@Service
public class ContactoSolicitudService {

    /**
     * Repositorio para consultar las solicitudes.
     */
    private final SolicitudRepository solicitudRepository;

    /**
     * Repositorio para consultar los perfiles, donde está el número de WhatsApp.
     */
    private final IPerfilRepository perfilRepository;

    /**
     * Crea el servicio con los repositorios que le entrega Spring.
     *
     * @param solicitudRepository repositorio de solicitudes
     * @param perfilRepository    repositorio de perfiles
     */
    public ContactoSolicitudService(SolicitudRepository solicitudRepository,
                                    IPerfilRepository perfilRepository) {
        this.solicitudRepository = solicitudRepository;
        this.perfilRepository = perfilRepository;
    }

    /**
     * RNF-01: devuelve el WhatsApp de la otra parte de una solicitud aceptada.
     * Si pregunta el estudiante recibe el número del voluntario, y al revés.
     *
     * @param solicitudId id de la solicitud
     * @param usuarioId   id de quien pide el número
     * @return número de WhatsApp de la otra parte
     */
    public String whatsappDeLaOtraParte(Integer solicitudId, Integer usuarioId) {
        Solicitud solicitud = solicitudRepository.findById(solicitudId)
                .orElseThrow(() -> new IllegalArgumentException("La solicitud no existe."));

        boolean esEstudiante = solicitud.getEstudianteId().equals(usuarioId);
        boolean esVoluntario = solicitud.getVoluntarioId().equals(usuarioId);
        if (!esEstudiante && !esVoluntario) {
            throw new IllegalArgumentException(
                    "Solo el estudiante o el voluntario de la solicitud pueden ver el contacto.");
        }

        if (solicitud.getEstado() != EstadoSolicitud.ACEPTADA) {
            throw new IllegalStateException(
                    "El número de WhatsApp solo se comparte cuando la solicitud está aceptada.");
        }

        Integer otraParteId = esEstudiante ? solicitud.getVoluntarioId() : solicitud.getEstudianteId();
        Perfil perfil = perfilRepository.findById(otraParteId)
                .orElseThrow(() -> new IllegalStateException("La otra parte no tiene un perfil registrado."));

        String whatsapp = perfil.getWhatsapp();
        if (whatsapp == null || whatsapp.trim().isEmpty()) {
            throw new IllegalStateException("La otra parte no registró su número de WhatsApp.");
        }
        return whatsapp;
    }
}
