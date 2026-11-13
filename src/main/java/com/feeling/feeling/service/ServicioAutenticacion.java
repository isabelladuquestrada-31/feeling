package com.feeling.feeling.service;

import com.feeling.feeling.model.Usuario;
import com.feeling.feeling.repository.UsuarioRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

public class ServicioAutenticacion implements IServicioAutenticacion {

    private final UsuarioRepository usuarioRepository;
    private final BCryptPasswordEncoder encriptador = new BCryptPasswordEncoder();

    /**
     * Crea el servicio con el repositorio de usuarios
     *
     * @param usuarioRepository repositorio para buscar en la base de datos
     */
    public ServicioAutenticacion(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public Optional<Usuario> iniciarSesion(String nombreUsuario, String contrasena) {
        Optional<Usuario> resultado = usuarioRepository.findByNombreUsuario(nombreUsuario);

        if (resultado.isEmpty()) {
            return Optional.empty();
        }
        Usuario usuario = resultado.get();

        if (!usuario.isActivo()) {
            return Optional.empty();
        }

        if (!encriptador.matches(contrasena, usuario.getContrasenaHash())) {
            return Optional.empty();
        }

        return Optional.of(usuario);
    }
}

