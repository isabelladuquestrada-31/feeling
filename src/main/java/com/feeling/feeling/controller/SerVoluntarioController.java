package com.feeling.feeling.controller;

import com.feeling.feeling.model.Estudiante;
import com.feeling.feeling.model.TipoApoyo;
import com.feeling.feeling.model.Usuario;
import com.feeling.feeling.service.VoluntarioService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * Maneja la activación de un estudiante como voluntario (RF-05).
 *
 * @author Isabella Duque
 * @version 1.0
 */
@Controller
public class SerVoluntarioController {

    private final VoluntarioService voluntarioService;

    /**
     * Crea el controlador con el servicio de voluntarios.
     *
     * @param voluntarioService servicio que activa a los voluntarios
     */
    public SerVoluntarioController(VoluntarioService voluntarioService) {
        this.voluntarioService = voluntarioService;
    }

    /**
     * Muestra el formulario para activarse como voluntario.
     *
     * @param sesion sesión donde está el usuario que inició sesión
     * @param model  modelo para enviar las materias y los tipos de apoyo a la vista
     * @return la vista "Ser voluntario", o una redirección si no es estudiante
     */
    @GetMapping("/ser-voluntario")
    public String mostrarFormulario(HttpSession sesion, Model model) {
        Usuario usuario = (Usuario) sesion.getAttribute("usuario");

        if (usuario == null) {
            return "redirect:/login";
        }
        if (!(usuario instanceof Estudiante)) {
            return "redirect:/";
        }

        cargarOpciones(model);
        return "paginas/ser-voluntario";
    }

    /**
     * Activa al estudiante como voluntario con las materias y tipos de apoyo elegidos.
     *
     * @param materias           códigos de las materias elegidas (puede llegar vacío)
     * @param tiposApoyo         tipos de apoyo elegidos (puede llegar vacío)
     * @param sesion             sesión donde está el usuario que inició sesión
     * @param model              modelo para enviar el mensaje de error a la vista
     * @param redirectAttributes permite enviar un mensaje de éxito después de redirigir
     * @return la redirección al inicio si todo salió bien, o el formulario con el error
     */
    @PostMapping("/ser-voluntario")
    public String activar(@RequestParam(required = false) List<String> materias,
                          @RequestParam(required = false) List<TipoApoyo> tiposApoyo,
                          HttpSession sesion,
                          Model model,
                          RedirectAttributes redirectAttributes) {
        Usuario usuario = (Usuario) sesion.getAttribute("usuario");

        if (usuario == null) {
            return "redirect:/login";
        }

        try {
            voluntarioService.activarVoluntario(usuario.getId(), materias, tiposApoyo);
        } catch (IllegalArgumentException | IllegalStateException e) {
            model.addAttribute("error", e.getMessage());
            cargarOpciones(model);
            return "paginas/ser-voluntario";
        }

        redirectAttributes.addFlashAttribute("mensaje", "¡Ya eres voluntario! Ahora apareces en la búsqueda.");
        return "redirect:/";
    }

    /**
     * Envía a la vista las materias y los tipos de apoyo que se pueden elegir.
     *
     * @param model modelo de la vista
     */
    private void cargarOpciones(Model model) {
        model.addAttribute("materias", voluntarioService.listarMaterias());
        model.addAttribute("tiposApoyo", TipoApoyo.values());
    }
}