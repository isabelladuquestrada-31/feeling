package com.feeling.feeling.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * Seguimiento que registra el voluntario al terminar un acompañamiento.
 * Corresponde a la tabla "seguimiento". Cada solicitud tiene como máximo uno,
 * por eso su id es el mismo de la solicitud.
 *
 * @author Yeymi Cuellar
 */
@Entity
@Table(name = "seguimiento")
public class Seguimiento {

    /**
     * Id de la solicitud a la que pertenece. No se genera: lo asigna el servicio.
     */
    @Id
    @Column(name = "solicitud_id")
    private Integer solicitudId;

    /**
     * Notas del voluntario sobre cómo fue el acompañamiento.
     */
    @Column(columnDefinition = "TEXT")
    private String notas;

    /**
     * Duración real del acompañamiento, en minutos.
     */
    @Column(name = "duracion_minutos", nullable = false)
    private Integer duracionMinutos;

    /**
     * Fecha y hora en que se registró el seguimiento.
     */
    @Column(nullable = false)
    private LocalDateTime fecha;

    /**
     * @return el id de la solicitud
     */
    public Integer getSolicitudId() {
        return solicitudId;
    }

    /**
     * @param solicitudId id de la solicitud a la que pertenece
     */
    public void setSolicitudId(Integer solicitudId) {
        this.solicitudId = solicitudId;
    }

    /**
     * @return las notas del voluntario, o null si no escribió
     */
    public String getNotas() {
        return notas;
    }

    /**
     * @param notas notas del voluntario
     */
    public void setNotas(String notas) {
        this.notas = notas;
    }

    /**
     * @return la duración en minutos
     */
    public Integer getDuracionMinutos() {
        return duracionMinutos;
    }

    /**
     * @param duracionMinutos duración del acompañamiento en minutos
     */
    public void setDuracionMinutos(Integer duracionMinutos) {
        this.duracionMinutos = duracionMinutos;
    }

    /**
     * @return la fecha y hora en que se registró
     */
    public LocalDateTime getFecha() {
        return fecha;
    }

    /**
     * @param fecha fecha y hora del registro
     */
    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }
}