package com.feeling.feeling.repository;

import com.feeling.feeling.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.Optional;

/**
 * Permite consultar y guardar usuarios en la base de datos.
 *
 * @author Isabella Duque
 * @version 1.0
 */
public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {
    /**
     * Busca un usuario por el nombre con el que inicia sesión.
     *
     * @param nombreUsuario nombre de usuario que se va a buscar
     * @return el usuario que se encontró o vacío si no existe
     */

    Optional<Usuario> findByNombreUsuario(String nombreUsuario);

    /**
     * Cuenta los estudiantes registrados (sin contar administradores).
     *
     * @return cantidad de estudiantes
     */
    @Query("SELECT COUNT(e) FROM Estudiante e")
    long contarEstudiantes();
}
