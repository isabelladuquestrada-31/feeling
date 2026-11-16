package com.feeling.feeling.service;

import com.feeling.feeling.model.Estudiante;
import com.feeling.feeling.model.Perfil;
import com.feeling.feeling.model.PreferenciasAccesibilidad;
import com.feeling.feeling.model.Usuario;
import com.feeling.feeling.repository.IPerfilRepository;
import com.feeling.feeling.repository.IPreferenciaAccesibilidadRepository;
import com.feeling.feeling.repository.UsuarioRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Maneja el inicio de sesión y el registro de los usuarios.
 *
 * @author Isabella Duque
 * @version 1.0
 */
@Service
public class ServicioAutenticacion implements IServicioAutenticacion {

    private final UsuarioRepository usuarioRepository;
    private final IPerfilRepository perfilRepository;
    private final IPreferenciaAccesibilidadRepository preferenciasRepository;
    private final BCryptPasswordEncoder encriptador = new BCryptPasswordEncoder();

    /**
     * Crea el servicio con los repositorios que necesita.
     *
     * @param usuarioRepository      repositorio de usuarios
     * @param perfilRepository       repositorio de perfiles
     * @param preferenciasRepository repositorio de preferencias de accesibilidad
     */
    public ServicioAutenticacion(UsuarioRepository usuarioRepository,
                                 IPerfilRepository perfilRepository,
                                 IPreferenciaAccesibilidadRepository preferenciasRepository) {
        this.usuarioRepository = usuarioRepository;
        this.perfilRepository = perfilRepository;
        this.preferenciasRepository = preferenciasRepository;
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
     * Verifica que los datos no estén vacíos, que el WhatsApp sea válido y que
     * el nombre de usuario no exista, y guarda la contraseña cifrada con BCrypt.
     * Si algo falla, no se guarda nada.
     *
     * @param nombreUsuario     nombre con el que la persona se registra
     * @param contrasena        contraseña con la que la persona se registra
     * @param programaAcademico programa académico que cursa
     * @param whatsapp          número de WhatsApp de 10 dígitos
     * @return el estudiante registrado
     * @throws IllegalArgumentException si faltan datos, el WhatsApp no es válido o el nombre de usuario ya existe
     */
    @Override
    @Transactional
    public Estudiante registrar(String nombreUsuario, String contrasena,
                                String programaAcademico, String whatsapp) {

        if (estaVacio(nombreUsuario) || estaVacio(contrasena)
                || estaVacio(programaAcademico) || estaVacio(whatsapp)) {
            throw new IllegalArgumentException("Se deben llenar todos los campos");
        }

        if (!whatsapp.matches("3\\d{9}")) {
            throw new IllegalArgumentException("El número de WhatsApp no es válido");
        }

        if (usuarioRepository.findByNombreUsuario(nombreUsuario).isPresent()) {
            throw new IllegalArgumentException("El nombre de usuario ya existe");
        }

        Estudiante estudiante = new Estudiante();
        estudiante.setNombreUsuario(nombreUsuario);
        estudiante.setContrasenaHash(encriptador.encode(contrasena));
        estudiante.setFechaRegistro(LocalDateTime.now());
        estudiante.setProgramaAcademico(programaAcademico);
        Estudiante guardado = usuarioRepository.save(estudiante);

        Perfil perfil = new Perfil();
        perfil.setUsuario(guardado);
        perfil.setWhatsapp(whatsapp);
        perfilRepository.save(perfil);

        PreferenciasAccesibilidad preferencias = new PreferenciasAccesibilidad();
        preferencias.setUsuario(guardado);
        preferenciasRepository.save(preferencias);

        return guardado;
    }

    /**
     * Indica si un texto es nulo o solo tiene espacios.
     *
     * @param texto texto que se revisa
     * @return true si está vacío, false si tiene contenido
     */
    private boolean estaVacio(String texto) {
        return texto == null || texto.isBlank();
    }
}