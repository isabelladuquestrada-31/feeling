package com.feeling.feeling.service;

import com.feeling.feeling.model.DiaSemana;
import com.feeling.feeling.model.Disponibilidad;
import com.feeling.feeling.repository.DisponibilidadRepository;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * El voluntario agrega, edita y elimina sus franjas de disponibilidad
 * Antes de guardar valida que la hora de fin sea posterior a la de inicio y que la franja no se cruce con otra del mismo
 * voluntario y en el mismo dia
 *
 * @author Isabella Duque
 * @version 1.0
 */
@Service
public class DisponibilidadService {

    /**
     * Repositorio de las franjas de disponibilidad.
     */
    private final DisponibilidadRepository disponibilidadRepository;

    /**
     * Crea el servicio con el repositorio que entrega Spring.
     *
     * @param disponibilidadRepository repositorio de disponibilidad
     */
    public DisponibilidadService(DisponibilidadRepository disponibilidadRepository) {
        this.disponibilidadRepository = disponibilidadRepository;
    }

    /**
     * Lista las franjas de un voluntario ordenadas por día (lunes a domingo) y hora de inicio.
     *
     * @param voluntarioId identificador del voluntario
     * @return franjas ordenadas
     */
    public List<Disponibilidad> listar(Integer voluntarioId) {
        return disponibilidadRepository.findByVoluntarioId(voluntarioId).stream()
                .sorted(Comparator.comparing(Disponibilidad::getDia)
                        .thenComparing(Disponibilidad::getHoraInicio))
                .toList();
    }

    /**
     * Agrega una franja nueva al voluntario.
     *
     * @param voluntarioId identificador del voluntario
     * @param dia          día de la semana
     * @param horaInicio   hora de inicio
     * @param horaFin      hora de fin
     * @return la franja guardada
     * @throws IllegalArgumentException si los datos no son válidos o la franja se cruza con otra
     */
    public Disponibilidad agregar(Integer voluntarioId, DiaSemana dia, LocalTime horaInicio, LocalTime horaFin) {
        validar(voluntarioId, dia, horaInicio, horaFin, null);
        return disponibilidadRepository.save(new Disponibilidad(voluntarioId, dia, horaInicio, horaFin));
    }

    /**
     * Edita una franja existente del voluntario.
     *
     * @param id identificador de la franja
     * @param voluntarioId identificador del voluntario que la edita
     * @param dia nuevo día
     * @param horaInicio nueva hora de inicio
     * @param horaFin nueva hora de fin
     * @return la franja actualizada
     * @throws IllegalArgumentException si la franja no existe, no es del voluntario, los datos no son válidos o se cruza con otra
     */
    public Disponibilidad editar(Integer id, Integer voluntarioId, DiaSemana dia, LocalTime horaInicio, LocalTime horaFin) {
        Disponibilidad franja = buscarDelVoluntario(id, voluntarioId);
        validar(voluntarioId, dia, horaInicio, horaFin, id);
        franja.setDia(dia);
        franja.setHoraInicio(horaInicio);
        franja.setHoraFin(horaFin);
        return disponibilidadRepository.save(franja);
    }

    /**
     * Elimina una franja del voluntario.
     *
     * @param id identificador de la franja
     * @param voluntarioId identificador del voluntario que la elimina
     * @throws IllegalArgumentException si la franja no existe o no es del voluntario
     */
    public void eliminar(Integer id, Integer voluntarioId) {
        Disponibilidad franja = buscarDelVoluntario(id, voluntarioId);
        disponibilidadRepository.delete(franja);
    }

    /**
     * Busca una franja y revisa que pertenezca al voluntario, para que nadie edite o borre la disponibilidad de otra persona.
     *
     * @param id identificador de la franja
     * @param voluntarioId identificador del voluntario
     * @return la franja encontrada
     * @throws IllegalArgumentException si no existe o es de otro voluntario
     */
    private Disponibilidad buscarDelVoluntario(Integer id, Integer voluntarioId) {
        Disponibilidad franja = disponibilidadRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("La franja de disponibilidad no existe."));
        if (!Objects.equals(franja.getVoluntarioId(), voluntarioId)) {
            throw new IllegalArgumentException("No puedes modificar la disponibilidad de otro voluntario.");
        }
        return franja;
    }

    /**
     * Valida los datos de una franja.
     *
     * @param voluntarioId identificador del voluntario
     * @param dia día de la semana
     * @param horaInicio hora de inicio
     * @param horaFin hora de fin
     * @param idExcluido id de la franja que se está editando
     * @throws IllegalArgumentException si algún dato no es válido
     */
    private void validar(Integer voluntarioId, DiaSemana dia, LocalTime horaInicio,
                         LocalTime horaFin, Integer idExcluido) {
        if (dia == null || horaInicio == null || horaFin == null) {
            throw new IllegalArgumentException("Indica el día, la hora de inicio y la hora de fin.");
        }
        if (!horaFin.isAfter(horaInicio)) {
            throw new IllegalArgumentException("La hora de fin debe ser posterior a la hora de inicio.");
        }
        boolean hayCruce = disponibilidadRepository.findByVoluntarioIdAndDia(voluntarioId, dia).stream()
                .filter(otra -> !Objects.equals(otra.getId(), idExcluido))
                .anyMatch(otra -> otra.getHoraInicio().isBefore(horaFin)
                        && horaInicio.isBefore(otra.getHoraFin()));
        if (hayCruce) {
            throw new IllegalArgumentException("La franja se cruza con otra que ya registraste ese día.");
        }
    }
}