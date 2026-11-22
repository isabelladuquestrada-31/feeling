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

    /**
     * Obtiene el idenificador de usuario
     *
     * @return el ud del usuario
     */
    public Integer getId() {
        return id;
    }

    /**
     * Asigna el identificador dfel usuario
     *
     * @param id el nuevo id
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * Obtiene el nombre con el que el usuario inicia sesión.
     *
     * @return el nombre del usuuario
     */
    public String getNombreUsuario() {
        return nombreUsuario;
    }

    /**
     * Cambia el nombre con el que el usuario inicia sesión.
     *
     * @param nombreUsuario es el nuevo nombre de usuario
     */
    public void setNombreUsuario(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
    }

    /**
     * Guarda la contraseña ya encriptada del usuario
     * Nunca se debe pasar la contraseña real
     *
     * @return contrasenaHash la constraeña encriptada con BCrypt
     */
    public String getContrasenaHash() {
        return contrasenaHash;
    }

    /**
     * Guarda la contraseña encriptada del usuario
     * Nunca se pasa la contraseña real
     *
     * @param contrasenaHash la contrseña encriptada con BCrypt
     */
    public void setContrasenaHash(String contrasenaHash) {
        this.contrasenaHash = contrasenaHash;
    }

    /**
     * Indica su el usuario puede usar la plataforma
     *
     * @return true si esta activo, false si fue desactivado
     */
    public boolean isActivo() {
        return activo;
    }

    /**
     * Activa o desactiva el usuario en la plataforma
     *
     * @param activo true para activar, false para desactivar
     */
    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    /**
     * Obtiene la fecha y hora en que el usuario se registró.
     *
     * @return la fecha de registro
     */
    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    /**
     * Asigna la fecha y hora en que el usuario se registró.
     *
     * @param fechaRegistro la fecha de registro
     */
    public void setFechaRegistro(LocalDateTime fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }
}