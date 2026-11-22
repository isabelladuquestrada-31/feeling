package com.feeling.feeling.controller;

import com.feeling.feeling.model.PreferenciasAccesibilidad;
import com.feeling.feeling.model.Usuario;
import com.feeling.feeling.service.PreferenciasService;
import jakarta.servlet.http.HttpSession;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Guarda las preferencias de accesibilidad desde "Mi perfil RF-04
 *
 * @author Daniel Blandón
 * @version 1.0
 */
@Controller
public class AccesibilidadController {

    /**
     * Servicio que guarda las preferencias de la accesibilidad
     */
    private final PreferenciasService preferenciasService;

    /**
     * Crea el controlador con el servicio de preferencias.
     *
     * @param preferenciasService
     */
    public AccesibilidadController(PreferenciasService preferenciasService) {
        this.preferenciasService = preferenciasService;
    }

    /**
     * Guarda las preferencias y las actualiza en la sesión para que se apliquen
     * en todas las páginas. Si no se pueden guardar avisa y deja las anteriores.
     *
     * @param letraGrande        true si el interruptor de letra grade esta encendido
     * @param altoContraste      true si el interruptor de alto contraste está encendido
     * @param sesion             sesión con el ususario que inició sesión
     * @param redirectAttributes para  enviar el mensaje después de redirigir
     * @return la dirección a "Mi perfil"
     */
    @PostMapping("/perfil/accesibilidad")
    public String guardarPreferencias(@RequestParam(defaultValue = "false") boolean letraGrande,
                                      @RequestParam(defaultValue = "false") boolean altoContraste,
                                      HttpSession sesion,
                                      RedirectAttributes redirectAttributes) {
        Usuario usuario = (Usuario) sesion.getAttribute("usuario");

        if (usuario == null) {
            return "redirect:/login";
        }
        try {
            PreferenciasAccesibilidad guardadas = preferenciasService.guardar(usuario.getId(), letraGrande, altoContraste);
            sesion.setAttribute(PreferenciasService.ATRIBUTO_SESION, guardadas);
            redirectAttributes.addFlashAttribute("mensaje", "Guardamos tus preferencias de accesibilidad.");
        } catch (DataAccessException | IllegalArgumentException e) {
            //Caso de fallo: la sesión no cambia, así que la página se ve como antes
            redirectAttributes.addFlashAttribute("errorAccesibilidad", "No pudimos guardar tus preferencias intenta de nuevo.");

        }
        return "redirect:/perfil";
    }
}
