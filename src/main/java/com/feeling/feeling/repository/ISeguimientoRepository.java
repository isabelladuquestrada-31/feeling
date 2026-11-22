package com.feeling.feeling.repository;

import com.feeling.feeling.model.Seguimiento;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Guarda y consulta los seguimientos de los acompañamientos.
 *
 * @author Yeymi Cuellar
 */
public interface ISeguimientoRepository extends JpaRepository<Seguimiento, Integer> {
}