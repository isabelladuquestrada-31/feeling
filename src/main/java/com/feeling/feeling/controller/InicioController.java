package com.feeling.feeling.controller;

import com.feeling.feeling.model.Usuario;
import com.feeling.feeling.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class InicioController {
    @GetMapping("/")
    public String inicio() {
        return "paginas/index";
    }
}