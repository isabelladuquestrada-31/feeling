package com.feeling.feeling.service;
import com.feeling.feeling.model.Estudiante;
import com.feeling.feeling.model.Usuario;
import java.util.Optional;

/**
 * Define las operaciones para autenticar a los usuarios de la plataforma
 *
 * @author Isabella Duque
 * @version 1.0
 */
public interface IServicioAutenticacion {

    /**
     * Verifica el nombre y la contraseña del usuario
     *
     * @param nombreUsuario Nombre con el que la persona inicia sesión
     * @param contrasena    contraseña de la persona
     * @return el ususario, si los datos eta correctos y esta activo
     */
    Optional<Usuario> iniciarSesion(String nombreUsuario, String contrasena);

    /**
     * Registra un estudiante nuevo con perfil y preferencias por defecto
     *
     * @param nombreUsuario Nombre con el que la persona se registra
     * @param contrasena    contraseña con la que la persona se registra
     * @return el estudiante registrado
     * @throws IllegalArgumentException si faltan datos o el nombre de usuario ya existe
     */
    Estudiante registrar(String nombreUsuario, String contrasena);
}

