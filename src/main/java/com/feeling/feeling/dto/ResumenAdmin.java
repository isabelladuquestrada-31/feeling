package com.feeling.feeling.dto;

/**
 * Cifras que ve el administrador en su pantalla de inicio (SCRUM-53).
 *
 * @param estudiantes           estudiantes registrados
 * @param voluntariosActivos    voluntarios que aparecen en la búsqueda
 * @param voluntariosSuspendidos voluntarios suspendidos por un administrador
 * @author Daniel Blandón
 * @version 1.0
 */
public record ResumenAdmin(long estudiantes, long voluntariosActivos, long voluntariosSuspendidos) {
}
