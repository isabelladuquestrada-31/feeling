package com.feeling.feeling.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * Calificación que el estudiante le da al voluntario después de un acompañamiento.
 * Corresponde a la tabla "calificacion". Cada solicitud tiene como máximo una,
 * por eso su id es el mismo de la solicitud.
 *
 * @author Yeymi Cuéllar
 */
@Entity
@Table(name = "calificacion")
public class Calificacion {

    /**
     * Id de la solicitud que se califica. No se genera: lo asigna el servicio.
     */
    @Id
    @Column(name = "solicitud_id")
    private Integer solicitudId;

    /**
     * Estrellas que da el estudiante, de 1 a 5.
     */
    @Column(nullable = false)
    private Integer puntuacion;

    /**
     * Comentario opcional del estudiante.
     */
    @Column(length = 255)
    private String comentario;

    /**
     * Fecha y hora en que se registró la calificación.
     */
    @Column(nullable = false)
    private LocalDateTime fecha;

    /**
     * @return el id de la solicitud calificada
     */
    public Integer getSolicitudId() {
        return solicitudId;
    }

    /**
     * @param solicitudId id de la solicitud que se califica
     */
    public void setSolicitudId(Integer solicitudId) {
        this.solicitudId = solicitudId;
    }

    /**
     * @return las estrellas, de 1 a 5
     */
    public Integer getPuntuacion() {
        return puntuacion;
    }

    /**
     * @param puntuacion estrellas, de 1 a 5
     */
    public void setPuntuacion(Integer puntuacion) {
        this.puntuacion = puntuacion;
    }

    /**
     * @return el comentario, o null si no dejó
     */
    public String getComentario() {
        return comentario;
    }

    /**
     * @param comentario comentario del estudiante
     */
    public void setComentario(String comentario) {
        this.comentario = comentario;
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