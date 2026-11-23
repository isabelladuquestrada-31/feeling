package com.feeling.feeling.repository;

import com.feeling.feeling.model.Suspension;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Acceso a datos de las suspensiones de voluntarios
 *
 * @author Isabella Duque
 * @version 1.0
 */
public interface SuspensionRepository extends JpaRepository<Suspension, Integer> {

    /**
     * Busca la suspensión más reciente de un voluntario.
     *
     * @param voluntarioId id del voluntario
     * @return la última suspensión, si tiene
     */
    Optional<Suspension> findFirstByVoluntarioIdOrderByIdDesc(Integer voluntarioId);
}
