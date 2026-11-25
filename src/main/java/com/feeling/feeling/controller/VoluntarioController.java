package com.feeling.feeling.controller;

import com.feeling.feeling.model.Administrador;
import com.feeling.feeling.model.Usuario;
import com.feeling.feeling.service.BusquedaVoluntariosService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Clase que atiende la busqueda de voluntarios (RF-07).
 *
 * @author Daniel Blandón
 */
@Controller
public class VoluntarioController {

    /**
     * Servicio con la lógica de la búsqueda.
     */
    private final BusquedaVoluntariosService busquedaService;

    /**
     * Crea el controlador con el servicio que entrega Spring.
     *
     * @param busquedaService servicio de búsqueda de voluntarios
     */
    public VoluntarioController(BusquedaVoluntariosService busquedaService) {
        this.busquedaService = busquedaService;
    }

    /**
     * RF-07: muestra la pantalla "Buscar voluntarios" con los resultados de los filtros.
     *
     * @param materia  código de la materia elegida (opcional)
     * @param tipo     tipo de apoyo elegido (opcional)
     * @param programa código del programa académico elegido (opcional)
     * @param sesion   sesión con el usuario que inició sesión
     * @param model    datos que se envían a la vista
     * @return la vista de búsqueda de voluntarios
     */
    @GetMapping("/voluntarios")
    public String buscar(@RequestParam(required = false) String materia,
                         @RequestParam(required = false) String tipo,
                         @RequestParam(required = false) String programa,
                         HttpSession sesion,
                         Model model) {
        model.addAttribute("voluntarios", busquedaService.buscar(materia, tipo, programa));
        model.addAttribute("materias", busquedaService.materias());
        model.addAttribute("tiposApoyo", busquedaService.tiposApoyo());
        model.addAttribute("programas", busquedaService.programas());
        model.addAttribute("materiaSeleccionada", materia == null ? "" : materia);
        model.addAttribute("tipoSeleccionado", tipo == null ? "" : tipo);
        model.addAttribute("programaSeleccionado", programa == null ? "" : programa);
        model.addAttribute("paginaActiva", "voluntarios");
        if (sesion.getAttribute("usuario") instanceof Usuario usuario) {
            model.addAttribute("usuarioNombre", usuario.getNombreUsuario());
            model.addAttribute("usuarioRol", usuario instanceof Administrador ? "Administrador" : "Estudiante");
        }
        return "paginas/buscar-voluntarios";
    }
}
