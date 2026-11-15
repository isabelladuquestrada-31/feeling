package com.feeling.feeling.controller;

import com.feeling.feeling.service.SolicitudService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Atiende las acciones del estudiante sobre sus solicitudes de acompañamiento.
 *
 * @author Daniel Blandón
 */
@Controller
public class SolicitudController {

    /**
     * TEMPORAL: id de estudiante1 del data.sql. Se reemplaza por el usuario
     * de la sesión cuando esté listo el login (SCRUM-51).
     */
    private static final Integer ESTUDIANTE_PRUEBA_ID = 2;

    /**
     * TEMPORAL: id de voluntario1 del data.sql. Se reemplaza por el voluntario
     * de la sesión cuando exista la entidad Voluntario.
     */
    private static final Integer VOLUNTARIO_PRUEBA_ID = 1;

    /**
     * Servicio con la lógica de las solicitudes.
     */
    private final SolicitudService solicitudService;

    /**
     * Crea el controlador con el servicio que le entrega Spring.
     *
     * @param solicitudService servicio de solicitudes
     */
    public SolicitudController(SolicitudService solicitudService) {
        this.solicitudService = solicitudService;
    }

    /**
     * RF-12: recibe el clic en el botón "Cancelar" y muestra un mensaje de éxito o el motivo del error.
     *
     * @param id                 id de la solicitud que se quiere cancelar
     * @param redirectAttributes mensajes que se muestran en la siguiente página
     * @return redirección a la página de acompañamientos del estudiante
     */
    @PostMapping("/solicitudes/{id}/cancelar")
    public String cancelar(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            solicitudService.cancelar(id, ESTUDIANTE_PRUEBA_ID);
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
     * @param model datos que se envían a la vista
     * @return la visa de mis acompañamientos
     */
    @GetMapping("/mis-acompanamientos")
    public String misAcompanamientos(Model model) {
        model.addAttribute("proximos", solicitudService.proximosDelEstudiante(ESTUDIANTE_PRUEBA_ID));
        model.addAttribute("historial", solicitudService.historialDelEstudiante(ESTUDIANTE_PRUEBA_ID));
        return "paginas/mis-acompanamientos";

    }

    /**
     * RF-10: muestra al voluntario las solicitudes pendientes que ha recibido,
     * con los botones para aceptarlas o rechazarlas.
     *
     * @param model datos que se envían a la vista
     * @return la vista de solicitudes recibidas
     */
    @GetMapping("/solicitudes-recibidas")
    public String solicitudesRecibidas(Model model) {
        model.addAttribute("solicitudesRecibidas", solicitudService.solicitudesRecibidas(VOLUNTARIO_PRUEBA_ID));
        model.addAttribute("paginaActiva", "acompanamientos");
        return "paginas/solicitudes-recibidas";
    }
}
