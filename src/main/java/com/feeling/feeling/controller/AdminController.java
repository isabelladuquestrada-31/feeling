package com.feeling.feeling.controller;

import com.feeling.feeling.model.Usuario;
import com.feeling.feeling.service.PanelService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Maneja las páginas del administrador.
 * Solo pueden entrar usuarios con el rol de administrador
 * (lo controla {@link com.feeling.feeling.config.AdminInterceptor}).
 *
 * @author Isabella Duque
 * @version 1.1
 */
@Controller
public class AdminController {

    private final PanelService panelService;

    /**
     * Crea el controlador con el servicio de las pantallas de inicio.
     *
     * @param panelService datos de las pantallas de inicio
     */
    public AdminController(PanelService panelService) {
        this.panelService = panelService;
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
}
