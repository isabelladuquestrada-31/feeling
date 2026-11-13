package com.feeling.feeling.model;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;


/** Usuario encargado de administrar la plataforma
 * Hereda de Usuario los datos para iniciar esión.
 *
 * @author ISabella Duque
 * @version 1.p
 */

@Entity
@DiscriminatorValue("ADMIN")
public class Administrador extends Usuario{
}
