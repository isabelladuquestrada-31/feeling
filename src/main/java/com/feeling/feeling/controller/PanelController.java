package com.feeling.feeling.controller;

import com.feeling.feeling.model.Administrador;
import com.feeling.feeling.model.Estudiante;
import com.feeling.feeling.service.PanelService;
import com.feeling.feeling.service.SolicitudService;
import com.feeling.feeling.service.VoluntarioService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Locale;

/**
 * Pantalla de inicio del estudiante después de iniciar sesión (SCRUM-53, RF-02).
 * Si entra un administrador, lo lleva a su propia pantalla de inicio.
 *
 * @author Daniel Blandón
 * @version 1.0
 */
@Controller
public class PanelController {

    /** Cantidad de próximas solicitudes que se muestran en el inicio. */
    private static final int PROXIMAS_EN_INICIO = 3;

    /** Idioma de las fechas ("jue, 23 oct"), sin depender del navegador. */
    private static final Locale ESPANOL = Locale.forLanguageTag("es-CO");

    private final PanelService panelService;
    private final SolicitudService solicitudService;
    private final VoluntarioService voluntarioService;

    /**
     * Crea el controlador con los servicios que necesita.
     *
     * @param panelService      datos de las pantallas de inicio
     * @param solicitudService  solicitudes del estudiante
     * @param voluntarioService perfil de voluntario del estudiante
     */
    public PanelController(PanelService panelService,
                           SolicitudService solicitudService,
                           VoluntarioService voluntarioService) {
        this.panelService = panelService;
        this.solicitudService = solicitudService;
        this.voluntarioService = voluntarioService;
    }

    /**
     * Muestra el inicio del estudiante: saludo, accesos a sus funciones
     * y sus próximas solicitudes.
     *
     * @param acceso indica si se llegó aquí porque se intentó entrar al panel del administrador
     * @param sesion sesión con el usuario que inició sesión
     * @param model  datos que se envían a la vista
     * @return la vista de inicio del estudiante, o la redirección que corresponda
     */
    @GetMapping("/inicio")
    public String inicio(@RequestParam(name = "acceso", required = false) String acceso,
                         HttpSession sesion,
                         Model model) {
        Object usuario = sesion.getAttribute("usuario");

        if (usuario == null) {
            return "redirect:/login";
        }
        if (usuario instanceof Administrador) {
            return "redirect:/admin";
        }
        if (!(usuario instanceof Estudiante estudiante)) {
            return "redirect:/login";
        }

        model.addAttribute("nombre", panelService.nombreParaSaludo(estudiante));
        model.addAttribute("proximas",
                solicitudService.proximasSolicitudesDelEstudiante(estudiante.getId(), PROXIMAS_EN_INICIO));
        model.addAttribute("nombresMaterias", panelService.nombresDeMaterias());
        model.addAttribute("idioma", ESPANOL);
        voluntarioService.buscarPorEstudiante(estudiante.getId()).ifPresent(voluntario -> {
            model.addAttribute("voluntario", voluntario);
            model.addAttribute("pendientesRecibidas",
                    solicitudService.solicitudesRecibidas(voluntario.getId()).size());
            model.addAttribute("horasVoluntariado",
                    voluntarioService.calcularHorasVoluntariado(voluntario.getId()));
        });
        model.addAttribute("accesoDenegado", "denegado".equals(acceso));

        model.addAttribute("paginaActiva", "inicio");
        model.addAttribute("usuarioNombre", estudiante.getNombreUsuario());
        model.addAttribute("usuarioRol", "Estudiante");
        return "paginas/inicio-estudiante";
    }
}
