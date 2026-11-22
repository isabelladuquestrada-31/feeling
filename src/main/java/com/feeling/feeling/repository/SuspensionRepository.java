package com.feeling.feeling.repository;

import com.feeling.feeling.model.Suspension;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Acceso a datos de las suspensiones de voluntarios
 *
 * @author Isabella Duque
 * @version 1.0
 */
public interface SuspensionRepository extends JpaRepository<Suspension, Integer> {
}
