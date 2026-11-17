package com.feeling.feeling.config;

import com.feeling.feeling.model.Administrador;
import com.feeling.feeling.model.Usuario;
import com.feeling.feeling.service.ServicioAutenticacion;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

public class AdminInterceptor implements HandlerInterceptor {

    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        Object usuario = request.getSession().getAttribute("usuario");

        if(usuario instanceof Administrador){
            return true;
        }

        if (usuario == null) {
            response.sendRedirect("/login");
        } else {
            // Un estudiante no puede entrar: vuelve a su inicio con el aviso de acceso denegado.
            response.sendRedirect("/inicio?acceso=denegado");
        }
        return false;
    }
}
