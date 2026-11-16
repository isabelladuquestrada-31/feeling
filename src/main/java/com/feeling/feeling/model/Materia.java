package com.feeling.feeling.model;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Representa una materia académica en la que un voluntario
 * puede ofrecer apoyo.
 *
 * <p>Se identifica por su código, que es único para cada materia.</p>
 *
 * @author Isabella Duque
 * @version 1.0
 */

@Entity
@Table(name = "materia")
public class Materia {

    /** Código único de la materia. Funciona como llave primaria.
     *
     * */
    @Id
    @Column (length = 20)
    private String codigo;

    /** Nombre de la materia.
     *
     * */
    @Column (nullable = false, length = 100)
    private String nombre;

    /**
     * Constructor vacío.
     */
    public Materia() {
    }

    /**
     * Crea una materia con su código y nombre.
     *
     * @param codigo código único de la materia
     * @param nombre nombre de la materia
     */
    public Materia(String codigo, String nombre) {
        this.codigo = codigo;
        this.nombre = nombre;
    }

    /**
     * Obtiene el código de la materia.
     *
     * @return el código de la materia
     */
    public String getCodigo() {
        return codigo;
    }

    /**
     * Obtiene el nombre de la materia.
     *
     * @return el nombre de la materia
     */
    public String getNombre() {
        return nombre;
    }

    /**
     * Cambia el nombre de la materia.
     *
     * @param nombre el nuevo nombre
     */
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
}


