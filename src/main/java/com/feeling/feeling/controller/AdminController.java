package com.feeling.feeling.controller;

import com.feeling.feeling.model.Administrador;
import com.feeling.feeling.model.Estudiante;
import com.feeling.feeling.model.Usuario;
import com.feeling.feeling.model.Voluntario;
import com.feeling.feeling.service.GestionUsuariosService;
import com.feeling.feeling.service.PanelService;
import jakarta.servlet.http.HttpSession;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Maneja las páginas del administrador.
 * Solo pueden entrar usuarios con el rol de administrador
 *
 * @author Isabella Duque
 * @version 1.1
 */
@Controller
public class AdminController {

    /** Largo máximo del motivo de la suspensión, igual al de la base de datos. */
    private static final int LARGO_MAXIMO_MOTIVO = 255;

    private final PanelService panelService;
    private final GestionUsuariosService  gestionUsuariosService;

    /**
     * Crea el controlador con el servicio de las pantallas de inicio.
     *
     * @param panelService datos de las pantallas de inicio
     * @param gestionUsuariosService gestión de usuarios y suspensiones
     */
    public AdminController(PanelService panelService,  GestionUsuariosService gestionUsuariosService) {
        this.panelService = panelService;
        this.gestionUsuariosService = gestionUsuariosService;
    }

    /**
     * Muestra la pantalla de inicio del administrador (SCRUM-53):
     * saludo, resumen de usuarios y acceso a la gestión de usuarios.
     *
     * @param sesion sesión con el administrador que inició sesión
     * @param model  datos que se envían a la vista
     * @return la vista de inicio del administrador
     */
    @GetMapping("/admin")
    public String inicioAdmin(HttpSession sesion, Model model) {
        Usuario admin = (Usuario) sesion.getAttribute("usuario");

        model.addAttribute("nombre", panelService.nombreParaSaludo(admin));
        model.addAttribute("resumen", panelService.resumenAdmin());

        model.addAttribute("paginaActiva", "inicio");
        model.addAttribute("usuarioNombre", admin.getNombreUsuario());
        model.addAttribute("usuarioRol", "Administrador");
        return "paginas/admin";
    }
    /**
     * Muestra la gestión de usuarios (RF-17): lista de usuarios y de voluntarios
     * con su estado, para suspenderlos o reactivarlos.
     *
     * @param sesion sesión con el administrador que inició sesión
     * @param model  datos que se envían a la vista
     * @return la vista de gestión de usuarios
     */
    @GetMapping("/admin/usuarios")
    public String gestionUsuarios(HttpSession sesion, Model model) {
        Usuario admin = (Usuario) sesion.getAttribute("usuario");

        List<Usuario> usuarios = gestionUsuariosService.listarUsuarios();
        List<Voluntario> voluntarios = gestionUsuariosService.listarVoluntarios();
        model.addAttribute("usuarios", usuarios);
        model.addAttribute("voluntarios", voluntarios);
        model.addAttribute("roles", rolesDe(usuarios));
        model.addAttribute("suspensiones", gestionUsuariosService.suspensionesVigentes(voluntarios));
        model.addAttribute("fechaMinimaFin", LocalDate.now().plusDays(1));
        model.addAttribute("largoMaximoMotivo", LARGO_MAXIMO_MOTIVO);

        model.addAttribute("paginaActiva", "usuarios");
        model.addAttribute("usuarioNombre", admin.getNombreUsuario());
        model.addAttribute("usuarioRol", "Administrador");
        return "paginas/admin-usuarios";
    }

    /**
     * Suspende a un voluntario con un motivo y una fecha de fin (RF-17).
     *
     * @param voluntarioId id del voluntario a suspender
     * @param motivo       razón de la suspensión
     * @param fechaFin     fecha en que termina la suspensión
     * @param sesion       sesión con el administrador que inició sesión
     * @param redirect     mensajes que se muestran después de redirigir
     * @return redirección a la gestión de usuarios
     */
    @PostMapping("/admin/usuarios/{voluntarioId}/suspender")
    public String suspender(@PathVariable Integer voluntarioId,
                            @RequestParam(required = false) String motivo,
                            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
                            HttpSession sesion,
                            RedirectAttributes redirect) {
        Administrador admin = (Administrador) sesion.getAttribute("usuario");
        if (motivo != null && motivo.trim().length() > LARGO_MAXIMO_MOTIVO) {
            redirect.addFlashAttribute("error",
                    "El motivo puede tener máximo " + LARGO_MAXIMO_MOTIVO + " caracteres.");
            return "redirect:/admin/usuarios";
        }
        try {
            gestionUsuariosService.suspenderVoluntario(voluntarioId, admin, motivo, fechaFin);
            redirect.addFlashAttribute("exito", "El voluntario fue suspendido.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/usuarios";
    }

    /**
     * Reactiva a un voluntario suspendido (RF-17).
     *
     * @param voluntarioId id del voluntario a reactivar
     * @param redirect     mensajes que se muestran después de redirigir
     * @return redirección a la gestión de usuarios
     */
    @PostMapping("/admin/usuarios/{voluntarioId}/reactivar")
    public String reactivar(@PathVariable Integer voluntarioId, RedirectAttributes redirect) {
        try {
            gestionUsuariosService.reactivarVoluntario(voluntarioId);
            redirect.addFlashAttribute("exito", "El voluntario fue reactivado.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/usuarios";
    }

    /**
     * Texto del rol de cada usuario para la tabla de usuarios (RF-17).
     *
     * @param usuarios usuarios de la plataforma
     * @return rol de cada usuario por su id
     */
    private Map<Integer, String> rolesDe(List<Usuario> usuarios) {
        Map<Integer, String> roles = new HashMap<>();
        for (Usuario usuario : usuarios) {
            String rol = "Estudiante";
            if (usuario instanceof Administrador) {
                rol = "Administrador";
            } else if (usuario instanceof Estudiante estudiante && estudiante.getVoluntario() != null) {
                rol = "Estudiante y voluntario";
            }
            roles.put(usuario.getId(), rol);
        }
        return roles;
    }
}
