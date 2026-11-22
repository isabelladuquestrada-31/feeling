package com.feeling.feeling.config;

import com.feeling.feeling.model.PreferenciasAccesibilidad;
import com.feeling.feeling.service.PreferenciasService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Envía las preferencias de accesibilidad  correspondientes al RF-04 a TODAS las vistas, para que el
 * layout pueda poner las clases CSS en la página sin que cada controlador lo haga.
 * Las preferencias salen de la sesión, donde se cargan al iniciar sesión.
 *
 * @author Daniel Blandón
 * @version 1.0
 */
@ControllerAdvice
public class PreferenciasAdvice {

    /**
     * Se ejecuta antes de cada método de los controladores y agrega al modelo:
     * letraGrande, altoContraste (para los interruptores) y clases Accesibilidad
     * por ejemplo "letra-grande alto-contraste", para el body del layout.
     *
     * @param request peticion actual
     * @param model   modelo que recibe la vista
     */
    @ModelAttribute
    public void agregarPreferencias(HttpServletRequest request, Model model) {
        //getSession(false) no crea una sesión nueva en las páginas públicas.
        HttpSession sesion = request.getSession(false);
        PreferenciasAccesibilidad preferencias = sesion == null ? null
                : (PreferenciasAccesibilidad) sesion.getAttribute(PreferenciasService.ATRIBUTO_SESION);
        model.addAttribute("letraGrande", preferencias != null && preferencias.isLetraGrande());
        model.addAttribute("altoContraste", preferencias != null && preferencias.isAltoContraste());
        model.addAttribute("clasesAccesibilidad", PreferenciasService.clasesCss(preferencias));
    }
}
