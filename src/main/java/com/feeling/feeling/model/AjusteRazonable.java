package com.feeling.feeling.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Un ajuste razonable que el estudiante pide para una solicitud.
 * Corresponde a la tabla "ajuste_razonable". Una solicitud puede tener varios.
 *
 * @author Yeymi
 */
@Entity
@Table(name = "ajuste_razonable")
public class AjusteRazonable {

    /**
     * Identificador único, generado por la base de datos.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * Id de la solicitud a la que pertenece el ajuste.
     */
    @Column(name = "solicitud_id", nullable = false)
    private Integer solicitudId;

    /**
     * Tipo de ajuste que se marcó.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TipoAjuste tipo;

    /**
     * Descripción escrita por el estudiante. Solo se usa cuando el tipo es OTRO.
     */
    @Column(length = 255)
    private String detalle;

    /**
     * @return el id del ajuste
     */
    public Integer getId() {
        return id;
    }

    /**
     * @return el id de la solicitud a la que pertenece
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
     * @return el tipo de ajuste
     */
    public TipoAjuste getTipo() {
        return tipo;
    }

    /**
     * @param tipo tipo de ajuste marcado
     */
    public void setTipo(TipoAjuste tipo) {
        this.tipo = tipo;
    }

    /**
     * @return la descripción del ajuste, o null si no aplica
     */
    public String getDetalle() {
        return detalle;
    }

    /**
     * @param detalle descripción escrita cuando se elige OTRO
     */
    public void setDetalle(String detalle) {
        this.detalle = detalle;
    }
}