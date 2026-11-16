package com.feeling.feeling.repository;

import com.feeling.feeling.model.Voluntario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio para acceder a los perfiles de voluntario en la base de datos.
 *
 * <p>La llave primaria es el id del estudiante dueño del perfil,
 * por eso el tipo del id es {@link Integer}.</p>
 *
 * @author Isabella Duque
 * @version 1.0
 */
@Repository
public interface IVoluntarioRepository extends JpaRepository<Voluntario, Integer> {
}
