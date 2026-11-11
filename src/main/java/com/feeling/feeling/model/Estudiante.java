package com.feeling.feeling.model;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * Usuario que usa la plataforma para pedir o brindar un aocmpañamiento.
 * Hereda de Usuario los datos para iniciar sesión.
 *
 * @author Isabella Duque
 */

@Entity
@Table(name= "Estudiante")
public class Estudiante extends Usuario{

    /**Programa académico que cursa el estudiante. */
    private String programaAcademico;
    public String getProgramaAcademico() {
        return programaAcademico;
    }
    public void setProgramaAcademico(String programaAcademico) {
        this.programaAcademico = programaAcademico;
    }
}