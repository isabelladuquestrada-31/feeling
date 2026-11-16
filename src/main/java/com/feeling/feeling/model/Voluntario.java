package com.feeling.feeling.model;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Perfil de voluntario de un estudiante.
 *
 * <p>Un estudiante puede tener como máximo un perfil de voluntario.
 * Todo voluntario debe dominar al menos una materia y ofrecer
 * al menos un tipo de apoyo.</p>
 *
 * @author Isabella Duque
 * @version 1.0
 */
@Entity
@Table(name = "voluntario")
public class Voluntario {

    /** Identificador único del voluntario. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /** Estudiante dueño de este perfil. Un estudiante solo puede tener un perfil. */
    @OneToOne
    @JoinColumn(name = "estudiante_id", nullable = false, unique = true)
    private Estudiante estudiante;

    /** Materias que domina el voluntario (al menos una). */
    @ManyToMany
    @JoinTable(
            name = "voluntario_materia",
            joinColumns = @JoinColumn(name = "voluntario_id"),
            inverseJoinColumns = @JoinColumn(name = "materia_codigo")
    )
    private List<Materia> materias = new ArrayList<>();

    /** Tipos de apoyo que ofrece el voluntario (al menos uno). */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "voluntario_tipo_apoyo",
            joinColumns = @JoinColumn(name = "voluntario_id")
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_apoyo", nullable = false)
    private List<TipoApoyo> tiposApoyo = new ArrayList<>();

    /** Estado actual del voluntario. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoVoluntario estado;

    /**
     * Constructor vacío requerido por JPA.
     */
    public Voluntario() {
    }

    /**
     * Crea el perfil de voluntario de un estudiante en estado {@link EstadoVoluntario#ACTIVO}.
     *
     * @param estudiante estudiante que se activa como voluntario
     * @param materias   materias que domina
     * @param tiposApoyo tipos de apoyo que ofrece
     */
    public Voluntario(Estudiante estudiante, List<Materia> materias, List<TipoApoyo> tiposApoyo) {
        this.estudiante = estudiante;
        this.materias = new ArrayList<>(materias);
        this.tiposApoyo = new ArrayList<>(tiposApoyo);
        this.estado = EstadoVoluntario.ACTIVO;
    }

    /**
     * Obtiene el identificador del voluntario.
     *
     * @return el id del voluntario
     */
    public Integer getId() {
        return id;
    }

    /**
     * Obtiene el estudiante dueño del perfil.
     *
     * @return el estudiante
     */
    public Estudiante getEstudiante() {
        return estudiante;
    }

    /**
     * Obtiene las materias que domina el voluntario.
     *
     * @return lista de materias
     */
    public List<Materia> getMaterias() {
        return materias;
    }

    /**
     * Reemplaza las materias que domina el voluntario.
     *
     * @param materias nueva lista de materias
     */
    public void setMaterias(List<Materia> materias) {
        this.materias = new ArrayList<>(materias);
    }

    /**
     * Obtiene los tipos de apoyo que ofrece el voluntario.
     *
     * @return lista de tipos de apoyo
     */
    public List<TipoApoyo> getTiposApoyo() {
        return tiposApoyo;
    }

    /**
     * Reemplaza los tipos de apoyo que ofrece el voluntario.
     *
     * @param tiposApoyo nueva lista de tipos de apoyo
     */
    public void setTiposApoyo(List<TipoApoyo> tiposApoyo) {
        this.tiposApoyo = new ArrayList<>(tiposApoyo);
    }

    /**
     * Obtiene el estado actual del voluntario.
     *
     * @return el estado del voluntario
     */
    public EstadoVoluntario getEstado() {
        return estado;
    }

    /**
     * Cambia el estado del voluntario.
     *
     * @param estado el nuevo estado
     */
    public void setEstado(EstadoVoluntario estado) {
        this.estado = estado;
    }
}