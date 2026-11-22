package com.feeling.feeling.controller;

import com.feeling.feeling.model.Estudiante;
import com.feeling.feeling.model.Perfil;
import com.feeling.feeling.model.Usuario;
import com.feeling.feeling.service.PerfilService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

/**
 * Muestra y guarda la pantalla "Mi perfil" del estudiante (RF-03).
 *
 * @author Daniel Blandón
 * @version 1.0
 */
@Controller
public class PerfilController {

    /**
     * Servicio con la lógica para consultar y editar el perfil.
     */
    private final PerfilService perfilService;

    /**
     * Crea el controlador con el servicio de perfiles.
     *
     * @param perfilService servicio de perfiles
     */
    public PerfilController(PerfilService perfilService) {
        this.perfilService = perfilService;
    }

    /**
     * Muestra el formulario de "Mi perfil" con los datos actuales del estudiante.
     *
     * @param sesion sesión con el usuario que inició sesión
     * @param model  modelo para enviar los datos a la vista
     * @return la vista del perfil, o una redirección a otra pagina si no es un estudiante
     */
    @GetMapping("/perfil")
    public String mostrarPerfil(HttpSession sesion, Model model) {
        Usuario usuario = (Usuario) sesion.getAttribute("usuario");

        if (usuario == null) {
            return "redirect:/login";
        }
        if (!(usuario instanceof Estudiante)) {

            return "redirect:/admin";
        }

        Perfil perfil = perfilService.obtenerPerfil(usuario.getId());
        cargarFormulario(model, perfil.getNombrePreferido(), perfil.getPronombres(),
                perfil.getWhatsapp(), Map.of());
        cargarDatosPagina(model, usuario);
        return "paginas/perfil";

    }

    /**
     * Recibe el formulario de "Mi perfil". Si hay errores vuelve a mostrar el formulario
     * con lo que escribió el estudiante; si no, guarda y muestra un mensaje de éxito.
     *
     * @param nombrePreferido    nombre preferido escrito en el formulario
     * @param pronombres         pronombres elegidos en el formulario
     * @param whatsapp           número de WhatsApp escrito en el formulario
     * @param sesion             sesión con el usuario que inició sesión
     * @param model              modelo para enviar los datos a la vista
     * @param redirectAttributes para enviar el mensaje de exito después de redirigir
     * @return la vista con errores, o la redirección a /perfil
     */
    @PostMapping("/perfil")
    public String guardarPerfil(@RequestParam(required = false) String nombrePreferido,
                                @RequestParam(required = false) String pronombres,
                                @RequestParam(required = false) String whatsapp,
                                HttpSession sesion,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        Usuario usuario = (Usuario) sesion.getAttribute("usuario");

        if (usuario == null) {
            return "redirect:/login";
        }
        if (!(usuario instanceof Estudiante)) {
            return "redirect:/admin";
        }

        Map<String, String> errores = perfilService.validar(nombrePreferido, pronombres, whatsapp);

        if (!errores.isEmpty()) {
            cargarFormulario(model, nombrePreferido, pronombres, whatsapp, errores);
            cargarDatosPagina(model, usuario);
            return "paginas/perfil";
        }
        perfilService.actualizarPerfil(usuario.getId(), nombrePreferido, pronombres, whatsapp);
        redirectAttributes.addFlashAttribute("mensaje", "Tus datos se guardaron correctamente");
        return "redirect:/perfil";
    }

    /**
     * Envía a la vista los valores del formulario, las opciones de pronombres y los errores.
     *
     * @param model           modelo para enviar los datos a la vista
     * @param nombrePreferido nombre preferido ingresado por el usuario
     * @param pronombres      pronombre seleccionado por el usuario
     * @param whatsapp        WhatsApp del usuario
     * @param errores         Errores que saltaron al llenar el formulario
     */
    private void cargarFormulario(Model model, String nombrePreferido, String pronombres, String whatsapp, Map<String, String> errores) {
        model.addAttribute("nombrePreferido", nombrePreferido);
        model.addAttribute("pronombres", pronombres);
        model.addAttribute("whatsapp", whatsapp);
        model.addAttribute("opcionesPronombres", PerfilService.PRONOMBRES);
        model.addAttribute("errores", errores);
    }

    /**
     * Envía a la vista los datos del menú latereal y del encabezado.
     *
     * @param model   Modelo para enviar los datos a la vista
     * @param usuario Usuario del cual se cargan los datos
     */
    private void cargarDatosPagina(Model model, Usuario usuario) {
        model.addAttribute("paginaActiva", "perfil");
        model.addAttribute("usuarioNombre", usuario.getNombreUsuario());
        model.addAttribute("usuarioRol", "Estudiante");
    }
}
