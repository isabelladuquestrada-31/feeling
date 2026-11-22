package com.feeling.feeling.config;

import com.feeling.feeling.model.Administrador;
import com.feeling.feeling.model.Usuario;
import com.feeling.feeling.service.ServicioAutenticacion;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.stereotype.Component;

/**
 * Protege las rutas del administrador.
 * Solo deja pasar a usuarios con sesión iniciada y rol de administrador;
 * a los demás los redirige.
 *
 * @author Isabella Duque
 * @version 1.0
 */

@Component
public class AdminInterceptor implements HandlerInterceptor {

    /**
     * Revisa, antes de cada petición a las rutas del administrador,
     * que quien la hace sea un administrador con sesión iniciada.
     *
     * @param request  petición que llega
     * @param response respuesta, usada para redirigir si no tiene permiso
     * @param handler  controlador que atendería la petición
     * @return si es administrador y puede continuar; si se redirigió
     * @throws Exception si falla la redirección
     */
    @Override
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
