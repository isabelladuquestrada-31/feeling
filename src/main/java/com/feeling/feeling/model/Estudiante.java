package com.feeling.feeling.model;
import jakarta.persistence.*;


/**
 * Usuario que usa la plataforma para pedir o brindar un aocmpañamiento.
 * Hereda de Usuario los datos para iniciar sesión.
 * Sus datos propios se guardan en la tabla "estudiante".
 *
 * @author Isabella Duque
 */

@Entity
@DiscriminatorValue("ESTUDIANTE")
@SecondaryTable(name = "estudiante", pkJoinColumns = @PrimaryKeyJoinColumn(name = "usuario_id"))
public class Estudiante extends Usuario{

    /**Programa académico que cursa el estudiante. */
    @Column(name = "programa_academico", table = "estudiante", length = 100, nullable = false)
    private String programaAcademico;

    public String getProgramaAcademico() {
        return programaAcademico;
    }
    public void setProgramaAcademico(String programaAcademico) {
        this.programaAcademico = programaAcademico;
    }
}