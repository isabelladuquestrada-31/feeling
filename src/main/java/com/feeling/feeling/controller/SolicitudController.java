package com.feeling.feeling.controller;

import com.feeling.feeling.service.SolicitudService;
import org.springframework.stereotype.Controller;
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
}