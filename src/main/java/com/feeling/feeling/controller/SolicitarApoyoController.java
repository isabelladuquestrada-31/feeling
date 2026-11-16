package com.feeling.feeling.controller;

import com.feeling.feeling.dto.HorarioLibre;
import com.feeling.feeling.model.Administrador;
import com.feeling.feeling.model.Estudiante;
import com.feeling.feeling.model.Voluntario;
import com.feeling.feeling.service.PanelService;
import com.feeling.feeling.service.SolicitarApoyoService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Formulario "Solicitar apoyo" (RF-08, SCRUM-81): el estudiante elige materia, tema,
 * tipo de apoyo, un horario libre del voluntario y la modalidad, y se crea la
 * solicitud en estado PENDIENTE.
 *
 * @author Daniel Blandón
 * @version 1.0
 */
@Controller
public class SolicitarApoyoController {

    /** Idioma de las fechas ("lun, 23 nov"), sin depender del navegador. */
    private static final Locale ESPANOL = Locale.forLanguageTag("es-CO");

    private static final String VISTA = "paginas/solicitar-apoyo";
    private static final String REDIRECCION_BUSQUEDA = "redirect:/voluntarios";

    private final SolicitarApoyoService solicitarApoyoService;
    private final PanelService panelService;

    /**
     * Crea el controlador con los servicios que necesita.
     *
     * @param solicitarApoyoService horarios libres y creación de la solicitud
     * @param panelService          nombres de los estudiantes
     */
    public SolicitarApoyoController(SolicitarApoyoService solicitarApoyoService, PanelService panelService) {
        this.solicitarApoyoService = solicitarApoyoService;
        this.panelService = panelService;
    }

    /**
     * Muestra el formulario para pedirle apoyo a un voluntario.
     *
     * @param voluntarioId       id del voluntario elegido en la búsqueda
     * @param sesion             sesión con el estudiante
     * @param model              datos que se envían a la vista
     * @param redirectAttributes mensaje si el voluntario no se puede elegir
     * @return el formulario, o la redirección que corresponda
     */
    @GetMapping("/solicitudes/nueva")
    public String formulario(@RequestParam(name = "voluntario", required = false) Integer voluntarioId,
                             HttpSession sesion, Model model, RedirectAttributes redirectAttributes) {
        Object usuario = sesion.getAttribute("usuario");
        if (!(usuario instanceof Estudiante estudiante)) {
            return usuario instanceof Administrador ? "redirect:/admin" : "redirect:/login";
        }

        Optional<Voluntario> voluntario = solicitarApoyoService.voluntarioActivo(voluntarioId);
        if (voluntario.isEmpty()) {
            redirectAttributes.addFlashAttribute("mensajeError",
                    "Ese voluntario no está disponible. Elige otro de la lista.");
            return REDIRECCION_BUSQUEDA;
        }
        if (Objects.equals(voluntario.get().getEstudiante().getId(), estudiante.getId())) {
            redirectAttributes.addFlashAttribute("mensajeError", "No puedes solicitarte apoyo a ti mismo.");
            return REDIRECCION_BUSQUEDA;
        }

        // Si el voluntario solo tiene una materia o un tipo de apoyo, ya quedan elegidos.
        Voluntario v = voluntario.get();
        String materia = v.getMaterias().size() == 1 ? v.getMaterias().get(0).getCodigo() : null;
        String tipo = v.getTiposApoyo().size() == 1 ? v.getTiposApoyo().get(0).name() : null;
        cargarFormulario(model, estudiante, v, materia, null, tipo, null, null);
        return VISTA;
    }

    /**
     * Recibe el formulario y crea la solicitud. Si falta algo o el horario ya se ocupó,
     * vuelve a mostrar el formulario con lo que escribió y el motivo.
     *
     * @param voluntarioId       id del voluntario
     * @param materia            código de la materia
     * @param tema               tema que quiere trabajar
     * @param tipo               tipo de apoyo
     * @param horario            inicio del horario elegido (fecha y hora)
     * @param modalidad          PRESENCIAL o VIRTUAL
     * @param sesion             sesión con el estudiante
     * @param model              datos para volver a mostrar el formulario
     * @param redirectAttributes mensaje de éxito
     * @return la redirección a Mis acompañamientos, o el formulario con el error
     */
    @PostMapping("/solicitudes/nueva")
    public String enviar(@RequestParam(name = "voluntario", required = false) Integer voluntarioId,
                         @RequestParam(required = false) String materia,
                         @RequestParam(required = false) String tema,
                         @RequestParam(required = false) String tipo,
                         @RequestParam(required = false) String horario,
                         @RequestParam(required = false) String modalidad,
                         HttpSession sesion, Model model, RedirectAttributes redirectAttributes) {
        Object usuario = sesion.getAttribute("usuario");
        if (!(usuario instanceof Estudiante estudiante)) {
            return usuario instanceof Administrador ? "redirect:/admin" : "redirect:/login";
        }

        Optional<Voluntario> voluntario = solicitarApoyoService.voluntarioActivo(voluntarioId);
        if (voluntario.isEmpty()) {
            redirectAttributes.addFlashAttribute("mensajeError",
                    "Ese voluntario no está disponible. Elige otro de la lista.");
            return REDIRECCION_BUSQUEDA;
        }

        try {
            solicitarApoyoService.solicitar(estudiante.getId(), voluntarioId, materia, tema, tipo, modalidad,
                    leerHorario(horario), LocalDateTime.now());
        } catch (IllegalArgumentException | IllegalStateException e) {
            model.addAttribute("error", e.getMessage());
            cargarFormulario(model, estudiante, voluntario.get(), materia, tema, tipo, horario, modalidad);
            return VISTA;
        }

        redirectAttributes.addFlashAttribute("mensajeExito", "Enviaste tu solicitud a "
                + nombreDe(voluntario.get()) + ". Queda pendiente hasta que la acepte.");
        return "redirect:/mis-acompanamientos";
    }

    /**
     * Pone en el modelo todo lo que necesita el formulario y lo que el estudiante ya había elegido.
     */
    private void cargarFormulario(Model model, Estudiante estudiante, Voluntario voluntario,
                                  String materia, String tema, String tipo, String horario, String modalidad) {
        List<HorarioLibre> horarios = solicitarApoyoService.horariosLibres(voluntario.getId(), LocalDateTime.now());
        Map<LocalDate, List<HorarioLibre>> horariosPorDia = new LinkedHashMap<>();
        horarios.forEach(h -> horariosPorDia.computeIfAbsent(h.inicio().toLocalDate(), d -> new ArrayList<>()).add(h));

        model.addAttribute("voluntario", voluntario);
        model.addAttribute("nombreVoluntario", nombreDe(voluntario));
        model.addAttribute("horariosPorDia", horariosPorDia);
        model.addAttribute("diasDeHorarios", SolicitarApoyoService.DIAS_DE_HORARIOS);
        model.addAttribute("largoMaximoTema", SolicitarApoyoService.LARGO_MAXIMO_TEMA);
        model.addAttribute("etiquetasApoyo", SerVoluntarioController.etiquetasDeApoyo());
        model.addAttribute("idioma", ESPANOL);

        model.addAttribute("materiaElegida", materia);
        model.addAttribute("temaEscrito", tema);
        model.addAttribute("tipoElegido", tipo);
        model.addAttribute("horarioElegido", horario);
        model.addAttribute("modalidadElegida", modalidad);

        model.addAttribute("paginaActiva", "voluntarios");
        model.addAttribute("usuarioNombre", estudiante.getNombreUsuario());
        model.addAttribute("usuarioRol", "Estudiante");
    }

    /**
     * Nombre con el que se muestra al voluntario.
     */
    private String nombreDe(Voluntario voluntario) {
        Integer id = voluntario.getEstudiante().getId();
        return panelService.nombresDeEstudiantes(List.of(id))
                .getOrDefault(id, voluntario.getEstudiante().getNombreUsuario());
    }

    /**
     * Convierte el valor del horario ("2026-11-23T14:00") en fecha y hora; vacío o inválido da null.
     */
    private static LocalDateTime leerHorario(String horario) {
        if (horario == null || horario.isBlank()) {
            return null;
        }
        try {
            return LocalDateTime.parse(horario);
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}
