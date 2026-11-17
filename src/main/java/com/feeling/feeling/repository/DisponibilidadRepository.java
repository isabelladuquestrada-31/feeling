package com.feeling.feeling.repository;

import com.feeling.feeling.model.DiaSemana;
import com.feeling.feeling.model.Disponibilidad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Acceso a la tabla disponibilidad
 * Crea las consultas a partir del nombre de cada método
 *
 * @author Isabella Duque
 * @version 1.0
 */
@Repository
public interface DisponibilidadRepository extends JpaRepository<Disponibilidad,Integer> {

    /**
     * Busca todas las franjas de un voluntario
     *
     * @param voluntarioId Identificador del volunatrio
     * @return franjas del voluntario
     */
    List<Disponibilidad> findByVoluntarioId(Integer voluntarioId);


    /**
     * Busca las franjas de un voluntario en un día, para revisar si hay cruces.
     *
     * @param voluntarioId identificador del voluntario
     * @param dia día de la semana
     * @return franjas del voluntario en ese día
     */
    List<Disponibilidad> findByVoluntarioIdAndDia(Integer voluntarioId, DiaSemana dia);


}
