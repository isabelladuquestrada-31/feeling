package com.feeling.feeling.controller;

import com.feeling.feeling.model.Administrador;
import com.feeling.feeling.model.Usuario;
import com.feeling.feeling.service.IServicioAutenticacion;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

/**
 * Maneja el inicio y el cierre de sesión de los usuarios.
 * Redirige a cada usuario a su página según su rol.
 *
 * @author Isabella Duque
 * @version 1.0
 */
@Controller
public class LoginController {

    private final IServicioAutenticacion servicioAutenticacion;

    /**
     * Crea el controlador con el servicio de autenticación.
     *
     * @param servicioAutenticacion servicio que verifica los datos del usuario
     */
    public LoginController(IServicioAutenticacion servicioAutenticacion) {
        this.servicioAutenticacion = servicioAutenticacion;
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
     * @param contrasena contraseña escrita en el formulario
     * @param sesion sesión donde se guarda el usuario que inició sesión
     * @param model modelo para enviar el mensaje de error a la vista
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

        if (usuario instanceof Administrador) {
            return "redirect:/admin";
        }
        return "redirect:/";
    }

    /**
     * Cierra la sesión del usuario.
     *
     * @param sesion sesión que se va a cerrar
     * @return la redirección al login
     */
    @GetMapping("/logout")
    public String cerrarSesion(HttpSession sesion) {
        sesion.invalidate();
        return "redirect:/login";
    }

}