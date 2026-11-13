package com.feeling.feeling.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "solicitud")

/**
 *Representa una solicitud de acompañamiento académico que un estudiante
 * hace a un voluntario. Corresponde a la tabla "solicitud" de la base de datos.
 *
 * @author Daniel Blandón
 */
public class Solicitud {

    /**
     * Identificador único de la solicitud, generado por la base de datos
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * Id del estudiante que pide el acompañamiento
     */
    @Column(name = "estudiante_id", nullable = false)
    private Integer estudianteId;

    /**
     * Id del voluntario al que se le hace la solicitud
     */
    @Column(name = "voluntario_id", nullable = false)
    private Integer voluntarioId;

    /**
     * Código de la materia en la que el estudiante necesita apoyo
     */
    @Column(name = "materia_codigo", nullable = false, length = 20)
    private String materiaCodigo;

    /**
     * Tema específico que el estudiante quiere trabajar
     */
    @Column(nullable = false, length = 150)
    private String tema;

    /**
     * Tipo de apoyo que pide el estudiante, por ejemplo explicación de un tema
     * o ayuda para repasar para un parcial.
     */
    @Column(nullable = false, length = 30)
    private String tipo;

    /**
     * Modalidad de acompañamiento: presencial o virtual.
     */
    @Column(nullable = false, length = 20)
    private String modalidad;

    /**
     * Estado actual de la solicitud. Empieza en PENDIENTE por defecto.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoSolicitud estado = EstadoSolicitud.PENDIENTE;

    /**
     * Fecha y hora en que se creó la solicitud
     */
    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    /**
     * Fecha y hora en la que se va a realizar el acompañamiento
     */
    @Column(name = "fecha_programada", nullable = false)
    private LocalDateTime fechaProgramada;

    /**
     * Se ejecuta automaticamente antes de guardar la solicitud por primera vez.
     * Pone la fecha de creación y el estado PENDIENTE si no se asignaron.
     */
    @PrePersist
    void alCrear() {
        if (fechaCreacion == null) fechaCreacion = LocalDateTime.now();
        if (estado == null) estado = EstadoSolicitud.PENDIENTE;
    }

    /**
     * @return el id de la solicitud
     */
    public Integer getId() {
        return id;
    }

    /**
     * @return el id del estudiante que hizo la solicitud
     */
    public Integer getEstudianteId() {
        return estudianteId;
    }

    /**
     * @param estudianteId id del estudiante que hace la solicitud
     */
    public void setEstudianteId(Integer estudianteId) {
        this.estudianteId = estudianteId;
    }

    /**
     * @return el id del voluntario que recibe la solicitud
     */
    public Integer getVoluntarioId() {
        return voluntarioId;
    }

    /**
     * @param voluntarioId id del voluntario que recibe la solicitud
     */
    public void setVoluntarioId(Integer voluntarioId) {
        this.voluntarioId = voluntarioId;
    }

    /**
     * @return el código de la materia
     */
    public String getMateriaCodigo() {
        return materiaCodigo;
    }

    /**
     * @param materiaCodigo código de la materia en la que se pide apoyo
     */
    public void setMateriaCodigo(String materiaCodigo) {
        this.materiaCodigo = materiaCodigo;
    }

    /**
     * @return el tema que se quiere trabajar
     */
    public String getTema() {
        return tema;
    }

    /**
     * @param tema tema específico del acompañamiento
     */
    public void setTema(String tema) {
        this.tema = tema;
    }

    /**
     * @return el tipo de apoyo solicitado
     */
    public String getTipo() {
        return tipo;
    }

    /**
     * @param tipo tipo de apoyo solicitado
     */
    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    /**
     * @return la modalidad del acompañamiento
     */
    public String getModalidad() {
        return modalidad;
    }

    /**
     * @param modalidad presencial o virtual
     */
    public void setModalidad(String modalidad) {
        this.modalidad = modalidad;
    }

    /**
     * @return el estado actual de la solicitud
     */
    public EstadoSolicitud getEstado() {
        return estado;
    }

    /**
     * @param estado nuevo estado de la solicitud
     */
    public void setEstado(EstadoSolicitud estado) {
        this.estado = estado;
    }

    /**
     * @return la fecha y hora en que se creó la solicitud
     */
    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    /**
     * @return la fecha y hora programada del acompañamiento
     */
    public LocalDateTime getFechaProgramada() {
        return fechaProgramada;
    }

    /**
     * @param fechaProgramada fecha y hora en que se hará el acompañamiento
     */
    public void setFechaProgramada(LocalDateTime fechaProgramada) {
        this.fechaProgramada = fechaProgramada;
    }


}
