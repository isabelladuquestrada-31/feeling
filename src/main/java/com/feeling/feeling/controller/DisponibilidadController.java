package com.feeling.feeling.controller;

import com.feeling.feeling.model.Administrador;
import com.feeling.feeling.model.DiaSemana;
import com.feeling.feeling.model.Disponibilidad;
import com.feeling.feeling.model.Estudiante;
import com.feeling.feeling.model.Voluntario;
import com.feeling.feeling.service.DisponibilidadService;
import com.feeling.feeling.service.VoluntarioService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * Sección "Mi disponibilidad" del voluntario (RF-06, SCRUM-77): agrega, edita
 * y elimina sus franjas con el servicio de disponibilidad (SCRUM-76).
 *
 * @author Daniel Blandón
 * @version 1.0
 */
@Controller
public class DisponibilidadController {

    private static final String REDIRECCION = "redirect:/disponibilidad";

    /** Nombre de cada día para mostrarlo en la vista. */
    private static final Map<String, String> NOMBRES_DIAS = new LinkedHashMap<>();

    static {
        NOMBRES_DIAS.put(DiaSemana.LUNES.name(), "Lunes");
        NOMBRES_DIAS.put(DiaSemana.MARTES.name(), "Martes");
        NOMBRES_DIAS.put(DiaSemana.MIERCOLES.name(), "Miércoles");
        NOMBRES_DIAS.put(DiaSemana.JUEVES.name(), "Jueves");
        NOMBRES_DIAS.put(DiaSemana.VIERNES.name(), "Viernes");
        NOMBRES_DIAS.put(DiaSemana.SABADO.name(), "Sábado");
        NOMBRES_DIAS.put(DiaSemana.DOMINGO.name(), "Domingo");
    }

    private final DisponibilidadService disponibilidadService;
    private final VoluntarioService voluntarioService;

    /**
     * Crea el controlador con los servicios que necesita.
     *
     * @param disponibilidadService servicio de la disponibilidad (SCRUM-76)
     * @param voluntarioService     servicio para saber si el estudiante es voluntario
     */
    public DisponibilidadController(DisponibilidadService disponibilidadService,
                                    VoluntarioService voluntarioService) {
        this.disponibilidadService = disponibilidadService;
        this.voluntarioService = voluntarioService;
    }

    /**
     * Muestra las franjas del voluntario y el formulario para agregar otra.
     * Si el estudiante todavía no es voluntario, la vista lo invita a activarse.
     *
     * @param sesion sesión con el estudiante
     * @param model  datos que se envían a la vista
     * @return la vista de disponibilidad, o la redirección que corresponda
     */
    @GetMapping("/disponibilidad")
    public String ver(HttpSession sesion, Model model) {
        Object usuario = sesion.getAttribute("usuario");
        if (!(usuario instanceof Estudiante estudiante)) {
            return usuario instanceof Administrador ? "redirect:/admin" : "redirect:/login";
        }

        Optional<Voluntario> voluntario = voluntarioService.buscarPorEstudiante(estudiante.getId());
        List<Disponibilidad> franjas = voluntario
                .map(v -> disponibilidadService.listar(v.getId()))
                .orElse(List.of());

        // Franjas agrupadas por día para el resumen de la semana.
        Map<String, List<Disponibilidad>> franjasPorDia = new LinkedHashMap<>();
        NOMBRES_DIAS.keySet().forEach(dia -> franjasPorDia.put(dia, franjas.stream()
                .filter(f -> f.getDia().name().equals(dia))
                .toList()));

        model.addAttribute("esVoluntario", voluntario.isPresent());
        model.addAttribute("franjas", franjas);
        model.addAttribute("franjasPorDia", franjasPorDia);
        model.addAttribute("dias", NOMBRES_DIAS);
        model.addAttribute("idioma", Locale.forLanguageTag("es-CO"));

        model.addAttribute("paginaActiva", "ser-voluntario");
        model.addAttribute("usuarioNombre", estudiante.getNombreUsuario());
        model.addAttribute("usuarioRol", "Estudiante");
        return "paginas/disponibilidad";
    }

    /**
     * Agrega una franja (caso de éxito) o explica por qué no se pudo (caso de fallo).
     *
     * @param dia                día de la semana
     * @param horaInicio         hora de inicio (HH:mm)
     * @param horaFin            hora de fin (HH:mm)
     * @param sesion             sesión con el voluntario
     * @param redirectAttributes mensaje y datos escritos para volver a mostrarlos
     * @return redirección a Mi disponibilidad
     */
    @PostMapping("/disponibilidad")
    public String agregar(@RequestParam(required = false) String dia,
                          @RequestParam(required = false) String horaInicio,
                          @RequestParam(required = false) String horaFin,
                          HttpSession sesion, RedirectAttributes redirectAttributes) {
        Optional<Integer> voluntarioId = voluntarioDeLaSesion(sesion);
        if (voluntarioId.isEmpty()) {
            return redireccionSinVoluntario(sesion, redirectAttributes);
        }
        try {
            disponibilidadService.agregar(voluntarioId.get(), leerDia(dia), leerHora(horaInicio), leerHora(horaFin));
            redirectAttributes.addFlashAttribute("mensajeExito",
                    "Agregaste la franja del " + nombreDia(dia) + " de " + horaInicio + " a " + horaFin + ".");
            redirectAttributes.addFlashAttribute("resaltar", dia + "-" + horaInicio);
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("mensajeError", e.getMessage());
            redirectAttributes.addFlashAttribute("diaEscrito", dia);
            redirectAttributes.addFlashAttribute("inicioEscrito", horaInicio);
            redirectAttributes.addFlashAttribute("finEscrito", horaFin);
        }
        return REDIRECCION;
    }

    /**
     * Cambia el día o las horas de una franja.
     *
     * @param id                 id de la franja
     * @param dia                nuevo día
     * @param horaInicio         nueva hora de inicio
     * @param horaFin            nueva hora de fin
     * @param sesion             sesión con el voluntario
     * @param redirectAttributes mensaje y datos escritos para volver a mostrarlos
     * @return redirección a Mi disponibilidad
     */
    @PostMapping("/disponibilidad/{id}/editar")
    public String editar(@PathVariable Integer id,
                         @RequestParam(required = false) String dia,
                         @RequestParam(required = false) String horaInicio,
                         @RequestParam(required = false) String horaFin,
                         HttpSession sesion, RedirectAttributes redirectAttributes) {
        Optional<Integer> voluntarioId = voluntarioDeLaSesion(sesion);
        if (voluntarioId.isEmpty()) {
            return redireccionSinVoluntario(sesion, redirectAttributes);
        }
        try {
            disponibilidadService.editar(id, voluntarioId.get(), leerDia(dia), leerHora(horaInicio), leerHora(horaFin));
            redirectAttributes.addFlashAttribute("mensajeExito", "Guardaste los cambios de la franja.");
            redirectAttributes.addFlashAttribute("resaltar", dia + "-" + horaInicio);
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("mensajeError", e.getMessage());
            redirectAttributes.addFlashAttribute("editandoId", id);
            redirectAttributes.addFlashAttribute("diaEscrito", dia);
            redirectAttributes.addFlashAttribute("inicioEscrito", horaInicio);
            redirectAttributes.addFlashAttribute("finEscrito", horaFin);
        }
        return REDIRECCION;
    }

    /**
     * Elimina una franja del voluntario.
     *
     * @param id                 id de la franja
     * @param sesion             sesión con el voluntario
     * @param redirectAttributes mensaje de éxito o de error
     * @return redirección a Mi disponibilidad
     */
    @PostMapping("/disponibilidad/{id}/eliminar")
    public String eliminar(@PathVariable Integer id, HttpSession sesion, RedirectAttributes redirectAttributes) {
        Optional<Integer> voluntarioId = voluntarioDeLaSesion(sesion);
        if (voluntarioId.isEmpty()) {
            return redireccionSinVoluntario(sesion, redirectAttributes);
        }
        try {
            disponibilidadService.eliminar(id, voluntarioId.get());
            redirectAttributes.addFlashAttribute("mensajeExito", "Eliminaste la franja.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("mensajeError", e.getMessage());
        }
        return REDIRECCION;
    }

    /**
     * Devuelve el id del voluntario que inició sesión, si el estudiante es voluntario.
     */
    private Optional<Integer> voluntarioDeLaSesion(HttpSession sesion) {
        if (!(sesion.getAttribute("usuario") instanceof Estudiante estudiante)) {
            return Optional.empty();
        }
        return voluntarioService.buscarPorEstudiante(estudiante.getId()).map(Voluntario::getId);
    }

    /**
     * Redirección cuando no hay un voluntario en la sesión.
     */
    private String redireccionSinVoluntario(HttpSession sesion, RedirectAttributes redirectAttributes) {
        Object usuario = sesion.getAttribute("usuario");
        if (usuario instanceof Administrador) {
            return "redirect:/admin";
        }
        if (!(usuario instanceof Estudiante)) {
            return "redirect:/login";
        }
        redirectAttributes.addFlashAttribute("mensajeError", "Solo los voluntarios pueden registrar disponibilidad.");
        return REDIRECCION;
    }

    /**
     * Convierte el día del formulario; vacío o desconocido da null (el servicio pide completarlo).
     */
    private static DiaSemana leerDia(String dia) {
        if (dia == null || dia.isBlank()) {
            return null;
        }
        try {
            return DiaSemana.valueOf(dia);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * Convierte una hora "HH:mm"; vacía o inválida da null (el servicio pide completarla).
     */
    private static LocalTime leerHora(String hora) {
        if (hora == null || hora.isBlank()) {
            return null;
        }
        try {
            return LocalTime.parse(hora);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    /**
     * Nombre del día en minúsculas para los mensajes ("lunes").
     */
    private static String nombreDia(String dia) {
        return NOMBRES_DIAS.getOrDefault(dia, "día elegido").toLowerCase();
    }
}
