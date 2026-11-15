package com.feeling.feeling.repository;

import com.feeling.feeling.model.EstadoSolicitud;
import com.feeling.feeling.model.Solicitud;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repositorio para acceder a las solicitudes de acompañamiento guardadas en la base de datos.
 * Hereda de JpaRepository los métodos básicos (guardar, buscar por id, listar y eliminar),
 * y agrega las consultas que necesita Feeling para los próximos acompañamientos y el historial.
 *
 * @author Daniel Blandón
 */
public interface SolicitudRepository extends JpaRepository<Solicitud, Integer> {

    /**
     * Busca los próximos acompañamientos de un estudiante en un estado dado,
     * ordenados del más cercano al más lejano.
     * Se usa en RF-13 con el estado ACEPTADA.
     *
     * @param estudianteId id del estudiante
     * @param estado       estado de las solicitudes que se quieren buscar
     * @param desde        fecha y hora a partir de la cual se buscan (normalmente, el momento actual)
     * @return lista de solicitudes programadas después de la fecha indiada
     */
    List<Solicitud> findByEstudianteIdAndEstadoAndFechaProgramadaAfterOrderByFechaProgramadaAsc(
            Integer estudianteId, EstadoSolicitud estado, LocalDateTime desde);

    /**
     * Busca los proximos acompañamientos de un voluntario en un estado dado,
     * ordenados del más cercano al más lejano.
     * Se usa en RF-13 con el estado ACEPTADA.
     *
     * @param voluntarioId id del voluntario
     * @param estado       estado de las solicitudes que se quieren buscar
     * @param desde        fecha y hora a partir de la cual se buscan (normalmente, el momento actual)
     * @return lista de solicitudes programadas después de la fecha indicada
     */

    List<Solicitud> findByVoluntarioIdAndEstadoAndFechaProgramadaAfterOrderByFechaProgramadaAsc(
            Integer voluntarioId, EstadoSolicitud estado, LocalDateTime desde);

    /**
     * Busca todas las solicitudes que ha hecho un estudiante, sin importar su estado,
     * de la más reciente a la más antigua. Se usa en RF-13 para el historial.
     *
     * @param estudianteId id del estudiante
     * @return lista con el historial de solicitudes del estudiante
     */
    List<Solicitud> findByEstudianteIdOrderByFechaCreacionDesc(Integer estudianteId);

    /**
     * RF-10: busca las solicitudes de un voluntario en un estado dado,
     * ordenadas de la más cercana a la más lejana.
     *
     * @param voluntarioId id del voluntario
     * @param estado       estado de las solicitudes que se quieren buscar
     * @return lista de solicitudes del voluntario
     */
    List<Solicitud> findByVoluntarioIdAndEstadoOrderByFechaProgramadaAsc(Integer voluntarioId, EstadoSolicitud estado);
}
