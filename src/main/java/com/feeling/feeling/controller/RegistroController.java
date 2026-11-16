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
 * @version 1.2
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
        return "paginas/signup";
    }

    /**
     * Registra al estudiante y lo lleva a su página de inicio con la sesión iniciada.
     * Si algo falla, vuelve al formulario con el error y los datos que ya había escrito.
     *
     * @param nombreUsuario       nombre de usuario escrito en el formulario
     * @param contrasena          contraseña escrita en el formulario
     * @param confirmarContrasena repetición de la contraseña
     * @param programaAcademico   programa académico escrito en el formulario
     * @param whatsapp            número de WhatsApp escrito en el formulario
     * @param aceptaAviso         indica si marcó la casilla del tratamiento de datos
     * @param sesion              sesión donde se guarda el estudiante registrado
     * @param model               modelo para enviar el error y los datos a la vista
     * @return la redirección al inicio, o el formulario con el error
     */
    @PostMapping("/registro")
    public String registrar(@RequestParam String nombreUsuario,
                            @RequestParam String contrasena,
                            @RequestParam String confirmarContrasena,
                            @RequestParam String programaAcademico,
                            @RequestParam("numeroWhatsApp") String whatsapp,
                            @RequestParam(name = "aceptaTratamientoDatos", required = false) boolean aceptaAviso,
                            HttpSession sesion,
                            Model model) {

        if (!contrasena.equals(confirmarContrasena)) {
            return mostrarError("Las contraseñas no coinciden",
                    nombreUsuario, programaAcademico, whatsapp, aceptaAviso, model);
        }

        if (!aceptaAviso) {
            return mostrarError("Debes aceptar el aviso de tratamiento de datos",
                    nombreUsuario, programaAcademico, whatsapp, false, model);
        }

        try {
            Estudiante estudiante = servicioAutenticacion.registrar(
                    nombreUsuario, contrasena, programaAcademico, limpiarWhatsapp(whatsapp));
            sesion.setAttribute("usuario", estudiante);
            return "redirect:/";
        } catch (IllegalArgumentException e) {
            return mostrarError(e.getMessage(),
                    nombreUsuario, programaAcademico, whatsapp, aceptaAviso, model);
        }
    }

    /**
     * Deja el número de WhatsApp como lo espera el servicio: solo los 10 dígitos del celular.
     * El formulario lo envía con el indicativo, por ejemplo "+57 3001234567".
     *
     * @param numero número escrito en el formulario
     * @return el número sin espacios, sin símbolos y sin el indicativo 57
     */
    private String limpiarWhatsapp(String numero) {
        String digitos = numero.replaceAll("\\D", "");
        if (digitos.length() == 12 && digitos.startsWith("57")) {
            return digitos.substring(2);
        }
        return digitos;
    }

    /**
     * Vuelve a mostrar el formulario con el mensaje de error y los datos escritos.
     * La contraseña no se devuelve por seguridad.
     *
     * @param error             mensaje que se muestra al estudiante
     * @param nombreUsuario     nombre de usuario que había escrito
     * @param programaAcademico programa académico que había escrito
     * @param whatsapp          número de WhatsApp que había escrito
     * @param aceptaAviso       si ya había marcado la casilla del tratamiento de datos
     * @param model             modelo para enviar los datos a la vista
     * @return la vista del registro
     */
    private String mostrarError(String error, String nombreUsuario, String programaAcademico,
                                String whatsapp, boolean aceptaAviso, Model model) {
        model.addAttribute("error", error);
        model.addAttribute("nombreUsuario", nombreUsuario);
        model.addAttribute("programaAcademico", programaAcademico);
        model.addAttribute("numeroWhatsApp", whatsapp);
        model.addAttribute("aceptaTratamientoDatos", aceptaAviso);
        return "paginas/signup";
    }
}
