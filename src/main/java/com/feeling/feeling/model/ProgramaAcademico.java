package com.feeling.feeling.model;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Programas académicos de pregrado de la Universidad Autónoma de Manizales.
 * Es la única lista válida para el registro y para buscar voluntarios por programa,
 * así no hay variaciones como "Ingeniería de Sistemas" e "Ingeniería en Sistemas".
 * En la base de datos se guarda la etiqueta oficial del programa.
 *
 * @author Daniel Blandón
 * @version 1.0
 */
public enum ProgramaAcademico {

    /** Ingeniería Biomédica. */
    INGENIERIA_BIOMEDICA("Ingeniería Biomédica", Facultades.INGENIERIA),

    /** Ingeniería de Sistemas. */
    INGENIERIA_SISTEMAS("Ingeniería de Sistemas", Facultades.INGENIERIA),

    /** Ingeniería Electrónica. */
    INGENIERIA_ELECTRONICA("Ingeniería Electrónica", Facultades.INGENIERIA),

    /** Ingeniería Industrial. */
    INGENIERIA_INDUSTRIAL("Ingeniería Industrial", Facultades.INGENIERIA),

    /** Ingeniería Mecánica. */
    INGENIERIA_MECANICA("Ingeniería Mecánica", Facultades.INGENIERIA),

    /** Fisioterapia. */
    FISIOTERAPIA("Fisioterapia", Facultades.SALUD),

    /** Odontología. */
    ODONTOLOGIA("Odontología", Facultades.SALUD),

    /** Tecnología en Atención Prehospitalaria. */
    ATENCION_PREHOSPITALARIA("Tecnología en Atención Prehospitalaria", Facultades.SALUD),

    /** Administración de Empresas. */
    ADMINISTRACION_EMPRESAS("Administración de Empresas", Facultades.SOCIALES),

    /** Artes Culinarias y Gastronomía. */
    ARTES_CULINARIAS("Artes Culinarias y Gastronomía", Facultades.SOCIALES),

    /** Ciencia Política, Gobierno y Relaciones Internacionales. */
    CIENCIA_POLITICA("Ciencia Política, Gobierno y Relaciones Internacionales", Facultades.SOCIALES),

    /** Diseño de Modas. */
    DISENO_MODAS("Diseño de Modas", Facultades.SOCIALES),

    /** Diseño Industrial. */
    DISENO_INDUSTRIAL("Diseño Industrial", Facultades.SOCIALES),

    /** Economía. */
    ECONOMIA("Economía", Facultades.SOCIALES),

    /** Negocios Internacionales. */
    NEGOCIOS_INTERNACIONALES("Negocios Internacionales", Facultades.SOCIALES);

    private final String etiqueta;
    private final String facultad;

    /**
     * Crea un programa con el nombre que ve el estudiante y su facultad.
     *
     * @param etiqueta nombre oficial del programa
     * @param facultad facultad a la que pertenece
     */
    ProgramaAcademico(String etiqueta, String facultad) {
        this.etiqueta = etiqueta;
        this.facultad = facultad;
    }

    /**
     * Devuelve el código que viaja en los formularios, por ejemplo INGENIERIA_SISTEMAS.
     *
     * @return código del programa
     */
    public String getCodigo() {
        return name();
    }

    /**
     * Devuelve el nombre oficial del programa, que es el que se guarda y se muestra.
     *
     * @return nombre del programa
     */
    public String getEtiqueta() {
        return etiqueta;
    }

    /**
     * Devuelve la facultad del programa, para agrupar las opciones en la vista.
     *
     * @return nombre de la facultad
     */
    public String getFacultad() {
        return facultad;
    }

    /**
     * Busca un programa por el código que llega del formulario.
     *
     * @param codigo código del programa, por ejemplo INGENIERIA_SISTEMAS
     * @return el programa, o vacío si el código no existe
     */
    public static Optional<ProgramaAcademico> desdeCodigo(String codigo) {
        if (codigo == null || codigo.isBlank()) {
            return Optional.empty();
        }
        return Arrays.stream(values())
                .filter(programa -> programa.name().equals(codigo.trim()))
                .findFirst();
    }

    /**
     * Busca un programa por su nombre oficial, tal como se guarda en la base de datos.
     *
     * @param etiqueta nombre oficial del programa
     * @return el programa, o vacío si no es un programa de la lista
     */
    public static Optional<ProgramaAcademico> desdeEtiqueta(String etiqueta) {
        if (etiqueta == null || etiqueta.isBlank()) {
            return Optional.empty();
        }
        return Arrays.stream(values())
                .filter(programa -> programa.etiqueta.equals(etiqueta.trim()))
                .findFirst();
    }

    /**
     * Agrupa los programas por facultad, en el orden de la lista, para mostrarlos
     * en un select con grupos.
     *
     * @return mapa de facultad a sus programas
     */
    public static Map<String, List<ProgramaAcademico>> porFacultad() {
        return Arrays.stream(values())
                .collect(Collectors.groupingBy(ProgramaAcademico::getFacultad,
                        LinkedHashMap::new, Collectors.toList()));
    }

    /**
     * Nombres de las facultades de la UAM que se usan para agrupar los programas.
     */
    private static final class Facultades {
        private static final String INGENIERIA = "Ingeniería";
        private static final String SALUD = "Salud";
        private static final String SOCIALES = "Estudios Sociales y Empresariales";
    }
}
