package com.feeling.feeling.controller;

import com.feeling.feeling.model.Administrador;
import com.feeling.feeling.model.AjusteRazonable;
import com.feeling.feeling.model.Estudiante;
import com.feeling.feeling.model.Solicitud;
import com.feeling.feeling.model.Voluntario;
import com.feeling.feeling.service.AjusteRazonableService;
import com.feeling.feeling.service.ContactoSolicitudService;
import com.feeling.feeling.service.PanelService;
import com.feeling.feeling.service.SeguimientoService;
import com.feeling.feeling.service.SolicitudService;
import com.feeling.feeling.service.VoluntarioService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * Atiende las acciones sobre las solicitudes de acompañamiento:
 * el estudiante consulta y cancela las suyas (RF-12, RF-13) y el voluntario
 * acepta o rechaza las que recibe (RF-10).
 * El voluntario también ve los ajustes razonables que pidió el estudiante (RF-09, SCRUM-95)
 * y marca como realizados los acompañamientos que ya pasaron, con notas y duración
 * (RF-14, SCRUM-97).
 * Usa el usuario que inició sesión.
 *
 * @author Daniel Blandón
 */
@Controller
public class SolicitudController {

    /** Idioma de las fechas ("jue, 23 oct"), sin depender del navegador. */
    private static final Locale ESPANOL = Locale.forLanguageTag("es-CO");

    private static final String REDIRECCION_RECIBIDAS = "redirect:/solicitudes-recibidas";

    /**
     * Duraciones que el voluntario puede elegir al cerrar un acompañamiento (RF-14),
     * en minutos y con el texto que se muestra.
     */
    private static final Map<Integer, String> DURACIONES = duraciones();

    /** Largo máximo de las notas del seguimiento. */
    private static final int LARGO_MAXIMO_NOTAS = 1000;

    /**
     * Servicio con la lógica de las solicitudes.
     */
    private final SolicitudService solicitudService;

    /**
     * Servicio para saber si el estudiante es voluntario.
     */
    private final VoluntarioService voluntarioService;

    /**
     * Servicio con los nombres de estudiantes y materias.
     */
    private final PanelService panelService;

    /**
     * Servicio que entrega el WhatsApp de la otra parte (RNF-01, SCRUM-84).
     */
    private final ContactoSolicitudService contactoSolicitudService;

    /**
     * Servicio con los ajustes razonables de cada solicitud (RF-09, SCRUM-94).
     */
    private final AjusteRazonableService ajusteRazonableService;

    /**
     * Servicio que registra el seguimiento y marca el acompañamiento como realizado (RF-14, SCRUM-96).
     */
    private final SeguimientoService seguimientoService;

    /**
     * Crea el controlador con los servicios que le entrega Spring.
     *
     * @param solicitudService         servicio de solicitudes
     * @param voluntarioService        servicio de voluntarios
     * @param panelService             nombres de estudiantes y materias
     * @param contactoSolicitudService contacto de WhatsApp de la otra parte
     * @param ajusteRazonableService   ajustes razonables de cada solicitud
     * @param seguimientoService       registro del seguimiento de los acompañamientos
     */
    public SolicitudController(SolicitudService solicitudService,
                               VoluntarioService voluntarioService,
                               PanelService panelService,
                               ContactoSolicitudService contactoSolicitudService,
                               AjusteRazonableService ajusteRazonableService,
                               SeguimientoService seguimientoService) {
        this.solicitudService = solicitudService;
        this.voluntarioService = voluntarioService;
        this.panelService = panelService;
        this.contactoSolicitudService = contactoSolicitudService;
        this.ajusteRazonableService = ajusteRazonableService;
        this.seguimientoService = seguimientoService;
    }

    /**
     * Devuelve el estudiante que inició sesión, si lo hay.
     */
    private Optional<Estudiante> estudianteDeLaSesion(HttpSession sesion) {
        Object usuario = sesion.getAttribute("usuario");
        return usuario instanceof Estudiante estudiante ? Optional.of(estudiante) : Optional.empty();
    }

    /**
     * Pone en el modelo los datos del menú lateral.
     */
    private void datosDelMenu(Model model, Estudiante estudiante, String paginaActiva) {
        model.addAttribute("paginaActiva", paginaActiva);
        model.addAttribute("usuarioNombre", estudiante.getNombreUsuario());
        model.addAttribute("usuarioRol", "Estudiante");
    }

    /**
     * Redirección para quien no es estudiante: el administrador a su panel
     * y quien no ha iniciado sesión al login.
     */
    private String redireccionSinEstudiante(HttpSession sesion) {
        return sesion.getAttribute("usuario") instanceof Administrador ? "redirect:/admin" : "redirect:/login";
    }

    /**
     * RF-12: recibe el clic en el botón "Cancelar" y muestra un mensaje de éxito o el motivo del error.
     *
     * @param id                 id de la solicitud que se quiere cancelar
     * @param sesion             sesión con el estudiante que cancela
     * @param redirectAttributes mensajes que se muestran en la siguiente página
     * @return redirección a la página de acompañamientos del estudiante
     */
    @PostMapping("/solicitudes/{id}/cancelar")
    public String cancelar(@PathVariable Integer id, HttpSession sesion, RedirectAttributes redirectAttributes) {
        Optional<Estudiante> estudiante = estudianteDeLaSesion(sesion);
        if (estudiante.isEmpty()) {
            return redireccionSinEstudiante(sesion);
        }
        try {
            solicitudService.cancelar(id, estudiante.get().getId());
            redirectAttributes.addFlashAttribute("mensajeExito", "La solicitud se canceló correctamente.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            redirectAttributes.addFlashAttribute("mensajeError", e.getMessage());
        }
        return "redirect:/mis-acompanamientos";
    }

    /**
     * RF-13: muestra al estudiante sus próximos acompañamientos aceptados
     * y su historial de solicitudes.
     *
     * @param sesion sesión con el estudiante
     * @param model  datos que se envían a la vista
     * @return la vista de mis acompañamientos
     */
    @GetMapping("/mis-acompanamientos")
    public String misAcompanamientos(HttpSession sesion, Model model) {
        Optional<Estudiante> estudiante = estudianteDeLaSesion(sesion);
        if (estudiante.isEmpty()) {
            return redireccionSinEstudiante(sesion);
        }
        Integer id = estudiante.get().getId();
        model.addAttribute("proximos", solicitudService.proximosDelEstudiante(id));
        model.addAttribute("historial", solicitudService.historialDelEstudiante(id));
        datosDelMenu(model, estudiante.get(), "acompanamientos");
        return "paginas/mis-acompanamientos";
    }

    /**
     * RF-10: muestra al voluntario las solicitudes pendientes que ha recibido,
     * con los botones para aceptarlas o rechazarlas. Si el estudiante todavía
     * no es voluntario, la vista lo invita a activarse.
     *
     * @param sesion sesión con el estudiante voluntario
     * @param model  datos que se envían a la vista
     * @return la vista de solicitudes recibidas
     */
    @GetMapping("/solicitudes-recibidas")
    public String solicitudesRecibidas(HttpSession sesion, Model model) {
        Optional<Estudiante> estudiante = estudianteDeLaSesion(sesion);
        if (estudiante.isEmpty()) {
            return redireccionSinEstudiante(sesion);
        }

        Optional<Voluntario> voluntario = voluntarioService.buscarPorEstudiante(estudiante.get().getId());
        List<Solicitud> recibidas = voluntario
                .map(v -> solicitudService.solicitudesRecibidas(v.getId()))
                .orElse(List.of());
        List<Solicitud> aceptadas = voluntario
                .map(v -> solicitudService.proximosAceptadosDelVoluntario(v.getId()))
                .orElse(List.of());
        List<Solicitud> porCerrar = voluntario
                .map(v -> solicitudService.pendientesDeSeguimiento(v.getId()))
                .orElse(List.of());
        List<Integer> estudiantes = new ArrayList<>(recibidas.stream().map(Solicitud::getEstudianteId).toList());
        aceptadas.forEach(s -> estudiantes.add(s.getEstudianteId()));
        porCerrar.forEach(s -> estudiantes.add(s.getEstudianteId()));

        model.addAttribute("esVoluntario", voluntario.isPresent());
        model.addAttribute("solicitudesRecibidas", recibidas);
        model.addAttribute("aceptadas", aceptadas);
        model.addAttribute("porCerrar", porCerrar);
        model.addAttribute("duraciones", DURACIONES);
        model.addAttribute("largoMaximoNotas", LARGO_MAXIMO_NOTAS);
        model.addAttribute("ajustesPorSolicitud", ajustesDe(recibidas, aceptadas, porCerrar));
        model.addAttribute("nombresEstudiantes", panelService.nombresDeEstudiantes(estudiantes));
        model.addAttribute("nombresMaterias", panelService.nombresDeMaterias());
        model.addAttribute("etiquetasApoyo", SerVoluntarioController.etiquetasDeApoyo());
        model.addAttribute("idioma", ESPANOL);
        datosDelMenu(model, estudiante.get(), "ser-voluntario");
        return "paginas/solicitudes-recibidas";
    }

    /**
     * RF-10: el voluntario acepta una solicitud pendiente.
     *
     * @param id                 id de la solicitud
     * @param sesion             sesión con el estudiante voluntario
     * @param redirectAttributes mensajes que se muestran en la siguiente página
     * @return redirección a las solicitudes recibidas
     */
    @PostMapping("/solicitudes/{id}/aceptar")
    public String aceptar(@PathVariable Integer id, HttpSession sesion, RedirectAttributes redirectAttributes) {
        return responder(id, true, sesion, redirectAttributes);
    }

    /**
     * RF-10: el voluntario rechaza una solicitud pendiente.
     *
     * @param id                 id de la solicitud
     * @param sesion             sesión con el estudiante voluntario
     * @param redirectAttributes mensajes que se muestran en la siguiente página
     * @return redirección a las solicitudes recibidas
     */
    @PostMapping("/solicitudes/{id}/rechazar")
    public String rechazar(@PathVariable Integer id, HttpSession sesion, RedirectAttributes redirectAttributes) {
        return responder(id, false, sesion, redirectAttributes);
    }

    /**
     * Acepta o rechaza la solicitud con el voluntario de la sesión y deja el mensaje
     * de éxito, o el motivo por el que no se pudo (por ejemplo, si el estudiante la canceló).
     */
    private String responder(Integer id, boolean aceptar, HttpSession sesion, RedirectAttributes redirectAttributes) {
        Optional<Estudiante> estudiante = estudianteDeLaSesion(sesion);
        if (estudiante.isEmpty()) {
            return redireccionSinEstudiante(sesion);
        }
        Optional<Voluntario> voluntario = voluntarioService.buscarPorEstudiante(estudiante.get().getId());
        if (voluntario.isEmpty()) {
            redirectAttributes.addFlashAttribute("mensajeError", "Solo los voluntarios pueden responder solicitudes.");
            return REDIRECCION_RECIBIDAS;
        }

        try {
            Solicitud solicitud = aceptar
                    ? solicitudService.aceptar(id, voluntario.get().getId())
                    : solicitudService.rechazar(id, voluntario.get().getId());
            String nombre = panelService.nombresDeEstudiantes(List.of(solicitud.getEstudianteId()))
                    .getOrDefault(solicitud.getEstudianteId(), "el estudiante");
            redirectAttributes.addFlashAttribute("mensajeExito", aceptar
                    ? "Aceptaste la solicitud de " + nombre + ". Ya aparece como aceptada en sus solicitudes."
                    : "Rechazaste la solicitud de " + nombre + ". Ya aparece como rechazada en sus solicitudes.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            redirectAttributes.addFlashAttribute("mensajeError", e.getMessage());
        }
        return REDIRECCION_RECIBIDAS;
    }

    /**
     * RF-14 (SCRUM-97): el voluntario registra las notas y la duración y marca el
     * acompañamiento como realizado. El servicio de Yeymi (SCRUM-96) no lo permite si
     * la solicitud no está aceptada o si su fecha todavía no llega.
     *
     * @param id                 id de la solicitud
     * @param duracionMinutos    duración elegida, en minutos
     * @param notas              notas del acompañamiento (opcionales)
     * @param sesion             sesión con el voluntario
     * @param redirectAttributes mensaje de éxito o del motivo del error
     * @return la redirección a Solicitudes recibidas
     */
    @PostMapping("/solicitudes/{id}/realizada")
    public String marcarRealizada(@PathVariable Integer id,
                                  @RequestParam(required = false) Integer duracionMinutos,
                                  @RequestParam(required = false) String notas,
                                  HttpSession sesion, RedirectAttributes redirectAttributes) {
        Optional<Estudiante> estudiante = estudianteDeLaSesion(sesion);
        if (estudiante.isEmpty()) {
            return redireccionSinEstudiante(sesion);
        }
        Optional<Voluntario> voluntario = voluntarioService.buscarPorEstudiante(estudiante.get().getId());
        if (voluntario.isEmpty()) {
            redirectAttributes.addFlashAttribute("mensajeError",
                    "Solo los voluntarios pueden registrar el seguimiento.");
            return REDIRECCION_RECIBIDAS;
        }
        if (notas != null && notas.length() > LARGO_MAXIMO_NOTAS) {
            redirectAttributes.addFlashAttribute("mensajeError",
                    "Las notas pueden tener máximo " + LARGO_MAXIMO_NOTAS + " caracteres.");
            return REDIRECCION_RECIBIDAS;
        }

        try {
            seguimientoService.marcarComoRealizada(id, voluntario.get().getId(), notas, duracionMinutos);
            String duracion = DURACIONES.getOrDefault(duracionMinutos, duracionMinutos + " min");
            redirectAttributes.addFlashAttribute("mensajeExito", "Registraste el acompañamiento como realizado ("
                    + duracion + "). Ya suma a tus horas de voluntariado.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            redirectAttributes.addFlashAttribute("mensajeError", e.getMessage());
        }
        return REDIRECCION_RECIBIDAS;
    }

    /**
     * RF-11: abre el chat de WhatsApp con la otra parte de una solicitud aceptada.
     * El número no se escribe en la página: solo se entrega aquí, después de revisar
     * que la solicitud esté aceptada y que la pida una de las dos partes (RNF-01).
     *
     * @param id                 id de la solicitud
     * @param volver             página a la que se vuelve si no se puede: recibidas, inicio o acompanamientos
     * @param sesion             sesión con el estudiante o el voluntario
     * @param redirectAttributes motivo si no se puede ver el contacto
     * @return redirección a https://wa.me/57NUMERO, o a la página de origen con el motivo
     */
    @GetMapping("/solicitudes/{id}/whatsapp")
    public String whatsapp(@PathVariable Integer id,
                           @RequestParam(name = "volver", required = false) String volver,
                           HttpSession sesion, RedirectAttributes redirectAttributes) {
        Optional<Estudiante> estudiante = estudianteDeLaSesion(sesion);
        if (estudiante.isEmpty()) {
            return redireccionSinEstudiante(sesion);
        }
        try {
            return "redirect:" + contactoSolicitudService.enlaceWhatsapp(id, estudiante.get().getId());
        } catch (IllegalArgumentException | IllegalStateException e) {
            redirectAttributes.addFlashAttribute("mensajeError", e.getMessage());
            if ("recibidas".equals(volver)) {
                return REDIRECCION_RECIBIDAS;
            }
            return "inicio".equals(volver) ? "redirect:/inicio" : "redirect:/mis-acompanamientos";
        }
    }

    /**
     * Junta los ajustes razonables de cada solicitud (RF-09), para que el voluntario
     * los vea al revisarla. Toda solicitud queda en el mapa, aunque no tenga ajustes.
     *
     * @param listas listas de solicitudes que se muestran en la página
     * @return ajustes por id de solicitud
     */
    @SafeVarargs
    private Map<Integer, List<AjusteRazonable>> ajustesDe(List<Solicitud>... listas) {
        Map<Integer, List<AjusteRazonable>> ajustes = new HashMap<>();
        for (List<Solicitud> lista : listas) {
            for (Solicitud solicitud : lista) {
                ajustes.computeIfAbsent(solicitud.getId(), ajusteRazonableService::ajustesDeLaSolicitud);
            }
        }
        return ajustes;
    }

    /**
     * Opciones de duración, en el orden en que se muestran.
     */
    private static Map<Integer, String> duraciones() {
        Map<Integer, String> opciones = new LinkedHashMap<>();
        opciones.put(30, "30 min");
        opciones.put(60, "1 hora");
        opciones.put(90, "1 h 30 min");
        opciones.put(120, "2 horas");
        opciones.put(180, "3 horas");
        return opciones;
    }
}
