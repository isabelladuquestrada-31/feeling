package com.feeling.feeling.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Maneja las páginas del administrador.
 * Solo pueden entrar usuarios con el rol de administrador
 *
 * @author Isabella Duque
 * @version 1.0
 */

@Controller
public class AdminController {
    /**
     * Muestra la página principal del administrador
     * @return
     */
    @GetMapping("/admin")
    public String inicioAdmin(){
        return "paginas/admin";
    }
}
