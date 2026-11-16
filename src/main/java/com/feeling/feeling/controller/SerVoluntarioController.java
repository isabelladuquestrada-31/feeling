package com.feeling.feeling.controller;

import com.feeling.feeling.model.Estudiante;
import com.feeling.feeling.model.Materia;
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

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Maneja la activación de un estudiante como voluntario (RF-05).
 *
 * @author Isabella Duque
 * @version 1.1
 */
@Controller
public class SerVoluntarioController {

    /**
     * Nombre y explicación corta de cada tipo de apoyo, en el orden en que se muestran.
     */
    private static final Map<String, String[]> TEXTOS_APOYO = new LinkedHashMap<>();

    static {
        TEXTOS_APOYO.put(TipoApoyo.EXPLICACION_CONCEPTOS.name(),
                new String[]{"Explicación de conceptos", "Explicar los temas que a otros se les dificultan."});
        TEXTOS_APOYO.put(TipoApoyo.TUTORIA.name(),
                new String[]{"Tutoría", "Acompañar a un compañero en una materia durante varias sesiones."});
        TEXTOS_APOYO.put(TipoApoyo.PREPARACION_EXAMENES.name(),
                new String[]{"Preparación de exámenes", "Repasar y practicar antes de un parcial o un quiz."});
        TEXTOS_APOYO.put(TipoApoyo.COMPRENSION_MATERIAL.name(),
                new String[]{"Comprensión de material", "Ayudar a entender apuntes, lecturas o guías de estudio."});
    }

    /**
     * Devuelve el nombre en español de cada tipo de apoyo ("Tutoría"),
     * para mostrarlo en otras vistas como las solicitudes recibidas.
     *
     * @return mapa del nombre del enum a su etiqueta
     */
    static Map<String, String> etiquetasDeApoyo() {
        Map<String, String> etiquetas = new LinkedHashMap<>();
        TEXTOS_APOYO.forEach((tipo, textos) -> etiquetas.put(tipo, textos[0]));
        return etiquetas;
    }

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
     * Muestra el formulario para activarse como voluntario. Si el estudiante ya es
     * voluntario, muestra su perfil de voluntario en lugar del formulario.
     *
     * @param sesion sesión donde está el usuario que inició sesión
     * @param model  modelo para enviar las materias, los tipos de apoyo y el perfil a la vista
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

        voluntarioService.buscarPorEstudiante(usuario.getId())
                .ifPresent(voluntario -> model.addAttribute("voluntario", voluntario));
        cargarOpciones(usuario, model);
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
     * @return la misma página con el perfil de voluntario si todo salió bien,
     *         o el formulario con el error y lo que ya había elegido
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
            model.addAttribute("materiasElegidas", materias == null ? List.of() : materias);
            model.addAttribute("tiposElegidos", tiposApoyo == null ? List.of()
                    : tiposApoyo.stream().map(TipoApoyo::name).toList());
            voluntarioService.buscarPorEstudiante(usuario.getId())
                    .ifPresent(voluntario -> model.addAttribute("voluntario", voluntario));
            cargarOpciones(usuario, model);
            return "paginas/ser-voluntario";
        }

        redirectAttributes.addFlashAttribute("mensaje", "¡Ya eres voluntario! Ahora apareces en la búsqueda.");
        return "redirect:/ser-voluntario";
    }

    /**
     * Envía a la vista las materias, los tipos de apoyo con sus textos y los datos
     * del menú lateral.
     *
     * @param usuario usuario que inició sesión
     * @param model   modelo de la vista
     */
    private void cargarOpciones(Usuario usuario, Model model) {
        model.addAttribute("materias", voluntarioService.listarMaterias().stream()
                .sorted(Comparator.comparing(Materia::getNombre))
                .toList());
        model.addAttribute("tiposApoyo", TEXTOS_APOYO);
        model.addAttribute("paginaActiva", "ser-voluntario");
        model.addAttribute("usuarioNombre", usuario.getNombreUsuario());
        model.addAttribute("usuarioRol", "Estudiante");
    }
}
