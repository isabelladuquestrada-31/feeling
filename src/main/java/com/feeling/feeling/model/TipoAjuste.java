package com.feeling.feeling.model;

/**
 * Tipos de ajuste razonable que el estudiante puede marcar al hacer una solicitud.
 *
 * @author Yeymi
 */
public enum TipoAjuste {
    ESPACIO_ACCESIBLE("Espacio físico accesible"),
    MATERIAL_ACCESIBLE("Material en formato accesible"),
    OTRO("Otro");

    /**
     * Texto que se le muestra a las personas en pantalla.
     */
    private final String etiqueta;

    TipoAjuste(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    /**
     * @return el texto legible del ajuste
     */
    public String getEtiqueta() {
        return etiqueta;
    }
}