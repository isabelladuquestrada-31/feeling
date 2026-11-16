package com.feeling.feeling.repository;

import com.feeling.feeling.model.EstadoVoluntario;
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

    /**
     * Cuenta los voluntarios que están en el estado indicado.
     *
     * @param estado estado que se quiere contar
     * @return cantidad de voluntarios en ese estado
     */
    long countByEstado(EstadoVoluntario estado);
}
