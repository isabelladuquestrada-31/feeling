package com.feeling.feeling.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configura los interceptores de la aplicaión.
 *
 * @author Isabella Duque
 * @version 1.0
 */
@Configuration
public class ConfiguracionWeb implements WebMvcConfigurer {

    /**
     * Registra el interceptor que protege las rutas del administrador.
     *
     * @param registry registro donde se agregan los interceptores
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new AdminInterceptor()).addPathPatterns("/admin/**");
    }
}
