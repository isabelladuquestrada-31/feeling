package com.feeling.feeling.repository;

import com.feeling.feeling.model.PreferenciasAccesibilidad;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio donse se guarda y consulta las preferencias de accesibilidad.
 *
 * @author Isablla Duque
 * @ version 1.0
 */


public interface IPreferenciaAccesibilidadRepository extends JpaRepository<PreferenciasAccesibilidad, Integer> {
}
