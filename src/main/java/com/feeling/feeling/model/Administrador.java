package com.feeling.feeling.model;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/** Usuario encargado de administrar la plataforma
 * Hereda de Usuario los datos para iniciar esión.
 *
 * @author ISabella Duque
 * @version 1.p
 */

@Entity
@Table (name = "Administrador")
public class Administrador extends Usuario{
}
