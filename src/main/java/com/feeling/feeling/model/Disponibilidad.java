package com.feeling.feeling.model;

import jakarta.persistence.*;

import java.time.LocalTime;

/**
 * Franja horario en la que un voluntario puede atender acompañamientos
 *
 * @author Isabella Duque
 * @version 1.0
 */
@Entity
@Table(name = "disponibilidad")
public class Disponibilidad {

    /**
     * Identificador de la franja, generado por MySQL
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * Identificador del voluntario
     */
    @Column(name = "voluntario_id", nullable = false)
    private Integer voluntarioId;

    /**
     * Dia de la semana de la franja
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "dia", length = 10, nullable = false)
    private DiaSemana dia;

    /**
     * Hora en la empieza la franja
     */
    @Column(name = "hora_inicio", nullable = false)
    private LocalTime horaInicio;

    /**
     * Hora en la que termina la franja
     */
    @Column(name = "hora_fin", nullable = false)
    private LocalTime horaFin;

    /**
     * Constructor vacio
     */
    public Disponibilidad() {
    }

    /**
     * Crea una franja de disponibilidad
     *
     * @param voluntarioId Identificador del voluntario
     * @param dia Día de la semana
     * @param horaInicio hora de inicio
     * @param horaFin hora de fin
     */
    public Disponibilidad(Integer voluntarioId, DiaSemana dia, LocalTime horaInicio, LocalTime horaFin) {
        this.voluntarioId = voluntarioId;
        this.dia = dia;
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
    }

    /**
     * Indica si esta franja se cruza con otro horario del mismo dia
     * Dos franjas se cruzan si una empieza antes que la otra termine
     *
     * @param inicio hora de inicio del otro horario
     * @param fin hora de fin del otro horario
     * @return true si los horarios se cruzan
     */
    public boolean seCruzan(LocalTime inicio, LocalTime fin) {
        return horaInicio.isBefore(fin) && inicio.isBefore(horaFin);
    }

    /**
     *
     * @return identificador de la franja
     */
    public Integer getId() {
        return id;
    }

    /**
     * @param id identificador de la franja
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * @return identificador del voluntario
     */
    public Integer getVoluntarioId() {
        return voluntarioId;
    }

    /**
     * @param voluntarioId identificador del voluntario
     */
    public void setVoluntarioId(Integer voluntarioId) {
        this.voluntarioId = voluntarioId;
    }

    /**
     * @return día de la semana
     */
    public DiaSemana getDia() {
        return dia;
    }

    /**
     * @param dia día de la semana
     */
    public void setDia(DiaSemana dia) {
        this.dia = dia;
    }

    /**
     * @return hora de inicio
     */
    public LocalTime getHoraInicio() {
        return horaInicio;
    }

    /**
     * @param horaInicio hora de inicio
     */
    public void setHoraInicio(LocalTime horaInicio) {
        this.horaInicio = horaInicio;
    }

    /**
     * @return hora de fin
     */
    public LocalTime getHoraFin() {
        return horaFin;
    }

    /**
     * @param horaFin hora de fin
     */
    public void setHoraFin(LocalTime horaFin) {
        this.horaFin = horaFin;
    }
}
