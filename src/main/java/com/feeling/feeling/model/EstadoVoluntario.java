package com.feeling.feeling.model;

/**
 * Estados posibles del perfil de un voluntario.
 *
 * @author Isabella Duque
 * @version 1.0
 */
public enum EstadoVoluntario {
    /** El voluntario aparece en la búsqueda y puede recibir solicitudes. */
    ACTIVO,

    /** El voluntario fue suspendido por un administrador y no recibe solicitudes. */
    SUSPENDIDO

}
