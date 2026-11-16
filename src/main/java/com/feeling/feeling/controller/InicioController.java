
package com.feeling.feeling.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controlador encargado de gestionar las vistas públicas
 * de inicio y registro de la plataforma Feeling.
 *
 * @author Yulipaola Domínguez
 * @version 1.1.0
 */
@Controller
public class InicioController {

    /**
     * Muestra la página principal de Feeling.
     *
     * @return nombre de la vista de inicio.
     */
    @GetMapping("/")
    public String inicio() {
        return "paginas/index";
    }

    /**
     * Muestra el formulario de registro de nuevos estudiantes.
     *
     * @return nombre de la vista de registro.
     */
    @GetMapping("/registro")
    public String mostrarRegistro() {
        return "paginas/signup";
    }
}
