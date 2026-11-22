package com.feeling.feeling.model;

import jakarta.persistence.*;

import java.time.LocalDate;

/**
 * Registro de una suspensión aplicada a un voluntario por un administrador
 *
 * Cada suspensión guarda a quién se suspendió, quién lo hizo, el motivo
 * y el tiempo en que aplica
 * Un voluntario puede tener varias suspensiones
 * a lo largo del tiempo, lo que permite conservar su historial
 *
 * @author Isabella Duque
 * @version 1.0
 */

@Entity
@Table(name= "suspension")
public class Suspension {

    /**
     * Identificador único de la suspensión
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * Voluntario al que se le aplica la suspension
     */
    @ManyToOne
    @JoinColumn (name = "voluntario_id", nullable = false)
    private Voluntario voluntario;

    /**
     * Razón por la que suspende al voluntario
     */
    @ManyToOne
    @JoinColumn(name = "administrador_id", nullable = false)
    private Administrador administrador;

    /**
     * Razón por la que se suspende al voluntario
     */
    @Column(nullable = false)
    private String motivo;

    /**
     * Fecha en que empieza la suspensión
     */
    @Column(nullable = false)
    private LocalDate fechaInicio;

    /**
     * Fecha en que termina la suspensión
     */
    @Column(nullable = false)
    private LocalDate fechaFin;

    /**
     * Constructor vacío
     */
    public Suspension() {
    }

    /**
     * Crea una suspensión que empieza en la fecha actual.
     *
     * @param voluntario voluntario que se suspende
     * @param administrador administrador que aplica la suspensión
     * @param motivo razón de la suspensión
     * @param fechaFin fecha en que termina la suspensión
     */
    public Suspension(Voluntario voluntario, Administrador administrador, String motivo, LocalDate fechaFin) {
        this.voluntario = voluntario;
        this.administrador = administrador;
        this.motivo = motivo;
        this.fechaInicio = LocalDate.now();
        this.fechaFin = fechaFin;
    }

    /**
     * Obtiene el identificador de la suspensión.
     *
     * @return el id de la suspensión
     */
    public Integer getId() {
        return id;
    }

    /**
     * Obtiene el voluntario suspendido.
     *
     * @return el voluntario
     */
    public Voluntario getVoluntario() {
        return voluntario;
    }

    /**
     * Obtiene el administrador que aplicó la suspensión.
     *
     * @return el administrador
     */
    public Administrador getAdministrador() {
        return administrador;
    }

    /**
     * Obtiene el motivo de la suspensión.
     *
     * @return el motivo
     */
    public String getMotivo() {
        return motivo;
    }

    /**
     * Obtiene la fecha en que empezó la suspensión.
     *
     * @return la fecha de inicio
     */
    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    /**
     * Obtiene la fecha en que termina la suspensión.
     *
     * @return la fecha de fin
     */
    public LocalDate getFechaFin() {
        return fechaFin;
    }

}

