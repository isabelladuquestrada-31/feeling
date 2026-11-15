package com.feeling.feeling.controller;

import com.feeling.feeling.model.Estudiante;
import com.feeling.feeling.service.IServicioAutenticacion;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Maneja el registro de estudiantes nuevos.
 *
 * @author Isabella Duque
 * @version 1.0
 */
@Controller
public class RegistroController {

    private final IServicioAutenticacion servicioAutenticacion;

    /**
     * Crea el controlador con el servicio de autenticación.
     *
     * @param servicioAutenticacion servicio que registra a los estudiantes
     */
    public RegistroController(IServicioAutenticacion servicioAutenticacion) {
        this.servicioAutenticacion = servicioAutenticacion;
    }

    /**
     * Muestra el formulario de registro.
     *
     * @return la vista del registro
     */
    @GetMapping("/registro")
    public String mostrarRegistro() {
        return "paginas/registro";
    }

    /**
     * Registra al estudiante y lo lleva a su página de inicio con la sesión iniciada.
     * Si algo falla, vuelve al formulario con el error y los datos que ya había escrito.
     *
     * @param nombreUsuario     nombre de usuario escrito en el formulario
     * @param contrasena        contraseña escrita en el formulario
     * @param programaAcademico programa académico escrito en el formulario
     * @param whatsapp          número de WhatsApp escrito en el formulario
     * @param aceptaAviso       indica si marcó la casilla del aviso de tratamiento de datos
     * @param sesion            sesión donde se guarda el estudiante registrado
     * @param model             modelo para enviar el error y los datos a la vista
     * @return la redirección al inicio, o el formulario con el error
     */
    @PostMapping("/registro")
    public String registrar(@RequestParam String nombreUsuario,
                            @RequestParam String contrasena,
                            @RequestParam String programaAcademico,
                            @RequestParam String whatsapp,
                            @RequestParam(required = false) boolean aceptaAviso,
                            HttpSession sesion,
                            Model model) {

        if (!aceptaAviso) {
            return mostrarError("Debes aceptar el aviso de tratamiento de datos",
                    nombreUsuario, programaAcademico, whatsapp, model);
        }

        try {
            Estudiante estudiante = servicioAutenticacion.registrar(
                    nombreUsuario, contrasena, programaAcademico, whatsapp);
            sesion.setAttribute("usuario", estudiante);
            return "redirect:/";
        } catch (IllegalArgumentException e) {
            return mostrarError(e.getMessage(), nombreUsuario, programaAcademico, whatsapp, model);
        }
    }

    /**
     * Vuelve a mostrar el formulario con el mensaje de error y los datos escritos.
     * La contraseña no se devuelve por seguridad.
     *
     * @param error             mensaje que se muestra al estudiante
     * @param nombreUsuario     nombre de usuario que había escrito
     * @param programaAcademico programa académico que había escrito
     * @param whatsapp          número de WhatsApp que había escrito
     * @param model             modelo para enviar los datos a la vista
     * @return la vista del registro
     */
    private String mostrarError(String error, String nombreUsuario,
                                String programaAcademico, String whatsapp, Model model) {
        model.addAttribute("error", error);
        model.addAttribute("nombreUsuario", nombreUsuario);
        model.addAttribute("programaAcademico", programaAcademico);
        model.addAttribute("whatsapp", whatsapp);
        return "paginas/registro";
    }
}