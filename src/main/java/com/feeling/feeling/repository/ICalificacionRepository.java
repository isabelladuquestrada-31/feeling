package com.feeling.feeling.repository;

import com.feeling.feeling.model.Calificacion;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Guarda y consulta las calificaciones de los acompañamientos.
 * Para saber si una solicitud ya fue calificada se usa existsById.
 *
 * @author Yeymi Cuéllar
 */
public interface ICalificacionRepository extends JpaRepository<Calificacion, Integer> {
}