package com.feeling.feeling.dto;

import java.time.LocalDateTime;

/**
 * Horario concreto en el que un voluntario puede dar un acompañamiento:
 * sale de una franja de su disponibilidad (por ejemplo, "lunes de 2:00 a 4:00 p. m.")
 * en una fecha próxima y no está ocupado por otra solicitud (RF-08).
 *
 * @param inicio fecha y hora en que empieza (es la fecha programada de la solicitud)
 * @param fin    fecha y hora en que termina la franja
 * @author Daniel Blandón
 * @version 1.0
 */
public record HorarioLibre(LocalDateTime inicio, LocalDateTime fin) {
}
