package com.feeling.feeling.service;
import com.feeling.feeling.model.EstadoSolicitud;
import com.feeling.feeling.model.Perfil;
import com.feeling.feeling.model.Solicitud;
import com.feeling.feeling.repository.IPerfilRepository;
import com.feeling.feeling.repository.IVoluntarioRepository;
import com.feeling.feeling.repository.SolicitudRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Entrega el número de WhatsApp de la otra parte de una solicitud (RNF-01).
 * El número solo se comparte si la solicitud está aceptada y quien lo pide
 * es el estudiante o el voluntario de esa solicitud.
 *
 * <p>Ojo con los ids: la solicitud guarda el id del <b>voluntario</b> (tabla voluntario),
 * pero quien inicia sesión y los perfiles usan el id del <b>usuario</b>. Por eso el
 * voluntario se convierte a su id de usuario antes de comparar y de buscar su perfil.</p>
 *
 * @author Yeymi Tatiana Cuéllar Montes
 * @version 1.1 (Daniel Blandón: id de usuario del voluntario y enlace de WhatsApp)
 */
@Service
public class ContactoSolicitudService {

    /** Indicativo de Colombia para el enlace de WhatsApp. */
    private static final String INDICATIVO = "57";

    /**
     * Repositorio para consultar las solicitudes.
     */
    private final SolicitudRepository solicitudRepository;

    /**
     * Repositorio para consultar los perfiles, donde está el número de WhatsApp.
     */
    private final IPerfilRepository perfilRepository;

    /**
     * Repositorio para saber qué usuario es el voluntario de la solicitud.
     */
    private final IVoluntarioRepository voluntarioRepository;

    /**
     * Crea el servicio con los repositorios que le entrega Spring.
     *
     * @param solicitudRepository  repositorio de solicitudes
     * @param perfilRepository     repositorio de perfiles
     * @param voluntarioRepository repositorio de voluntarios
     */
    public ContactoSolicitudService(SolicitudRepository solicitudRepository,
                                    IPerfilRepository perfilRepository,
                                    IVoluntarioRepository voluntarioRepository) {
        this.solicitudRepository = solicitudRepository;
        this.perfilRepository = perfilRepository;
        this.voluntarioRepository = voluntarioRepository;
    }

    /**
     * RNF-01: devuelve el WhatsApp de la otra parte de una solicitud aceptada.
     * Si pregunta el estudiante recibe el número del voluntario, y al revés.
     *
     * @param solicitudId id de la solicitud
     * @param usuarioId   id del usuario que pide el número (el de la sesión)
     * @return número de WhatsApp de la otra parte
     */
    @Transactional(readOnly = true)
    public String whatsappDeLaOtraParte(Integer solicitudId, Integer usuarioId) {
        Solicitud solicitud = solicitudRepository.findById(solicitudId)
                .orElseThrow(() -> new IllegalArgumentException("La solicitud no existe."));

        // La solicitud guarda el id del voluntario; se busca el id de usuario de ese voluntario.
        Integer voluntarioUsuarioId = voluntarioRepository.findById(solicitud.getVoluntarioId())
                .map(voluntario -> voluntario.getEstudiante().getId())
                .orElseThrow(() -> new IllegalStateException("El voluntario de la solicitud no existe."));

        boolean esEstudiante = solicitud.getEstudianteId().equals(usuarioId);
        boolean esVoluntario = voluntarioUsuarioId.equals(usuarioId);
        if (!esEstudiante && !esVoluntario) {
            throw new IllegalArgumentException(
                    "Solo el estudiante o el voluntario de la solicitud pueden ver el contacto.");
        }

        if (solicitud.getEstado() != EstadoSolicitud.ACEPTADA) {
            throw new IllegalStateException(
                    "El número de WhatsApp solo se comparte cuando la solicitud está aceptada.");
        }

        Integer otraParteId = esEstudiante ? voluntarioUsuarioId : solicitud.getEstudianteId();
        Perfil perfil = perfilRepository.findById(otraParteId)
                .orElseThrow(() -> new IllegalStateException("La otra parte no tiene un perfil registrado."));

        String whatsapp = perfil.getWhatsapp();
        if (whatsapp == null || whatsapp.trim().isEmpty()) {
            throw new IllegalStateException("La otra parte no registró su número de WhatsApp.");
        }
        return whatsapp;
    }

    /**
     * RF-11: arma el enlace https://wa.me/57NUMERO para abrir el chat con la otra parte.
     * Aplica las mismas reglas que {@link #whatsappDeLaOtraParte(Integer, Integer)}.
     *
     * @param solicitudId id de la solicitud
     * @param usuarioId   id del usuario que pide el contacto
     * @return enlace de WhatsApp
     */
    @Transactional(readOnly = true)
    public String enlaceWhatsapp(Integer solicitudId, Integer usuarioId) {
        String digitos = whatsappDeLaOtraParte(solicitudId, usuarioId).replaceAll("\\D", "");
        if (!(digitos.length() == 12 && digitos.startsWith(INDICATIVO))) {
            digitos = INDICATIVO + digitos;
        }
        return "https://wa.me/" + digitos;
    }
}
