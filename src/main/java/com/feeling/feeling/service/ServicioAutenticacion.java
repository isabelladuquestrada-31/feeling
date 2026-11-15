package com.feeling.feeling.service;

import com.feeling.feeling.model.Estudiante;
import com.feeling.feeling.model.Usuario;
import com.feeling.feeling.repository.UsuarioRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
@Service
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

    /**
     * Registra un estudiante nuevo con perfil y preferencias por defecto.
     * Verifica que los datos no estén vacíos y que el nombre de usuario no exista,
     * y guarda la contraseña cifrada con BCrypt.
     *
     * @param nombreUsuario nombre con el que la persona se registra
     * @param contrasena    contraseña con la que la persona se registra
     * @return el estudiante registrado
     * @throws IllegalArgumentException si faltan datos o el nombre de usuario ya existe
     */

    @Override
    public Estudiante registrar(String nombreUsuario, String contrasena) {

        if (nombreUsuario == null || nombreUsuario.isBlank()
                || contrasena == null || contrasena.isBlank()) {
            throw new IllegalArgumentException("Se debe llenar el nombre de usuario y la contraseña");
        }

        if (usuarioRepository.findByNombreUsuario(nombreUsuario).isPresent()) {
            throw new IllegalArgumentException("El nombre de usuario ya existe");
        }

        Estudiante estudiante = new Estudiante();
        estudiante.setNombreUsuario(nombreUsuario);
        estudiante.setContrasenaHash(encriptador.encode(contrasena));
        estudiante.setFechaRegistro(LocalDateTime.now());


        return usuarioRepository.save(estudiante);
    }
}

