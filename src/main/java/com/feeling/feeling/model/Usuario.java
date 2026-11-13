package com.feeling.feeling.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;
 import jakarta.persistence.Column;

/**
 * Representa a una persona registrada en la plataforma Feeling.
 * Es una clase abstracta: cada usuario es un Estudiante o un Administrador.
 *  * Todos los usuarios se guardan en la tabla "usuario"; la columna "rol" indica el tipo.
 *
 * @author Isabella Duque
 */
@Entity
@Table(name = "usuario")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "rol", length = 20)
public abstract class Usuario {

    /** Identificador único del usuario, generado por la base de datos. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /** Nombre con el que el usuario inicia sesión. No se puede repetir. */
    private String nombreUsuario;

    /** Contraseña encriptada con BCrypt. Nunca se guarda la contraseña real. */
    @Column(name = "contrasena")
    private String contrasenaHash;

    /** Indica si el usuario puede usar la plataforma. El administrador puede desactivarlo. */
    private boolean activo = true;

    /** Fecha y hora en que el usuario se registró. */
    private LocalDateTime fechaRegistro;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public void setNombreUsuario(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
    }

    public String getContrasenaHash() {
        return contrasenaHash;
    }

    public void setContrasenaHash(String contrasenaHash) {
        this.contrasenaHash = contrasenaHash;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(LocalDateTime fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }
}