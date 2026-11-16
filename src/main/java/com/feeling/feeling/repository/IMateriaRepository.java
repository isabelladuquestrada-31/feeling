package com.feeling.feeling.repository;

import com.feeling.feeling.model.Materia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio para acceder a las materias en la base de datos.
 *
 * @author Isabella Duque
 * @version1.0
 */
@Repository
public interface IMateriaRepository extends JpaRepository<Materia, Integer> {
}
