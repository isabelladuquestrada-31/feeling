package com.feeling.feeling.model;
import jakarta.persistence.*;

import java.util.List;


/**
 * Usuario que usa la plataforma para pedir o brindar un aocmpañamiento.
 * Hereda de Usuario los datos para iniciar sesión.
 * Sus datos propios se guardan en la tabla "estudiante".
 *
 * @author Isabella Duque
 * @version 1.0
 */

@Entity
@DiscriminatorValue("ESTUDIANTE")
@SecondaryTable(name = "estudiante", pkJoinColumns = @PrimaryKeyJoinColumn(name = "usuario_id"))
public class Estudiante extends Usuario{

    /**Programa académico que cursa el estudiante. */
    @Column(name = "programa_academico", table = "estudiante", length = 100, nullable = false)
    private String programaAcademico;

    /** Perfil de voluntario del estudiante. Es null si todavía no se ha activado.
     *
     */
    @OneToOne(mappedBy = "estudiante")
    private Voluntario voluntario;

    public String getProgramaAcademico() {
        return programaAcademico;
    }
    public void setProgramaAcademico(String programaAcademico) {
        this.programaAcademico = programaAcademico;
    }

    /**
     * Activa al estudiante como voluntario con las materias y tipos de apoyo elegidos.
     *
     * @param materias   materias que domina (al menos una)
     * @param tiposApoyo tipos de apoyo que ofrece (al menos uno)
     * @throws IllegalStateException    si el estudiante ya es voluntario
     * @throws IllegalArgumentException si no eligió ninguna materia o ningún tipo de apoyo
     */
    public void activarseComoVoluntario(List<Materia> materias, List<TipoApoyo> tiposApoyo) {
        if (voluntario != null) {
            throw new IllegalStateException("Ya estás registrado como voluntario.");
        }
        if (materias == null || materias.isEmpty()) {
            throw new IllegalArgumentException("Debes elegir al menos una materia.");
        }
        if (tiposApoyo == null || tiposApoyo.isEmpty()) {
            throw new IllegalArgumentException("Debes elegir al menos un tipo de apoyo.");
        }
        this.voluntario = new Voluntario(this, materias, tiposApoyo);
    }

    /**
     * Obtiene el perfil de voluntario del estudiante.
     *
     * @return el perfil de voluntario, o null si no se ha activado
     */
    public Voluntario getVoluntario() {
        return voluntario;
    }
}