package com.feeling.feeling.model;

/**
 * Define los estados por los que puede pasar una solicitud de acompañamiento
 * en Feeling
 *
 * @author Daniel Blandón
 */
public enum EstadoSolicitud {

    /**
     * El estudiante envió una solicitud y el voluntario todavía no responde.
     */
    PENDIENTE,
    /**
     * El voluntario aceptó la solicitud; ambos pueden ver el WhatsApp del otro.
     */
    ACEPTADA,
    /**
     * El voluntario rechazó la solicitud.
     */
    RECHAZADA,
    /**
     * El estudiante canceló la solicitud y la franja horaria del voluntario queda libre.
     */
    CANCELADA,
    /**
     * El acompañamiento se llevó a cabo.
     */
    REALIZADA
}
