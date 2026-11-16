package com.feeling.feeling.dto;

/**
 * Clase que representa una opción de los filtros de busqueda: el valor que se envía y el texto que se muestra.
 *
 * @author Daniel Blandón
 */
public class OpcionFiltro {

    /**
     * Valor que se envía al buscar, por ejemplo: MAT101 o TUTORIA.
     */
    private final String valor;

    /**
     * Texto que ve el usuario por ejemplo: Cálculo Diferencial o Tutoría.
     */
    private final String etiqueta;

    /**
     * Crea una opción de filtro.
     *
     * @param valor    valor que se envía
     * @param etiqueta texto que se muestra
     */
    public OpcionFiltro(String valor, String etiqueta) {
        this.valor = valor;
        this.etiqueta = etiqueta;
    }

    /**
     * Getter de valor
     *
     * @return el valor que se envía
     */
    public String getValor() {
        return valor;
    }

    /**
     * Getter de etiqueta
     *
     * @return el texto que se muestra
     */
    public String getEtiqueta() {
        return etiqueta;
    }


}
