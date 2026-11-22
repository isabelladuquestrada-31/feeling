
package com.feeling.feeling.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controlador encargado de gestionar la vista pública
 * de inicio de la plataforma Feeling.
 * El formulario de registro lo maneja {@link RegistroController}.
 *
 * @author Yulipaola Domínguez
 * @version 1.2.0
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

}
