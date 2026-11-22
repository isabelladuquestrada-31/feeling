package com.feeling.feeling.controller;

import com.feeling.feeling.model.Administrador;
import com.feeling.feeling.model.Usuario;
import com.feeling.feeling.service.IServicioAutenticacion;
import com.feeling.feeling.service.PreferenciasService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

/**
 * Maneja el inicio y el cierre de sesión de los usuarios.
 * Redirige a cada usuario a su página según su rol.
 *
 * @author Isabella Duque
 * @version 1.1
 */
@Controller
public class LoginController {

    //=====DOCUMENTAR ESTEEEEE======= DORA
    private final IServicioAutenticacion servicioAutenticacion;

    /**
     * Servicio para cargar las preferencias de accesibilidad al iniciar sesión (RF-04)
     */
    private final PreferenciasService preferenciasService;


    /**
     * Crea el controlador con el servicio de autenticación.
     *
     * @param servicioAutenticacion servicio que verifica los datos del usuario
     */
    public LoginController(IServicioAutenticacion servicioAutenticacion, PreferenciasService preferenciasService) {
        this.servicioAutenticacion = servicioAutenticacion;
        this.preferenciasService = preferenciasService;
    }

    /**
     * Muestra el formulario de inicio de sesión.
     *
     * @return la vista del login
     */
    @GetMapping("/login")
    public String mostrarLogin() {
        return "paginas/login";
    }

    /**
     * Verifica los datos del formulario y redirige al usuario según su rol.
     *
     * @param nombreUsuario nombre de usuario escrito en el formulario
     * @param contrasena    contraseña escrita en el formulario
     * @param sesion        sesión donde se guarda el usuario que inició sesión
     * @param model         modelo para enviar el mensaje de error a la vista
     * @return la redirección a la página del rol, o el login con un error
     */
    @PostMapping("/login")
    public String iniciarSesion(@RequestParam String nombreUsuario,
                                @RequestParam String contrasena,
                                HttpSession sesion,
                                Model model) {
        Optional<Usuario> resultado = servicioAutenticacion.iniciarSesion(nombreUsuario, contrasena);

        if (resultado.isEmpty()) {
            model.addAttribute("error", "Usuario o contraseña incorrectos");
            return "paginas/login";
        }

        Usuario usuario = resultado.get();
        sesion.setAttribute("usuario", usuario);
        sesion.setAttribute(PreferenciasService.ATRIBUTO_SESION, preferenciasService.obtener(usuario.getId()));

        if (usuario instanceof Administrador) {
            return "redirect:/admin";
        }
        return "redirect:/inicio";
    }

    /**
     * Cierra la sesión del usuario. Acepta GET y POST porque el menú
     * del encabezado envía un formulario POST.
     *
     * @param sesion sesión que se va a cerrar
     * @return la redirección al login
     */
    @RequestMapping(value = "/logout", method = {RequestMethod.GET, RequestMethod.POST})
    public String cerrarSesion(HttpSession sesion) {
        sesion.invalidate();
        return "redirect:/login";
    }
}
