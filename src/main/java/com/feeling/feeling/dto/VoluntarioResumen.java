package com.feeling.feeling.dto;

import java.util.List;

/**
 * Datos de un voluntarios que se muestran en la busqueda (RF-07)
 * No incluye el número de WhatsApp: solo se comparte cuando una solicitud es aceptada (RF-11)
 *
 * @author Daniel Blandón
 */
public class VoluntarioResumen {

    /**
     * Identificador del voluntario
     */
    private final Integer id;

    /**
     * Nombre preferido del voluntario, o su nombre de usuario si no tiene nombre preferido
     */
    private final String nombre;

    /**
     * Programa académico que estudia el voluntario
     */
    private final String programa;

    /**
     * Nombres de las materias en las que ofrece apoyo
     */
    private final List<String> materias;

    /**
     * Tipos de apoyo que ofrece (tutoria, explicación de conceptos, ect.).
     */
    private final List<String> tiposApoyo;

    /**
     * Días de la semana en los que tiene disponibilidad
     */
    private final List<String> dias;

    /**
     * Crea el resumen de un voluntario
     *
     * @param id         id del voluntario
     * @param nombre     nombre que se muestra
     * @param programa   programa academico
     * @param materias   materias en las que ayuda
     * @param tiposApoyo tipos de apoyo que ofrece
     * @param dias       dias con disponibilidad
     */
    public VoluntarioResumen(Integer id, String nombre, String programa, List<String> materias, List<String> tiposApoyo, List<String> dias) {
        this.id = id;
        this.nombre = nombre;
        this.programa = programa;
        this.materias = materias;
        this.tiposApoyo = tiposApoyo;
        this.dias = dias;
    }

    /**
     * Getter del id
     *
     * @return el id del voluntario
     */
    public Integer getId() {
        return id;
    }

    /**
     * Getter de nombre
     *
     * @return el nombre que se muestra del voluntario
     */
    public String getNombre() {
        return nombre;
    }

    /**
     * Getter de programa
     *
     * @return el programa académico del voluntario
     */
    public String getPrograma() {
        return programa;
    }

    /**
     * Getter de materias
     *
     * @return las materias en las que ayuda el voluntario
     */
    public List<String> getMaterias() {
        return materias;
    }

    /**
     * Getter de tipos de apoyo
     *
     * @return los tipos de apoyo que ofrece el voluntario
     */
    public List<String> getTiposApoyo() {
        return tiposApoyo;
    }

    /**
     * Getter de dias
     *
     * @return los dias con disponibilidad del voluntario
     */
    public List<String> getDias() {
        return dias;
    }
}
